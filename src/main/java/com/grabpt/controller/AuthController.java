package com.grabpt.controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.config.jwt.properties.CookieSupport;
import com.grabpt.config.oauth.CookieUtils;
import com.grabpt.config.oauth.DynamicCookieSupport;
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
import jakarta.servlet.http.HttpServletRequest;
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
	public ApiResponse<String> userSignup(
		@RequestPart("data") SignupRequest.UserSignupRequestDto signupRequest,
		@RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
		HttpServletResponse response) {

		authService.registerUser_photo(signupRequest, profileImage, response);

		// 일반 회원은 메인 페이지로 redirect
		// response.setStatus(HttpServletResponse.SC_SEE_OTHER); // 303
		// response.setHeader("Location", "https://www.grabpt.com/authcallback");

		return ApiResponse.onSuccess("user 회원가입 성공");
	}

	@Operation(summary = "Pro 회원가입 요청 (Multipart)",
		description = "JSON 데이터와 프로필 이미지를 동시에 전송하는 회원가입")
	@PostMapping(value = "/pro-signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<String> proSignup(
		@RequestPart("data") SignupRequest.ProSignupRequestDto signupRequest,
		@RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
		HttpServletResponse response) {

		authService.registerPro_photo(signupRequest, profileImage, response);

		// 전문가 회원은 /expert로 redirect
		// response.setStatus(HttpServletResponse.SC_SEE_OTHER); // 303
		// response.setHeader("Location", "https://www.grabpt.com/authcallback");

		return ApiResponse.onSuccess("pro 회원가입 성공");
	}

	// JWT 토큰 재발행
	@Operation(summary = "JWT Refresh Token으로 인증 토큰 재발행",
		description = "유효한 Refresh Token 전달 시 인증 토큰 재발행, access, refresh 토큰은 쿠키로 전달")
	@PostMapping("/reissue")
	public ResponseEntity<Void> reissueToken(HttpServletRequest request, HttpServletResponse response) {

		log.info("reissue 진입");

		// 1) 쿠키에서 refresh 읽기 (신/구 이름 모두 허용)
		String refreshToken = findCookie(request, "REFRESH_TOKEN", "refreshToken");
		if (refreshToken == null || refreshToken.isBlank()) {
			response.setHeader("X-Reason", "missing-cookie");
			log.warn("[REISSUE] missing refresh cookie");
			return unauthorizedAndClear(response);
		}

		// 2) 유효성 검사
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			response.setHeader("X-Reason", "invalid-or-expired");
			try {
				jwtTokenProvider.getUserEmail(refreshToken);
			} catch (io.jsonwebtoken.ExpiredJwtException e) {
				response.setHeader("X-Reason", "expired");
			}
			log.warn("[REISSUE] invalid/expired refresh");
			return unauthorizedAndClear(response);
		}

		String email = jwtTokenProvider.getUserEmail(refreshToken);
		Users user = userRepository.findByEmail(email).orElse(null);
		if (user == null) {
			response.setHeader("X-Reason", "user-not-found");
			log.warn("[REISSUE] user not found: {}", email);
			return unauthorizedAndClear(response);
		}

		String stored = user.getRefreshToken();
		if (stored == null) {
			response.setHeader("X-Reason", "stored-null");
			log.warn("[REISSUE] stored refresh is null for {}", email);
			return unauthorizedAndClear(response);
		}
		if (!refreshToken.equals(stored)) {
			response.setHeader("X-Reason", "mismatch");
			log.warn("[REISSUE] refresh mismatch for {} (cookie len {} vs stored len {})",
				email, refreshToken.length(), stored.length());
			return unauthorizedAndClear(response);
		}

		// 3) 재발급(회전)
		var userDetails = userDetailsService.loadUserByUsername(email);
		var authentication = new UsernamePasswordAuthenticationToken(
			userDetails, null, userDetails.getAuthorities());

		String newAccessToken = jwtTokenProvider.generateToken(authentication);
		String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

		user.setRefreshToken(newRefreshToken);
		userRepository.save(user);

		// 4) 쿠키 재설정 (동적 속성)
		var access = DynamicCookieSupport.newCookie("ACCESS_TOKEN", newAccessToken, request)
			.maxAge(Duration.ofHours(4)).build();
		var refresh = DynamicCookieSupport.newCookie("REFRESH_TOKEN", newRefreshToken, request)
			.maxAge(Duration.ofDays(30)).build();

		response.addHeader(HttpHeaders.SET_COOKIE, access.toString());
		response.addHeader(HttpHeaders.SET_COOKIE, refresh.toString());

		return ResponseEntity.noContent().build();
	}

	private ResponseEntity<Void> unauthorizedAndClear(HttpServletResponse res) {
		CookieUtils.deleteCookie(res, "ACCESS_TOKEN");
		CookieUtils.deleteCookie(res, "REFRESH_TOKEN");
		CookieUtils.deleteCookie(res, "accessToken"); // 구버전 호환
		CookieUtils.deleteCookie(res, "refreshToken");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

	private static String findCookie(HttpServletRequest request, String... names) {
		var cs = request.getCookies();
		if (cs == null)
			return null;
		for (String n : names) {
			for (var c : cs)
				if (n.equals(c.getName()))
					return c.getValue();
		}
		return null;
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
		HttpServletRequest req,
		HttpServletResponse res,
		Authentication authentication) {

		log.info("쿠키 삭제 진입");

		// 1) 쿠키 삭제 세트
		for (var c : CookieSupport.logoutDeletionSet()) {
			res.addHeader("Set-Cookie", c.toString());
			log.info("[LOGOUT] Set-Cookie -> {}", c.toString());
		}

		// 2) refresh 토큰 확보: 바디 > 쿠키
		String refresh = null;
		if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
			refresh = request.getRefreshToken();
		} else if (req.getCookies() != null) {
			for (var c : req.getCookies()) {
				if ("refreshToken".equals(c.getName())) {
					refresh = c.getValue();
					break;
				}
			}
		}

		// 3) DB refreshToken 무효화 (가능한 모든 루트로 시도)
		boolean revoked = false;

		// 3-1) refresh 토큰이 있고 검증되면 이메일로 무효화
		if (refresh != null && !refresh.isBlank() && jwtTokenProvider.validateToken(refresh)) {
			String email = jwtTokenProvider.getUserEmail(refresh);
			userRepository.findByEmail(email).ifPresent(u -> {
				u.setRefreshToken(null);
				userRepository.save(u);
				log.info("[LOGOUT] refreshToken removed for user: {}", email);
			});
			revoked = true;
		}

		// 3-2) (보조) 인증 정보가 있다면 그 유저의 refreshToken 도 제거
		if (!revoked && authentication != null &&
			authentication.getPrincipal() instanceof com.grabpt.config.auth.PrincipalDetails pd) {
			Users u = pd.getUser();
			u.setRefreshToken(null);
			userRepository.save(u);
		}

		// 4) 세션 & 시큐리티 컨텍스트 정리
		var session = req.getSession(false);
		if (session != null)
			session.invalidate();
		org.springframework.security.core.context.SecurityContextHolder.clearContext();
		log.info("[LOGOUT] Logout process completed");

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
		response.addHeader("Set-Cookie", CookieSupport.accessCookie(accessToken).toString());
		response.addHeader("Set-Cookie", CookieSupport.refreshCookie(refreshToken).toString());
	}

	private void deleteTokenCookies(HttpServletResponse response) {
		response.addHeader("Set-Cookie", CookieSupport.deleteCookie("accessToken", "/").toString());
		response.addHeader("Set-Cookie", CookieSupport.deleteCookie("refreshToken", "/").toString());
		response.addHeader("Set-Cookie", CookieSupport.deleteCookie("refreshToken", "/api/auth/reissue").toString());
	}
}
