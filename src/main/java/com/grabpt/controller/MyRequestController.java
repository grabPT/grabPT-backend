package com.grabpt.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.dto.response.MyRequestListDTO;
import com.grabpt.service.ProfileService.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mypage/requests")
@RequiredArgsConstructor
public class MyRequestController {

	private final ProfileService profileService;

	@GetMapping
	public ApiResponse<Page<MyRequestListDTO>> getMyRequestList(
		@RequestParam(name = "userId") Long userId,
		Pageable pageable) {

		return ApiResponse.onSuccess(profileService.findMyRequests(userId, pageable));
	}
}

