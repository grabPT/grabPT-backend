package com.grabpt.controller;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matching")
@Validated
@Tag(name = "Matching", description = "요청/제안 매칭 및 상태 관리 API")
public class MatchingController {

	private final MatchingService matchingService;

	@Transactional
	@PostMapping
	@Operation(
		summary = "요청-제안 매칭 생성",
		description = """
			## 사용 목적 및 기능 요약
			- 요청서(`requestionId`)와 제안서(`suggestionId`)를 기반으로 매칭을 생성합니다.
			- 생성된 `matchingId`, `contractId`를 반환합니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공",
			content = @Content(mediaType = "application/json",
				schema = @Schema(implementation = ContractResponse.CreateMatchingAndContractResponseDto.class),
				examples = @ExampleObject(name = "성공 예시", value = """
					{
					  "isSuccess": true,
					  "code": "OK",
					  "message": "요청에 성공했습니다.",
					  "result": {
					    "matchingId": 101,
					    "contractId": 555
					  }
					}
					""")
			)
		)
	})
	public ApiResponse<ContractResponse.CreateMatchingAndContractResponseDto> createMatching(
		@Parameter(
			description = "요청서 ID",
			required = true,
			schema = @Schema(type = "integer", example = "10")
		)
		@RequestParam Long requestionId,

		@Parameter(
			description = "제안서 ID",
			required = true,
			schema = @Schema(type = "integer", example = "77")
		)
		@RequestParam Long suggestionId
	) {
		return ApiResponse.onSuccess(matchingService.createMatching(requestionId, suggestionId));
	}

	@Transactional
	@PostMapping("/{matchingId}/status")
	@Operation(
		summary = "매칭 상태 변경",
		description = """
			## 사용 목적 및 기능 요약
			- 매칭 상태를 변경합니다. (예: `CANCELLED`, `COMPLETED`)
			- 상태 변경 결과를 문자열로 반환합니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 변경 성공",
			content = @Content(mediaType = "application/json",
				examples = @ExampleObject(name = "성공 예시", value = """
					{
					  "isSuccess": true,
					  "code": "OK",
					  "message": "요청에 성공했습니다.",
					  "result": "매칭 상태 변경 완료: COMPLETED"
					}
					""")
			)
		)
	})
	public ApiResponse<String> updateMatchingStatus(
		@Parameter(
			description = "매칭 ID",
			required = true,
			schema = @Schema(type = "integer", example = "101")
		)
		@PathVariable Long matchingId,
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			required = true,
			description = "status(enum): CANCELLED | COMPLETED",
			content = @Content(
				schema = @Schema(implementation = MatchingStatusUpdateRequest.class),
				examples = @ExampleObject(value = "{ \"status\": \"COMPLETED\" }")
			)
		)
		@RequestBody MatchingStatusUpdateRequest request
	) {
		Matching updated = matchingService.updateStatus(matchingId, request.getStatus());
		return ApiResponse.onSuccess("매칭 상태 변경 완료: " + updated.getStatus());
	}
}
