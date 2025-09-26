package com.grabpt.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.dto.request.RequestionRequestDto;
import com.grabpt.dto.response.RequestionResponseDto;
import com.grabpt.dto.response.UserResponseDto;
import com.grabpt.service.RequestionService.RequestionService;
import com.grabpt.service.UserService.UserQueryService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/requestion")
@RequiredArgsConstructor
@Slf4j
public class RequestionController {

	private final RequestionService requestionService;
	private final UserQueryService userQueryService;

	@Operation(
		summary = "요청서 작성 저장 API",
		description = "작성한 요청서를 저장"
	)
	@PostMapping
	public ApiResponse<RequestionResponseDto.RequestionSaveResponseDto> setRequestion(
		@RequestBody RequestionRequestDto dto,
		HttpServletRequest request) throws IllegalAccessException {

		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();
		Requestions saved = requestionService.save(dto, email);

		return ApiResponse.onSuccess(
			RequestionResponseDto.RequestionSaveResponseDto.builder()
				.requestRequestionId(saved.getId())
				.build()
		);
	}

	@GetMapping("/{requestionId}")
	@Operation(
		summary = "요청서 상세 조회 API",
		description = "요청서와 유저 정보를 함께 조회합니다."
	)
	public ApiResponse<RequestionResponseDto.RequestionDetailResponseDto> getRequestionDetail(
		@PathVariable Long requestionId
	) {
		RequestionResponseDto.RequestionDetailResponseDto response = requestionService.getDetail(requestionId);
		return ApiResponse.onSuccess(response);
	}

	@GetMapping("/nearby")
	@Operation(
		summary = "요청서 조회 api",
		description = "트레이너 기준 일반 유저의 요청서를 조회합니다."
	)
	public ApiResponse<Page<RequestionResponseDto.RequestionResponsePagingDto>> getRequestionsNearby(
		@RequestParam(defaultValue = "latest") String sortBy,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "4") int size,
		HttpServletRequest request
	) throws IllegalAccessException {
		Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
		Page<RequestionResponseDto.RequestionResponsePagingDto> response =
			requestionService.getNearbyRequestions(request, sortBy, pageable);
		return ApiResponse.onSuccess(response);
	}

	@PatchMapping("/{requestionId}")
	@Operation(
		summary = "요청서 수정 API",
		description = "사용자가 본인의 요청서를 수정합니다."
	)
	public ApiResponse<String> updateRequestion(
		@PathVariable Long requestionId,
		@RequestBody RequestionRequestDto dto,
		HttpServletRequest request
	) throws IllegalAccessException {
		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();

		requestionService.update(requestionId, dto, email);
		return ApiResponse.onSuccess("요청서가 성공적으로 수정되었습니다.");
	}

	@DeleteMapping("/{requestionId}")
	@Operation(
		summary = "요청서 삭제 API",
		description = "사용자가 본인의 요청서를 삭제합니다."
	)
	public ApiResponse<String> deleteRequestion(
		@PathVariable Long requestionId,
		HttpServletRequest request
	) throws IllegalAccessException {
		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();

		requestionService.delete(requestionId, email);
		return ApiResponse.onSuccess("요청서가 성공적으로 삭제되었습니다.");
	}

	// RequestionController.java
	@GetMapping("/my")
	@Operation(summary = "내 요청서 목록 조회 API", description = "회원이 본인이 작성한 요청서 목록을 조회합니다.")
	public ApiResponse<Page<RequestionResponseDto.UserOwnRequestionDto>> getMyRequestions(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "3") int size,
		HttpServletRequest request
	) throws IllegalAccessException {
		Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
		Page<RequestionResponseDto.UserOwnRequestionDto> response = requestionService.getRequestionsByUser(request,
			pageable);
		return ApiResponse.onSuccess(response);
	}

	@GetMapping("/{requestionId}/requestion-can-edit")
	@Operation(summary = "요청서 수정 가능 여부 확인", description = "현재 로그인한 사용자가 요청서를 작성했는지 확인합니다.")
	public ApiResponse<Map<String, Boolean>> canEditRequestion(
		@PathVariable Long requestionId,
		HttpServletRequest request
	) throws IllegalAccessException {
		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();

		boolean isEdit = requestionService.canEditRequestion(requestionId, email);

		Map<String, Boolean> response = new HashMap<>();
		response.put("isEdit", isEdit);
		return ApiResponse.onSuccess(response);
	}

}
