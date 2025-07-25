package com.grabpt.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.response.UserResponseDto;
import com.grabpt.repository.UserRepository.UserRepository;
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
	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	@GetMapping("/info")
	@Operation(summary = "유저 내 정보 조회 API - 인증 필요",
		description = "유저가 내 정보를 조회하는 API입니다.",
		security = {@SecurityRequirement(name = "JWT TOKEN")}
	)
	public ApiResponse<UserResponseDto.UserInfoDTO> getMyInfo(HttpServletRequest request) throws
		IllegalAccessException {
		return ApiResponse.onSuccess(userQueryService.getUserInfo(request));
	}

	@GetMapping("/{userId}")
	@Operation(summary = "테스트용 AccessToken 발급", description = "특정 유저 ID를 기반으로 AccessToken을 발급합니다.")
	public ApiResponse<String> getTestAccessToken(@PathVariable Long userId) {
		Users user = userRepository.findById(userId)
			.orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		String token = jwtTokenProvider.generateToken(user); // 기존 generateToken(Users user) 사용
		return ApiResponse.onSuccess(token);
	}

}
