package com.grabpt.service.ContractService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.ContractHandler;
import com.grabpt.aws.s3.AmazonS3Manager;
import com.grabpt.domain.entity.*;
import com.grabpt.domain.enums.MatchingStatus;
import com.grabpt.dto.request.ContractRequest;
import com.grabpt.repository.ContractRepository.ContractRepository;
import com.grabpt.service.AlarmService.AlarmService;
import com.grabpt.service.PdfService.PdfGenerateService;

import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
	private final ContractRepository contractRepository;
	private final AlarmService alarmService;
	private final PdfGenerateService pdfGenerateService;
	private final TemplateEngine templateEngine;
	private final AmazonS3Manager amazonS3Manager;


	@Override
	@Transactional
	public Contract writeUserInfo(Long contractId, ContractRequest.ContractInfoDto request){
		Contract contract = contractRepository.findById(contractId).orElseThrow(() -> new ContractHandler(ErrorStatus.CONTRACT_NOT_FOUND));
		contract.getMatching().setStatus(MatchingStatus.USERWROTE);

		ContractInfo contractInfo = toContractInfo(request);
		contract.setUserInfo(contractInfo);

		Long proId = contract.getMatching().getSuggestion().getProProfile().getUser().getId();
		alarmService.sendAlarm(proId, "CONTRACT", "수강생 계약서 작성 완료",
			contractInfo.getName()+"님이 계약서 작성을 완료했습니다. 계약서를 작성해주세요.", "/contract/"+contractId);
		return contract;
	}

	@Override
	@Transactional
	public Contract writeProInfo(Long contractId, ContractRequest.ContractInfoDto request){
		Contract contract = contractRepository.findById(contractId).orElseThrow(() -> new ContractHandler(ErrorStatus.CONTRACT_NOT_FOUND));
		contract.getMatching().setStatus(MatchingStatus.COMPLETED);

		ContractInfo contractInfo = toContractInfo(request);
		contract.setProInfo(contractInfo);

		Long userId = contract.getMatching().getRequestion().getUser().getId(); //너무 길긴 함
		alarmService.sendAlarm(userId, "PAYMENT", "전문가 계약서 작성 완료",
			contractInfo.getName()+"님의 계약서 작성이 완료되었어요. 결제를 진행해주세요", "/contract/"+contractId);
		return contract;
	}

	private ContractInfo toContractInfo(ContractRequest.ContractInfoDto request) {
		ContractInfo userInfo = new ContractInfo();
		userInfo.setAddress(request.getAddress());
		userInfo.setName(request.getName());
		userInfo.setBirth(request.getBirth());
		userInfo.setGender(request.getGender());
		userInfo.setPhoneNumber(request.getPhoneNumber());
		userInfo.setSignUrl(request.getSignUrl());
		return userInfo;
	}

	@Override
	@Transactional
	public Contract createContract(Matching matching, Requestions req, Suggestions sug){
		Contract contract = Contract.builder()
			.matching(matching)
			.price(sug.getPrice())
			.ptAddress(sug.getLocation())
			.startDate(req.getStartPreference())
			.totalSession(sug.getSessionCount())
			.build();

		return contractRepository.save(contract);
	}

	@Override
	public Contract findById(Long contractId){
		return contractRepository.findById(contractId)
			.orElseThrow(() -> new ContractHandler(ErrorStatus.CONTRACT_NOT_FOUND));
	}

	/**
	 * 계약서 PDF를 생성하여 S3에 업로드하고, 파일 URL을 DB에 저장하는 최종 메소드
	 * @param contractId 계약서 ID
	 * @return S3에 저장된 파일의 URL
	 */
	@Transactional
	public String generateAndSavePdfToS3(Long contractId) {
		Contract contract = contractRepository.findById(contractId)
			.orElseThrow(() -> new RuntimeException("계약 정보를 찾을 수 없습니다. ID: " + contractId));

		Context context = new Context();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

		// --- Null 안전한 데이터 처리 ---
		if (contract.getUserInfo() != null) {
			context.setVariable("userName", contract.getUserInfo().getName());
			context.setVariable("userBirth", contract.getUserInfo().getBirth() != null ? contract.getUserInfo().getBirth().format(dateFormatter) : "-");
			context.setVariable("userPhoneNumber", contract.getUserInfo().getPhoneNumber());
			context.setVariable("userAddress", contract.getUserInfo().getAddress());
		} else {
			context.setVariable("userName", "입력 전");
			context.setVariable("userBirth", "-");
			context.setVariable("userPhoneNumber", "-");
			context.setVariable("userAddress", "-");
		}

		if (contract.getProInfo() != null) {
			context.setVariable("proName", contract.getProInfo().getName());
			context.setVariable("proBirth", contract.getProInfo().getBirth() != null ? contract.getProInfo().getBirth().format(dateFormatter) : "-");
			context.setVariable("proPhoneNumber", contract.getProInfo().getPhoneNumber());
			context.setVariable("proAddress", contract.getProInfo().getAddress());
		} else {
			context.setVariable("proName", "입력 전");
			context.setVariable("proBirth", "-");
			context.setVariable("proPhoneNumber", "-");
			context.setVariable("proAddress", "-");
		}

		context.setVariable("totalSession", contract.getTotalSession() != null ? contract.getTotalSession() : "-");
		context.setVariable("price", contract.getPrice() != null ? contract.getPrice() : 0);
		context.setVariable("startDate", contract.getStartDate() != null ? contract.getStartDate().format(dateFormatter) : "-");
		context.setVariable("ptAddress", contract.getPtAddress() != null ? contract.getPtAddress() : "-");
		context.setVariable("contractDate", contract.getContractDate() != null ? contract.getContractDate().format(dateFormatter) : "미지정");

		try {
			String html = templateEngine.process("contract_template", context);
			ByteArrayInputStream pdfInputStream = pdfGenerateService.generatePdfFromHtml(html);
			long contentLength = pdfInputStream.available();
			String objectKey = "contracts/contract_" + contract.getId() + ".pdf";

			String fileUrl = amazonS3Manager.uploadInputStream(objectKey, pdfInputStream, contentLength, MediaType.APPLICATION_PDF_VALUE);
			contract.setContractFileUrl(fileUrl);

			return fileUrl;
		} catch (IOException e) {
			log.error("PDF 생성 또는 S3 업로드 중 IOException 발생", e);
			throw new RuntimeException("PDF 처리 중 오류가 발생했습니다.", e);
		}
	}

}
