package com.grabpt.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.dto.request.ProProfileUpdateRequestDTO;
import com.grabpt.dto.request.UserProfileUpdateRequestDTO;
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

	@GetMapping("/pro")
	public ApiResponse<ProfileResponseDTO.MyProProfileDTO> getMyProUserProfile(
		@RequestParam(name = "userId") Long userId) {
		return ApiResponse.onSuccess(profileService.findMyProUserProfile(userId));
	}

	@PatchMapping
	public ApiResponse<ProfileResponseDTO.MyProfileDTO> updateMyUserProfile(
		@RequestParam(name = "userId") Long userId,
		@Valid @RequestBody UserProfileUpdateRequestDTO request) {

		profileService.updateMyUserProfile(userId, request);
		return ApiResponse.onSuccess(profileService.findMyUserProfile(userId));
	}

	@PatchMapping("/pro")
	public ApiResponse<ProfileResponseDTO.MyProProfileDTO> updateMyProUserProfile(
		@RequestParam(name = "userId") Long userId,
		@Valid @RequestBody ProProfileUpdateRequestDTO request) {

		profileService.updateMyProUserProfile(userId, request);
		return ApiResponse.onSuccess(profileService.findMyProUserProfile(userId));
	}
}
