package com.grabpt.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserRestController {

	private final UserQueryService userQueryService;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	@Operation(
		summary = "유저 내 정보 조회 API - 인증 필요",
		description = "유저가 내 정보를 조회하는 API입니다.",
		security = {@SecurityRequirement(name = "JWT TOKEN")}
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200", description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = UserResponseDto.UserInfoDTO.class),
				examples = @ExampleObject(name = "성공 예시", value = """
					{
					  "isSuccess": true,
					  "code": "OK",
					  "message": "요청에 성공했습니다.",
					  "result": {
					    "userId": 10,
					    "userNickname": "동이",
					    "userName": "홍길동",
					    "address": {
					      "city": "서울시",
					      "district": "관악구",
					      "street": "봉천동",
					      "zipcode": "12345",
					      "streetCode": "상암로 123",
					      "specAddress": "123동 456호"
					    },
					    "email": "email@gmail.com",
					    "role": "USER"
					  }
					}
					""")
			)
		)
	})
	@GetMapping("/info")
	public ApiResponse<UserResponseDto.UserInfoDTO> getMyInfo(HttpServletRequest request) throws
		IllegalAccessException {
		return ApiResponse.onSuccess(userQueryService.getUserInfo(request));
	}

	@Operation(summary = "테스트용 AccessToken 발급", description = "특정 유저 ID를 기반으로 AccessToken을 발급합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200", description = "발급 성공",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(name = "성공 예시", value = """
					{
					  "isSuccess": true,
					  "code": "OK",
					  "message": "요청에 성공했습니다.",
					  "result": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
					}
					""")
			)
		)
	})
	@GetMapping("/{userId}")
	public ApiResponse<String> getTestAccessToken(@PathVariable Long userId) {
		Users user = userRepository.findById(userId)
			.orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		String token = jwtTokenProvider.generateToken(user); // 기존 generateToken(Users user) 사용
		return ApiResponse.onSuccess(token);
	}

	@Operation(
		summary = "휴대폰 번호 사용 가능 여부 확인",
		description = "phoneNumber 파라미터를 받아 해당 번호로 가입된 계정이 없으면 true(사용 가능), 있으면 false(이미 존재)를 반환합니다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200", description = "검증 성공",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(name = "성공 예시", value = """
					{
					  "isSuccess": true,
					  "code": "OK",
					  "message": "요청에 성공했습니다.",
					  "result": true
					}
					""")
			)
		)
	})
	@GetMapping("/check-phone")
	public ApiResponse<Boolean> checkPhone(
		@Parameter(description = "확인할 휴대폰 번호", schema = @Schema(type = "string", example = "01012345678"))
		@RequestParam String phoneNumber
	) {
		// 숫자만 남기도록 정규화
		String normalized = phoneNumber.replaceAll("[^0-9]", "");

		// DB에 존재 여부 확인
		boolean exists = userRepository.existsActiveByPhone(normalized);

		// true → 사용 가능, false → 이미 존재
		boolean available = !exists;

		return ApiResponse.onSuccess(available);
	}
}
