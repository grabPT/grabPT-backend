package com.grabpt.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	private final UserDetailsService userDetailsService;
	private final AuthService authService;

	@Operation(summary = "User 회원가입 요청 (Multipart)",
		description = "JSON 데이터와 프로필 이미지를 동시에 전송하는 회원가입")
	@PostMapping(value = "/user-signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public void userSignup(
		@RequestPart("data") SignupRequest.UserSignupRequestDto signupRequest,
		@RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
		HttpServletResponse response) {

		authService.registerUser_photo(signupRequest, profileImage, response);

		// 일반 회원은 메인 페이지로 redirect
		response.setStatus(HttpServletResponse.SC_SEE_OTHER); // 303
		response.setHeader("Location", "https://www.grabpt.com/");
	}

	@Operation(summary = "Pro 회원가입 요청 (Multipart)",
		description = "JSON 데이터와 프로필 이미지를 동시에 전송하는 회원가입")
	@PostMapping(value = "/pro-signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public void proSignup(
		@RequestPart("data") SignupRequest.ProSignupRequestDto signupRequest,
		@RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
		HttpServletResponse response) {

		authService.registerPro_photo(signupRequest, profileImage, response);

		// 전문가 회원은 /expert로 redirect
		response.setStatus(HttpServletResponse.SC_SEE_OTHER); // 303
		response.setHeader("Location", "https://www.grabpt.com/expert");
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
		log.info("재발급된 accessToken = " + newAccessToken);
		log.info("재발급된 refreshToken = " + newRefreshToken);

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

	@GetMapping("/api/temp-info")
	@Operation(
		summary = "소셜 로그인 시 임시 정보 조회",
		description = "소셜 로그인 시 기존 쿠키 방식이 아닌 세션 방식으로 반환"
	)
	public ApiResponse<Map<String, String>> getTempInfo(HttpSession session) {
		Map<String, String> data = new HashMap<>();
		data.put("email", (String)session.getAttribute("tempEmail"));
		data.put("username", (String)session.getAttribute("tempName"));
		data.put("oauthProvider", (String)session.getAttribute("tempOauthProvider"));
		data.put("oauthId", (String)session.getAttribute("tempOauthId"));

		log.info("임시 회원가입 데이터 반환: {}", data);

		return ApiResponse.onSuccess(data);
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
