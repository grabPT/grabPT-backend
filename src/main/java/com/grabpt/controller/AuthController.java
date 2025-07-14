package com.grabpt.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.AuthHandler;
import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.request.RefreshTokenRequestDto;
import com.grabpt.dto.request.SignupRequest;
import com.grabpt.repository.UserRepository.UserRepository;
import com.grabpt.service.AuthService.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	private final UserDetailsService userDetailsService;
	private final AuthService authService;

	@Operation(
		summary = "User 회원가입 요청",
		description = "회원가입에 필요한 정보 전달 시 DB 저장 및 JWT 토큰 생성 후 반환, access, refresh 토큰은 쿠키로 전달"
	)
	// 최종 회원가입
	@PostMapping("/user-signup")
	public ApiResponse<String> user_signup(@RequestBody SignupRequest.UserSignupRequestDto signupRequest,
		HttpServletResponse response) {
		authService.registerUser(signupRequest, response);

		return ApiResponse.onSuccess("User 토큰 저장 완료");
	}

	@Operation(
		summary = "Pro 회원가입 요청",
		description = "회원가입에 필요한 정보 전달 시 DB 저장 및 JWT 토큰 생성 후 반환, access, refresh 토큰은 쿠키로 전달"
	)
	// 최종 회원가입
	@PostMapping("/pro-signup")
	public ApiResponse<String> pro_signup(@RequestBody SignupRequest.ProSignupRequestDto signupRequest,
		HttpServletResponse response) {
		authService.registerPro(signupRequest, response);

		return ApiResponse.onSuccess("Pro 토큰 저장 완료");
	}

	// JWT 토큰 재발행
	@Operation(
		summary = "JWT Refresh Token으로 인증 토큰 재발행",
		description = "유효한 Refresh Token 전달 시 인증 토큰 재발행, access, refresh 토큰은 쿠키로 전달"
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "리프레시 토큰을 담은 요청 body",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = RefreshTokenRequestDto.class)
		)
	)
	@PostMapping("/reissue")
	public ApiResponse<String> reissueToken(@RequestBody RefreshTokenRequestDto request,
		HttpServletResponse response) {
		String refreshToken = request.getRefreshToken();

		// 검증 단계
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			throw new AuthHandler(ErrorStatus.INVALID_JWT_ISSUE);
		}

		String email = jwtTokenProvider.getUserEmail(refreshToken);
		Users user = userRepository.findByEmail(email).orElseThrow();

		if (!refreshToken.equals(user.getRefreshToken())) {
			throw new AuthHandler(ErrorStatus.INVALID_JWT_ISSUE_REFRESH);
		}

		// 재발급
		UserDetails userDetails = userDetailsService.loadUserByUsername(email);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userDetails, null, userDetails.getAuthorities());
		String newAccessToken = jwtTokenProvider.generateToken(authentication);
		String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

		user.setRefreshToken(newRefreshToken);
		userRepository.save(user);

		// 쿠키로 저장
		addTokenCookies(newAccessToken, newRefreshToken, response);

		return ApiResponse.onSuccess("토큰 저장 완료");
	}

	@Operation(
		summary = "로그아웃",
		description = "accessToken 및 refreshToken 쿠키 삭제, DB refreshToken 초기화"
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "리프레시 토큰을 담은 요청 body",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = RefreshTokenRequestDto.class)
		)
	)
	@PostMapping("/logout")
	public ApiResponse<String> logout(@RequestBody(required = false) RefreshTokenRequestDto request,
		HttpServletResponse response) {
		// 쿠키 삭제 (클라이언트 브라우저에서 삭제됨)
		deleteTokenCookies(response);

		// optional: DB의 refreshToken도 삭제
		if (request != null && request.getRefreshToken() != null) {
			String email = jwtTokenProvider.getUserEmail(request.getRefreshToken());
			Users user = userRepository.findByEmail(email).orElse(null);
			if (user != null) {
				user.setRefreshToken(null);
				userRepository.save(user);
			}
		}

		log.info("로그아웃 완료");

		return ApiResponse.onSuccess("로그아웃 완료");
	}

	@GetMapping("/check-nickname")
	@Operation(
		summary = "nickname 중복 검증",
		description = "nickname 파라미터 전송 시 중복 여부 판단, 중복이면 true / 미중복이면 false"
	)
	@Parameters({
		@Parameter(name = "nickname", description = "중복 닉네임", required = true, example = "닉네임")
	})
	public ApiResponse<Boolean> checkNickname(@RequestParam String nickname) {
		boolean isDuplicate = userRepository.existsByNickname(nickname);
		return ApiResponse.onSuccess(isDuplicate);
	}

	private void addTokenCookies(String accessToken, String refreshToken, HttpServletResponse response) {
		Cookie accessCookie = new Cookie("accessToken", accessToken);
		accessCookie.setHttpOnly(true);
		accessCookie.setSecure(true);
		accessCookie.setPath("/");
		accessCookie.setMaxAge(60 * 30); // 30분

		Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
		refreshCookie.setHttpOnly(true);
		refreshCookie.setSecure(true);
		refreshCookie.setPath("/");
		refreshCookie.setMaxAge(60 * 60 * 24 * 7); // 7일

		response.addCookie(accessCookie);
		response.addCookie(refreshCookie);
	}

	private void deleteTokenCookies(HttpServletResponse response) {
		Cookie accessTokenCookie = new Cookie("accessToken", null);
		accessTokenCookie.setHttpOnly(true);
		accessTokenCookie.setSecure(true);
		accessTokenCookie.setPath("/");
		accessTokenCookie.setMaxAge(0); // 즉시 삭제

		Cookie refreshTokenCookie = new Cookie("refreshToken", null);
		refreshTokenCookie.setHttpOnly(true);
		refreshTokenCookie.setSecure(true);
		refreshTokenCookie.setPath("/");
		refreshTokenCookie.setMaxAge(0); // 즉시 삭제

		response.addCookie(accessTokenCookie);
		response.addCookie(refreshTokenCookie);
	}
}
