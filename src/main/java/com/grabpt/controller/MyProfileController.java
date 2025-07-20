package com.grabpt.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.dto.request.UserProfileUpdateRequestDTO;
import com.grabpt.dto.response.MyRequestListDTO;
import com.grabpt.dto.response.MyReviewListDTO;
import com.grabpt.dto.response.ProfileResponseDTO;
import com.grabpt.service.ProfileService.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyProfileController {

	private final ProfileService profileService;

	// @AuthenticationPrincipal Long userId
	@GetMapping
	public ApiResponse<ProfileResponseDTO.MyProfileDTO> getMyUserProfile(@RequestParam(name = "userId") Long userId) {
		return ApiResponse.onSuccess(profileService.findMyUserProfile(userId));
	}

	@PatchMapping
	public ApiResponse<ProfileResponseDTO.MyProfileDTO> updateMyUserProfile(
		@RequestParam(name = "userId") Long userId,
		@Valid @RequestBody UserProfileUpdateRequestDTO request) {

		profileService.updateMyUserProfile(userId, request);
		return ApiResponse.onSuccess(profileService.findMyUserProfile(userId));
	}

	@GetMapping("/reviews")
	public ApiResponse<Page<MyReviewListDTO>> getMyReviewList(
		@RequestParam(name = "userId") Long userId, Pageable pageable) {

		return ApiResponse.onSuccess(profileService.findMyReviews(userId, pageable));
	}

	@GetMapping("/requests")
	public ApiResponse<Page<MyRequestListDTO>> getMyRequestList(
		@RequestParam(name = "userId") Long userId,
		Pageable pageable) {

		return ApiResponse.onSuccess(profileService.findMyRequests(userId, pageable));
	}

	@PatchMapping(value = "/image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ApiResponse<String> updateUserProfileImage(
		@RequestParam(name = "userId") Long userId,
		@RequestPart(value = "image") MultipartFile profileImage) {

		profileService.updateUserProfileImage(userId, profileImage);

		return ApiResponse.onSuccess("프로필 이미지가 성공적으로 수정되었습니다.");
	}


}
