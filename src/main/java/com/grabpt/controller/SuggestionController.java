package com.grabpt.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.domain.entity.Suggestions;
import com.grabpt.dto.request.SuggestionRequestDto;
import com.grabpt.dto.response.SuggestionResponseDto;
import com.grabpt.dto.response.UserResponseDto;
import com.grabpt.service.SuggestionService.SuggestionService;
import com.grabpt.service.UserService.UserQueryService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/suggestion")
@RequiredArgsConstructor
@Slf4j
public class SuggestionController {

	private final SuggestionService suggestionService;
	private final UserQueryService userQueryService;

	@Operation(
		summary = "제안서 저장 API",
		description = "트레이너가 보낸 제안서를 저장합니다."
	)
	@PostMapping
	public ApiResponse<String> setSuggestion(@RequestBody SuggestionRequestDto dto,
		HttpServletRequest request) throws IllegalAccessException {

		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();  // 현재 로그인한 트레이너 이메일

		Suggestions saved = suggestionService.save(dto, email);

		return ApiResponse.onSuccess(saved.getId().toString() + " 저장 완료");
	}

	@GetMapping("/{suggestionId}")
	@Operation(
		summary = "제안서 상세 조회 API",
		description = "제안서 정보와 트레이너 프로필 정보를 조회합니다."
	)
	public ApiResponse<SuggestionResponseDto.SuggestionDetailResponseDto> getSuggestionDetail(
		@PathVariable Long suggestionId
	) {
		SuggestionResponseDto.SuggestionDetailResponseDto response = suggestionService.getDetail(suggestionId);
		return ApiResponse.onSuccess(response);
	}

}
