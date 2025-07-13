package com.grabpt.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.dto.response.ProProfileResponseDTO;
import com.grabpt.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProProfileController {

	private final ProfileService profileService;

	@GetMapping("/{categoryCode}")
	public ApiResponse<Page<ProProfileResponseDTO>> getProProfilesByCategory(
		@PathVariable String categoryCode,
		Pageable pageable) {
		Page<ProProfileResponseDTO> proProfiles = profileService.findProProfilesByCategory(categoryCode, pageable);
		return ApiResponse.onSuccess(proProfiles);
	}

	@GetMapping("/{categoryCode}/{userCode}")
	public ApiResponse<ProProfileResponseDTO> getProProfile(@PathVariable String categoryCode,
		@PathVariable(name = "userCode") Long userId) {
		ProProfileResponseDTO proProfile = profileService.findProProfile(userId);
		return ApiResponse.onSuccess(proProfile);
	}
}
