package com.grabpt.service.ContractService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.ContractHandler;
import com.grabpt.aws.s3.AmazonS3Manager;
import com.grabpt.config.AmazonConfig;
import com.grabpt.domain.entity.Contract;
import com.grabpt.domain.entity.ContractInfo;
import com.grabpt.domain.entity.Matching;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.entity.Suggestions;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Gender;
import com.grabpt.domain.enums.MatchingStatus;
import com.grabpt.domain.enums.PaymentStatus;
import com.grabpt.domain.enums.Role;
import com.grabpt.dto.request.ContractRequest;
import com.grabpt.dto.response.ContractResponse;
import com.grabpt.repository.ContractRepository.ContractRepository;
import com.grabpt.repository.MatchingRepository.MatchingRepository;
import com.grabpt.service.AlarmService.AlarmService;
import com.grabpt.service.PdfService.PdfGenerateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
	private final ContractRepository contractRepository;
	private final MatchingRepository matchingRepository;
	private final AlarmService alarmService;
	private final PdfGenerateService pdfGenerateService;
	private final TemplateEngine templateEngine;
	private final AmazonS3Manager amazonS3Manager;
	private final AmazonConfig amazonConfig;

	private static final List<MatchingStatus> ALL_CONTRACT_STATUSES =
		List.of(MatchingStatus.MATCHED, MatchingStatus.USERWROTE, MatchingStatus.COMPLETED);

	@Override
	@Transactional
	public Contract writeUserInfo(Long contractId, ContractRequest.ContractInfoDto request) {
		Contract contract = contractRepository.findById(contractId)
			.orElseThrow(() -> new ContractHandler(ErrorStatus.CONTRACT_NOT_FOUND));
		contract.getMatching().setStatus(MatchingStatus.USERWROTE);

		ContractInfo contractInfo = toContractInfo(request);
		contract.setUserInfo(contractInfo);

		Long proId = contract.getMatching().getSuggestion().getProProfile().getUser().getId();
		alarmService.sendAlarm(proId, "CONTRACT", "수강생 계약서 작성 완료",
			contractInfo.getName() + "님이 계약서 작성을 완료했습니다. 계약서를 작성해주세요.", "/contracts/new/" + contractId);
		return contract;
	}

	@Override
	@Transactional
	public Contract writeProInfo(Long contractId, ContractRequest.ContractInfoForProDto request) {
		Contract contract = contractRepository.findById(contractId)
			.orElseThrow(() -> new ContractHandler(ErrorStatus.CONTRACT_NOT_FOUND));
		contract.getMatching().setStatus(MatchingStatus.COMPLETED);

		ContractInfo contractInfo = toContractInfo(request);
		contract.setProInfo(contractInfo);
		contract.setStartDate(request.getStartDate());
		contract.setContractDate(request.getExpireDate());

		Long userId = contract.getMatching().getRequestion().getUser().getId(); //너무 길긴 함
		alarmService.sendAlarm(userId, "PAYMENT", "전문가 계약서 작성 완료",
			contractInfo.getName() + "님의 계약서 작성이 완료되었어요. 결제를 진행해주세요", "/contracts/new/" + contractId);
		return contract;
	}

	private ContractInfo toContractInfo(ContractRequest.ContractInfoDto request) {
		ContractInfo contractInfo = new ContractInfo();
		contractInfo.setLocation(request.getLocation());
		contractInfo.setName(request.getName());
		contractInfo.setBirth(request.getBirth());
		contractInfo.setGender(request.getGender());
		contractInfo.setPhoneNumber(request.getPhoneNumber());
		return contractInfo;
	}


	@Override
	@Transactional
	public Contract createContract(Matching matching, Requestions req, Suggestions sug) {
		Contract contract = new Contract(); // 기본 생성자 사용
		contract.setMatching(matching);
		contract.setPrice(sug.getPrice());
		contract.setPtAddress(sug.getLocation());
		contract.setTotalSession(sug.getSessionCount());
		contract.setUserInfo(new ContractInfo());
		contract.setProInfo(new ContractInfo());
		contract.setContractDate(LocalDate.now());

		return contractRepository.save(contract);
	}

	@Override
	public Contract findById(Long contractId) {
		return contractRepository.findById(contractId)
			.orElseThrow(() -> new ContractHandler(ErrorStatus.CONTRACT_NOT_FOUND));
	}

	@Override
	@Transactional(readOnly = true)
	public ContractResponse.ContractListResponseDto getContractList(Role role, Long userId, PaymentStatus paymentStatus, Pageable pageable) {
		Page<Matching> matchings;
		long totalActive;
		long totalCompleted;

		if (role == Role.USER) {
			if (paymentStatus == PaymentStatus.OK) {
				matchings = matchingRepository.findContractsByUserIdAndPaymentStatusOK(userId, ALL_CONTRACT_STATUSES, pageable);
			} else if (paymentStatus == PaymentStatus.READY) {
				matchings = matchingRepository.findContractsByUserIdAndPaymentStatusReady(userId, ALL_CONTRACT_STATUSES, pageable);
			} else {
				matchings = matchingRepository.findContractsByUserId(userId, ALL_CONTRACT_STATUSES, pageable);
			}
			totalActive = matchingRepository.countContractsByUserIdAndNotPaymentStatus(userId, ALL_CONTRACT_STATUSES, PaymentStatus.OK);
			totalCompleted = matchingRepository.countContractsByUserIdAndPaymentStatus(userId, ALL_CONTRACT_STATUSES, PaymentStatus.OK);
		} else {
			if (paymentStatus == PaymentStatus.OK) {
				matchings = matchingRepository.findContractsByProUserIdAndPaymentStatusOK(userId, ALL_CONTRACT_STATUSES, pageable);
			} else if (paymentStatus == PaymentStatus.READY) {
				matchings = matchingRepository.findContractsByProUserIdAndPaymentStatusReady(userId, ALL_CONTRACT_STATUSES, pageable);
			} else {
				matchings = matchingRepository.findContractsByProUserId(userId, ALL_CONTRACT_STATUSES, pageable);
			}
			totalActive = matchingRepository.countContractsByProUserIdAndNotPaymentStatus(userId, ALL_CONTRACT_STATUSES, PaymentStatus.OK);
			totalCompleted = matchingRepository.countContractsByProUserIdAndPaymentStatus(userId, ALL_CONTRACT_STATUSES, PaymentStatus.OK);
		}

		Page<ContractResponse.ContractListItemDto> items = matchings.map(m -> toContractListItem(m, role));

		return ContractResponse.ContractListResponseDto.builder()
			.totalActiveContracts((int) totalActive)
			.totalCompletedContracts((int) totalCompleted)
			.contracts(items)
			.build();
	}

	private ContractResponse.ContractListItemDto toContractListItem(Matching matching, Role role) {
		String nickname;
		String profileImageUrl;

		if (role == Role.USER) {
			Users proUser = matching.getSuggestion().getProProfile().getUser();
			nickname = proUser.getNickname();
			profileImageUrl = proUser.getProfileImageUrl();
		} else {
			Users userEntity = matching.getRequestion().getUser();
			nickname = userEntity.getNickname();
			profileImageUrl = userEntity.getProfileImageUrl();
		}

		Contract contract = matching.getContract();

		PaymentStatus paymentStatus = matching.getOrders().stream()
			.filter(o -> o.getPayment() != null)
			.map(o -> o.getPayment().getStatus())
			.filter(s -> s == PaymentStatus.OK)
			.findFirst()
			.orElse(PaymentStatus.READY);

		return ContractResponse.ContractListItemDto.builder()
			.contractId(contract != null ? contract.getId() : null)
			.userNickname(nickname)
			.profileImageUrl(profileImageUrl)
			.paymentStatus(paymentStatus)
			.sessionCount(contract != null ? contract.getTotalSession() : null)
			.contractPrice(contract != null ? contract.getPrice() : null)
			.startDate(contract != null ? contract.getStartDate() : null)
			.expireDate(contract != null ? contract.getContractDate() : null)
			.build();
	}

	@Transactional
	public String generateAndSavePdfToS3(Long contractId) {
		Contract contract = contractRepository.findById(contractId)
			.orElseThrow(() -> new RuntimeException("계약 정보를 찾을 수 없습니다. ID: " + contractId));

		Context context = new Context();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일");

		// --- 1. 회원(member) 정보 설정 ---
		ContractInfo userInfo = contract.getUserInfo();
		Map<String, Object> member = Map.of(
			"name", userInfo != null ? userInfo.getName() : "입력 전",
			"birth", userInfo != null && userInfo.getBirth() != null ? userInfo.getBirth().format(dateFormatter) : "-",
			"phoneNumber", userInfo != null ? userInfo.getPhoneNumber() : "-",
			"gender", (userInfo != null && userInfo.getGender() != null) ? userInfo.getGender().getKorean() : Gender.MALE.getKorean(),
			"address", userInfo != null ? userInfo.getLocation() : "-",
			"signImageUrl", userInfo != null ? userInfo.getSignImageUrl() : null // 서명 이미지 URL
		);
		context.setVariable("member", member);

		// --- 2. 트레이너(trainer) 정보 설정 ---
		ContractInfo proInfo = contract.getProInfo();
		Map<String, Object> trainer = Map.of(
			"name", proInfo != null ? proInfo.getName() : "입력 전",
			"birth", proInfo != null && proInfo.getBirth() != null ? proInfo.getBirth().format(dateFormatter) : "-",
			"phoneNumber", proInfo != null ? proInfo.getPhoneNumber() : "-",
			"gender", (proInfo != null && proInfo.getGender() != null) ? proInfo.getGender().getKorean() : Gender.MALE.getKorean(),
			"address", proInfo != null ? proInfo.getLocation() : "-",
			"signImageUrl", proInfo != null ? proInfo.getSignImageUrl() : null // 서명 이미지 URL
		);
		context.setVariable("trainer", trainer);

		// --- 3. 서비스(service) 정보 설정 ---
		Integer totalSession = contract.getTotalSession() != null ? contract.getTotalSession() : 0;
		Integer pricePerSession = contract.getPrice() != null ? contract.getPrice() : 0;
		long totalPrice = (long)totalSession * pricePerSession;

		// 유효기간 (예: 시작일로부터 3개월) - 정책에 맞게 수정 필요
		String endDateStr = "-";
		if (contract.getStartDate() != null) {
			endDateStr = contract.getStartDate().plusMonths(3).format(dateFormatter);
		}

		Map<String, Object> service = Map.of(
			"totalSession", totalSession,
			"price", pricePerSession,
			"totalPrice", totalPrice,
			"startDate", contract.getStartDate() != null ? contract.getStartDate().format(dateFormatter) : "-",
			"endDate", endDateStr,
			"ptAddress", contract.getPtAddress() != null ? contract.getPtAddress() : "-"
		);
		context.setVariable("service", service);

		// --- 4. 기타 정보 설정 ---
		context.setVariable("agreements", Map.of("termsAccepted", true)); // 필수 약관은 항상 동의했다고 가정
		context.setVariable("contractDate", contract.getContractDate());; // 계약 생성일
		context.setVariable("appLogoUrl",
			"https://grabpt-image-bucket-2.s3.ap-northeast-2.amazonaws.com/AppLogo.png/2025-08-14T01%3A38%3A19.770886374");

		// --- 5. PDF 생성 및 S3 업로드 ---
		try {
			// 템플릿 파일명을 정확하게 지정합니다.
			String html = templateEngine.process("contract_template.html", context);

			// 이 부분은 수정 없이 그대로 작동합니다.
			ByteArrayInputStream pdfInputStream = pdfGenerateService.generatePdfFromHtml(html);

			long contentLength = pdfInputStream.available();
			String objectKey = "contracts/contract_" + contract.getId() + ".pdf";

			String fileUrl = amazonS3Manager.uploadInputStream(objectKey, pdfInputStream, contentLength,
				"application/pdf");
			contract.setContractFileUrl(fileUrl); // 생성된 PDF의 URL을 DB에 저장

			return fileUrl;
		} catch (IOException e) {
			log.error("PDF 생성 또는 S3 업로드 중 IOException 발생", e);
			throw new RuntimeException("PDF 처리 중 오류가 발생했습니다.", e);
		}
	}

}
