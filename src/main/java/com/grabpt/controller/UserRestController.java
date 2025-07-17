package com.grabpt.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.dto.response.UserResponseDto;
import com.grabpt.service.UserService.UserQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserRestController {

	private final UserQueryService userQueryService;

	@GetMapping("/info")
	@Operation(summary = "유저 내 정보 조회 API - 인증 필요",
		description = "유저가 내 정보를 조회하는 API입니다.",
		security = {@SecurityRequirement(name = "JWT TOKEN")}
	)
	public ApiResponse<UserResponseDto.UserInfoDTO> getMyInfo(HttpServletRequest request) throws
		IllegalAccessException {
		return ApiResponse.onSuccess(userQueryService.getUserInfo(request));
	}

}
