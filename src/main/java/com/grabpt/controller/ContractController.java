package com.grabpt.controller;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.converter.ContractConverter;
import com.grabpt.domain.entity.Contract;
import com.grabpt.domain.entity.ContractInfo;
import com.grabpt.dto.request.ContractRequest;
import com.grabpt.dto.response.ContractResponse;
import com.grabpt.repository.ContractRepository;
import com.grabpt.service.ContractService.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ContractController {

	private final ContractService contractService;

	@Operation(
		description = "계약서 Id를 통해 계약서에 대한 정보를 조회합니다",
		summary = "계약서 정보 조회 API"
	)
	@GetMapping("/contract/{contractId}")
	public ApiResponse<ContractResponse.ContractResponseDto> getContract(@PathVariable(name = "contractId") Long id){
		Contract contract = contractService.findById(id);
		return ApiResponse.onSuccess(ContractConverter.toContractResponseDto(contract));
	}

	@Operation(
		description = "수강생이 계약서 정보를 입력한 뒤 제출하며 Matching status가 USERWROTE로 업데이트 됩니다",
		summary = "수강생 계약서 작성 API"
	)
	@PostMapping("/contract/{contractId}/user")
	public ApiResponse<Long> writeUserInfo(@RequestBody ContractRequest.ContractInfoDto request,
										   @PathVariable(name = "contractId") Long id){
		Contract contract = contractService.writeUserInfo(id, request);
		return ApiResponse.onSuccess(contract.getId());
	}

	@Operation(
		description = "트레이너가 계약서 정보를 입력한 뒤 제출하며 Matching status가 COMPLETE로 업데이트 됩니다",
		summary = "트레이너 계약서 작성 API"
	)
	@PostMapping("/contract/{contractId}/pro")
	public ApiResponse<Long> writeProInfo(@RequestBody ContractRequest.ContractInfoDto request,
										   @PathVariable(name = "contractId") Long id){
		Contract contract = contractService.writeProInfo(id, request);
		return ApiResponse.onSuccess(contract.getId());
	}

	@Operation(description = "계약서 제출 및 저장")
	@PostMapping("/contract/{contractId}/submit")
	public void submitContract(){
		//계약일 setting 및 pdf로 변환해서 s3저장
	}
}
