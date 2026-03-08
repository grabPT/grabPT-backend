package com.grabpt.controller;

import com.grabpt.service.ContractService.ContractPhotoServiceImpl;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.converter.ContractConverter;
import com.grabpt.domain.entity.Contract;
import com.grabpt.domain.enums.PaymentStatus;
import com.grabpt.domain.enums.Role;
import com.grabpt.dto.request.ContractRequest;
import com.grabpt.dto.response.ContractResponse;
import com.grabpt.service.ContractService.ContractService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ContractController {

	private final ContractService contractService;
	private final ContractPhotoServiceImpl contractPhotoService;

	@Operation(
		summary = "계약 내역 목록 조회 API",
		description = "role(USER/PRO)과 userId를 기반으로 계약 목록을 조회합니다. " +
			"USER 조회 시 상대방(전문가) 정보, PRO 조회 시 상대방(회원) 정보를 반환합니다."
	)
	@GetMapping("/contract/list")
	public ApiResponse<ContractResponse.ContractListResponseDto> getContractList(
		@RequestParam Role role,
		@RequestParam Long userId,
		@RequestParam(required = false) PaymentStatus paymentStatus,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
		return ApiResponse.onSuccess(contractService.getContractList(role, userId, paymentStatus, pageable));
	}

	@Operation(
		description = "계약서 Id를 통해 계약서에 대한 정보를 조회합니다",
		summary = "계약서 정보 조회 API"
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ContractResponse.ContractResponseDto.class)
			)
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "CONTRACT_NOT_FOUND"),
	})
	@GetMapping("/contract/{contractId}")
	public ApiResponse<ContractResponse.ContractResponseDto> getContract(@PathVariable(name = "contractId") Long id) {
		Contract contract = contractService.findById(id);
		return ApiResponse.onSuccess(ContractConverter.toContractResponseDto(contract));
	}

	@Operation(
		summary = "계약서 PDF 링크 조회 API",
		description = "contractId를 통해 계약서의 PDF 링크를 조회합니다."
	)
	@GetMapping("/contract/{contractId}/pdf")
	public ApiResponse<String> getContractPdf(@PathVariable(name = "contractId") Long id) {
		Contract contract = contractService.findById(id);
		if (contract.getContractFileUrl() == null || contract.getContractFileUrl().isEmpty()) {
			return ApiResponse.onFailure("PDF_NOT_FOUND", "PDF가 아직 생성되지 않았습니다.", null);
		}
		return ApiResponse.onSuccess(contract.getContractFileUrl());
	}


	@Operation(
		description = "수강생이 계약서 정보를 입력한 뒤 제출하며 Matching status가 USERWROTE로 업데이트 됩니다",
		summary = "수강생 계약서 작성 API"
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "CONTRACT_NOT_FOUND"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
	})
	@PostMapping("/contract/{contractId}/user")
	public ApiResponse<Long> writeUserInfo(@RequestBody ContractRequest.ContractInfoDto request,
		@PathVariable(name = "contractId") Long id) {
		Contract contract = contractService.writeUserInfo(id, request);
		return ApiResponse.onSuccess(contract.getId());
	}

	@Operation(
		description = "트레이너가 계약서 정보를 입력한 뒤 제출하며 Matching status가 COMPLETE로 업데이트 됩니다",
		summary = "트레이너 계약서 작성 API"
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "CONTRACT_NOT_FOUND"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
	})
	@PostMapping("/contract/{contractId}/pro")
	public ApiResponse<Long> writeProInfo(@RequestBody ContractRequest.ContractInfoForProDto request,
		@PathVariable(name = "contractId") Long id) {
		Contract contract = contractService.writeProInfo(id, request);
		return ApiResponse.onSuccess(contract.getId());
	}

	@Operation(
		summary = "계약서 PDF 생성 및 S3 저장 API",
		description = "계약서 ID를 받아 PDF를 생성하고 S3에 업로드한 뒤, 파일 URL을 DB에 저장합니다."
	)
	@PostMapping("/contract/{contractId}/submit")
	public ApiResponse<String> generateAndSavePdf(@PathVariable Long contractId) {
		try {
			String fileUrl = contractService.generateAndSavePdfToS3(contractId);
			return ApiResponse.onSuccess(fileUrl);
		} catch (Exception e) {
			// 서비스에서 RuntimeException으로 예외를 던지므로, 여기서 잡아서 처리합니다.
			return ApiResponse.onFailure("PDF_PROCESSING_ERROR", e.getMessage(), null);
		}
	}

	@Operation(
		summary = "수강생 전자서명 업로드 API",
		description = "수강생이 contractId와 MultipartFile을 통해 전자서명을 업로드 합니다"
	)@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "CONTRACT_NOT_FOUND"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
	})
	@PostMapping(value = "/contract/{contractId}/uploadUserSign", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ApiResponse<String> uploadUserSign(@PathVariable(name = "contractId") Long contractId, @RequestPart MultipartFile file) {
		contractPhotoService.uploadUserSign(contractId,file);
		return ApiResponse.onSuccess("수강생 전자서명 upload");
	}

	@Operation(
		summary = "트레이너 전자서명 업로드 API",
		description = "트레이너가 contractId와 MultipartFile을 통해 전자서명을 업로드 합니다"
	)@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "CONTRACT_NOT_FOUND"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
	})
	@PostMapping(value = "/contract/{contractId}/uploadProSign",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ApiResponse<String> uploadProSign(@PathVariable(name = "contractId") Long contractId, @RequestPart MultipartFile file) {
		contractPhotoService.uploadProSign(contractId,file);
		return ApiResponse.onSuccess("전문가 전자서명 upload");
	}

}
