package com.grabpt.service.ContractService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

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
import com.grabpt.domain.enums.Gender;
import com.grabpt.domain.enums.MatchingStatus;
import com.grabpt.dto.request.ContractRequest;
import com.grabpt.repository.ContractRepository.ContractRepository;
import com.grabpt.service.AlarmService.AlarmService;
import com.grabpt.service.PdfService.PdfGenerateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
	private final ContractRepository contractRepository;
	private final AlarmService alarmService;
	private final PdfGenerateService pdfGenerateService;
	private final TemplateEngine templateEngine;
	private final AmazonS3Manager amazonS3Manager;
	private final AmazonConfig amazonConfig;

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
		contract.setContractDate(request.getContractDate());

		Long userId = contract.getMatching().getRequestion().getUser().getId(); //너무 길긴 함
		alarmService.sendAlarm(userId, "PAYMENT", "전문가 계약서 작성 완료",
			contractInfo.getName() + "님의 계약서 작성이 완료되었어요. 결제를 진행해주세요", "/contracts/new/" + contractId);
		return contract;
	}

	private ContractInfo toContractInfo(ContractRequest.ContractInfoDto request) {
		ContractInfo contractInfo = new ContractInfo();
		contractInfo.setAddress(request.getAddress());
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
			"gender", userInfo != null ? userInfo.getGender() : Gender.FEMALE, // HTML에서 'MALE'/'FEMALE'로 분기 처리
			"address", userInfo != null ? userInfo.getAddress() : "-",
			"signImageUrl", userInfo != null ? userInfo.getSignUrl() : null // 서명 이미지 URL
		);
		context.setVariable("member", member);

		// --- 2. 트레이너(trainer) 정보 설정 ---
		ContractInfo proInfo = contract.getProInfo();
		Map<String, Object> trainer = Map.of(
			"name", proInfo != null ? proInfo.getName() : "입력 전",
			"birth", proInfo != null && proInfo.getBirth() != null ? proInfo.getBirth().format(dateFormatter) : "-",
			"phoneNumber", proInfo != null ? proInfo.getPhoneNumber() : "-",
			"gender", proInfo != null ? proInfo.getGender() : Gender.MALE,
			"address", proInfo != null ? proInfo.getAddress() : "-",
			"signImageUrl", proInfo != null ? proInfo.getSignUrl() : null // 서명 이미지 URL
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
