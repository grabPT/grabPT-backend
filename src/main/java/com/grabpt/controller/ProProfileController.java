package com.grabpt.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.dto.response.MyReviewListDTO;
import com.grabpt.dto.response.ProProfileResponseDTO;
import com.grabpt.service.ProfileService.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProProfileController {

	private final ProfileService profileService;

	@Operation(
		description = "해당 카테고리에 해당하는 전문가 출력",
		summary = "카테고리 전문가 출력"
	)
	@GetMapping("/{categoryCode}")
	public ApiResponse<Page<ProProfileResponseDTO>> getProProfilesByCategory(
		@PathVariable String categoryCode,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
		Page<ProProfileResponseDTO> proProfiles = profileService.findProProfilesByCategory(categoryCode, pageable);
		return ApiResponse.onSuccess(proProfiles);
	}

	@Operation(
		description = "특정 카테고리 전문가 프로필 출력",
		summary = "특정 카테고리 전문가 프로필 출력"
	)
	@GetMapping("category-proprofile/{userCode}")
	public ApiResponse<ProProfileResponseDTO> getProProfile(
		@PathVariable(name = "userCode") Long userId) {
		ProProfileResponseDTO proProfile = profileService.findProProfileByUser(userId);
		return ApiResponse.onSuccess(proProfile);
	}

	/**
	 * 특정 전문가의 리뷰 목록을 조회하는 API
	 * @param userId 전문가의 user ID
	 * @return 페이징 처리된 리뷰 목록
	 */
	@Operation(
		description = "해당 전문가의 리뷰 확인",
		summary = "해당 전문가의 리뷰 확인"
	)
	@GetMapping("/{userId}/reviews")
	public ApiResponse<Page<MyReviewListDTO>> getProReviews(
		@PathVariable(name = "userId") Long userId,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
		Page<MyReviewListDTO> reviews = profileService.findProReviews(userId, pageable);
		return ApiResponse.onSuccess(reviews);
	}
}


