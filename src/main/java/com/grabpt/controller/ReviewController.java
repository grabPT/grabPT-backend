package com.grabpt.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.dto.request.ReviewRequestDTO;
import com.grabpt.service.ReviewService.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
	private final ReviewService reviewService;

	@Operation(summary = "리뷰 작성 API", description = "사용자가 전문가에 대한 리뷰를 작성합니다.")
	@PostMapping
	public ApiResponse<String> addReview(@AuthenticationPrincipal(expression = "user.id") Long userId
	,@RequestBody ReviewRequestDTO reviewRequestDTO) {
		reviewService.createReview(userId, reviewRequestDTO);
		return ApiResponse.onSuccess("리뷰가 성공적으로 등록되었습니다.");
	}
}
