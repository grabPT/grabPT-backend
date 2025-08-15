package com.grabpt.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.dto.request.ReviewRequestDTO;
import com.grabpt.dto.response.UserResponseDto;
import com.grabpt.service.ReviewService.ReviewService;
import com.grabpt.service.UserService.UserQueryService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
	private final ReviewService reviewService;
	private final UserQueryService userQueryService;

	@Operation(summary = "리뷰 작성 API", description = "사용자가 전문가에 대한 리뷰를 작성합니다.")
	@PostMapping
	public ApiResponse<String> addReview(HttpServletRequest request
		, @RequestBody ReviewRequestDTO reviewRequestDTO) throws IllegalAccessException {
		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		Long userId = userInfo.getUserId();
		reviewService.createReview(userId, reviewRequestDTO);
		return ApiResponse.onSuccess("리뷰가 성공적으로 등록되었습니다.");
	}
}
