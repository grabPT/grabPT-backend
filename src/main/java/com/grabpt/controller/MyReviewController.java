package com.grabpt.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.dto.response.MyReviewListDTO;
import com.grabpt.service.ProfileService.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mypage/reviews")
@RequiredArgsConstructor
public class MyReviewController {

	private final ProfileService profileService;

	@GetMapping
	public ApiResponse<Page<MyReviewListDTO>> getMyReviewList(
		@RequestParam(name = "userId") Long userId,
		Pageable pageable) {

		return ApiResponse.onSuccess(profileService.findMyReviews(userId, pageable));
	}
}

