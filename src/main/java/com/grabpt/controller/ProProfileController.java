package com.grabpt.controller;

import com.grabpt.service.ProfileService.ProfileFacade;
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

	private final ProfileFacade  profileFacade;

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
		Page<ProProfileResponseDTO> proProfiles = profileFacade.findProProfilesByCategory(categoryCode, pageable);
		return ApiResponse.onSuccess(proProfiles);
	}

	@Operation(
		description = "특정 카테고리 전문가 프로필 출력",
		summary = "특정 카테고리 전문가 프로필 출력"
	)
	@GetMapping("category-proprofile/{userCode}")
	public ApiResponse<ProProfileResponseDTO> getProProfile(
		@PathVariable(name = "userCode") Long userId) {
		ProProfileResponseDTO proProfile = profileFacade.findProProfileByUser(userId);
		return ApiResponse.onSuccess(proProfile);
	}

}


