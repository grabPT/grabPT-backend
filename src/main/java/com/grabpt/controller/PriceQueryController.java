package com.grabpt.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.service.priceService.PriceQueryService;

import lombok.RequiredArgsConstructor;

@RestController("/api")
@RequiredArgsConstructor
public class PriceQueryController {

	private final PriceQueryService priceQueryService;

	/**
	 * 예) /price/avg-per-session?categoryName=PT&city=서울&district=강남구&street=역삼동
	 * "서울 강남구 역삼동%" 만 집계
	 */
	@GetMapping("/price/avg-per-session")
	public ApiResponse<PriceQueryService.IntAvgPriceResult> getAvgUnitPrice(
		@RequestParam String categoryName,
		@RequestParam String city,
		@RequestParam String district,
		@RequestParam String street
	) {
		var result = priceQueryService.getAvgUnitPrice(categoryName, city, district, street);
		return ApiResponse.onSuccess(result);
	}
}
