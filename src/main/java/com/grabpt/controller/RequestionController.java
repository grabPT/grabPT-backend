package com.grabpt.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping("/requestion")
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
	public ApiResponse<String> setRequestion(@RequestBody RequestionRequestDto dto,
		HttpServletRequest request) throws IllegalAccessException {

		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();
		Requestions saved = requestionService.save(dto, email);

		return ApiResponse.onSuccess(saved.getId().toString() + "저장 완료");
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
}
