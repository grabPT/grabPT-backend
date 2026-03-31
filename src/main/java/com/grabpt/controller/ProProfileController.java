package com.grabpt.controller;

import com.grabpt.domain.enums.SortType;
import com.grabpt.dto.request.ProSearchRequest;
import com.grabpt.dto.response.ProSearchResponse;
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

	@Operation(
		summary = "트레이너 검색 API",
		description = "키워드, 카테고리, 지역, 가격, 평점 조건으로 트레이너를 검색합니다. " +
			"sortBy: RATING(기본) / PRICE_ASC / PRICE_DESC / REVIEW_COUNT"
	)
	@GetMapping("/pro/search")
	public ApiResponse<Page<ProSearchResponse>> searchProfiles(
		@RequestParam(required = false) String keyword,
		@RequestParam(required = false) String categoryCode,
		@RequestParam(required = false) String city,
		@RequestParam(required = false) String district,
		@RequestParam(required = false) Integer minPrice,
		@RequestParam(required = false) Integer maxPrice,
		@RequestParam(required = false) Double minRating,
		@RequestParam(required = false, defaultValue = "RATING") SortType sortBy,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "20") int size) {

		ProSearchRequest request = new ProSearchRequest();
		request.setKeyword(keyword);
		request.setCategoryCode(categoryCode);
		request.setCity(city);
		request.setDistrict(district);
		request.setMinPrice(minPrice);
		request.setMaxPrice(maxPrice);
		request.setMinRating(minRating);
		request.setSortBy(sortBy);

		Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
		return ApiResponse.onSuccess(profileFacade.searchProfiles(request, pageable));
	}

}


