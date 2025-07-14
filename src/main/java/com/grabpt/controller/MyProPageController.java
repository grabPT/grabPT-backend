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
@RequestMapping("/mypage/pro")
@RequiredArgsConstructor
public class MyProPageController {

	private final ProfileService profileService;

	@GetMapping("/reviews")
	public ApiResponse<Page<MyReviewListDTO>> getProReviews(
		@RequestParam(name = "userId") Long userId,
		Pageable pageable) {

		Page<MyReviewListDTO> reviews = profileService.findProReviews(userId, pageable);
		return ApiResponse.onSuccess(reviews);
	}
}

