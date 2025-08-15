package com.grabpt.controller;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.domain.entity.Matching;
import com.grabpt.dto.request.MatchingStatusUpdateRequest;
import com.grabpt.dto.response.ContractResponse;
import com.grabpt.service.MatchingService.MatchingService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matching")
public class MatchingController {

	private final MatchingService matchingService;

	@Transactional
	@PostMapping
	@Operation(summary = "요청-제안 매칭 API", description = "요청서와 제안서 기반으로 매칭 생성(matchingId, contractId 반환)")
	public ApiResponse<ContractResponse.CreateMatchingAndContractResponseDto> createMatching(
		@RequestParam Long requestionId, @RequestParam Long suggestionId) {
		return ApiResponse.onSuccess(matchingService.createMatching(requestionId, suggestionId));
	}

	@Transactional
	@PostMapping("/{matchingId}/status")
	@Operation(summary = "매칭 상태 변경 API", description = "매칭 상태를 CANCELLED 또는 COMPLETED로 변경")
	public ApiResponse<String> updateMatchingStatus(
		@PathVariable Long matchingId,
		@RequestBody MatchingStatusUpdateRequest request
	) {
		Matching updated = matchingService.updateStatus(matchingId, request.getStatus());
		return ApiResponse.onSuccess("매칭 상태 변경 완료: " + updated.getStatus());
	}
}
