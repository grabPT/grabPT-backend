package com.grabpt.config.jwt.properties;

import static com.grabpt.config.jwt.properties.CookieConstants.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import com.grabpt.config.oauth.EnvironmentDetector;
import com.grabpt.config.oauth.EnvironmentDetector.EnvironmentProfile;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 통합 쿠키 관리 클래스 v2
 * - 환경별(로컬/개발/운영) 쿠키 설정 자동화
 * - EnvironmentDetector와 연동하여 일관된 환경 감지
 * - 한글 등 non-ASCII 문자 URL 인코딩 처리
 */
@Slf4j
public final class CookieManagerV2 {

	private CookieManagerV2() {
		throw new AssertionError("Cannot instantiate utility class");
	}

	// ==================== JWT 토큰 쿠키 ====================

	/**
	 * Access Token 쿠키 생성 (15분)
	 */
	public static void setAccessToken(HttpServletResponse response, HttpServletRequest request, String token) {
		EnvironmentProfile env = EnvironmentDetector.detectEnvironment(request);

		ResponseCookie cookie = createCookie(env, ACCESS_TOKEN, token)
			.httpOnly(true)
			.maxAge(Duration.ofMinutes(15))
			.path("/")
			.build();
		addCookie(response, cookie);
	}

	/**
	 * Refresh Token 쿠키 생성 (7일)
	 * 운영 환경에서는 /api/auth/reissue 경로에서만 전송되도록 제한
	 */
	public static void setRefreshToken(HttpServletResponse response, HttpServletRequest request, String token) {
		EnvironmentProfile env = EnvironmentDetector.detectEnvironment(request);

		// 운영 환경: 특정 경로만, 개발 환경: 모든 경로
		String path = EnvironmentDetector.isProduction(env) ? "/api/auth/reissue" : "/";

		ResponseCookie cookie = createCookie(env, REFRESH_TOKEN, token)
			.httpOnly(true)
			.maxAge(Duration.ofDays(7))
			.path(path)
			.build();
		addCookie(response, cookie);
	}

	/**
	 * Access Token 조회
	 */
	public static String getAccessToken(HttpServletRequest request) {
		return getCookieValue(request, ACCESS_TOKEN);
	}

	/**
	 * Refresh Token 조회
	 */
	public static String getRefreshToken(HttpServletRequest request) {
		return getCookieValue(request, REFRESH_TOKEN);
	}

	// ==================== 사용자 정보 쿠키 (공개) ====================

	/**
	 * Role 쿠키 설정 (프론트에서 읽을 수 있음)
	 */
	public static void setRole(HttpServletResponse response, HttpServletRequest request, String role) {
		EnvironmentProfile env = EnvironmentDetector.detectEnvironment(request);

		ResponseCookie cookie = createCookie(env, ROLE, role)
			.httpOnly(false)  // 프론트에서 읽음
			.maxAge(Duration.ofDays(30))
			.path("/")
			.build();
		addCookie(response, cookie);
	}

	/**
	 * User ID 쿠키 설정 (프론트에서 읽을 수 있음)
	 */
	public static void setUserId(HttpServletResponse response, HttpServletRequest request, String userId) {
		EnvironmentProfile env = EnvironmentDetector.detectEnvironment(request);

		ResponseCookie cookie = createCookie(env, USER_ID, userId)
			.httpOnly(false)
			.maxAge(Duration.ofDays(30))
			.path("/")
			.build();
		addCookie(response, cookie);
	}

	// ==================== OAuth 임시 정보 쿠키 ====================

	/**
	 * OAuth 회원가입용 임시 정보 설정 (5분)
	 *
	 * ⚠️ 환경별 동작 차이:
	 * - LOCAL/DEV: URL 파라미터로 전달되므로 쿠키 미설정
	 * - PROD: HttpOnly 쿠키로 안전하게 설정
	 */
	public static void setOAuthTempInfo(HttpServletResponse response, HttpServletRequest request,
		String email, String name, String oauthId, String oauthProvider) {
		EnvironmentProfile env = EnvironmentDetector.detectEnvironment(request);

		// 개발 환경: URL 파라미터 사용하므로 쿠키 불필요
		if (EnvironmentDetector.isDevelopment(env)) {
			log.debug("[Cookie] OAuth temp info skipped (dev environment uses URL params)");
			return;
		}

		// 운영 환경: HttpOnly 쿠키로 설정
		Duration ttl = Duration.ofMinutes(5);

		// URL 인코딩 처리 (한글 등 non-ASCII 문자 지원)
		try {
			addCookie(response, createCookie(env, OAUTH_EMAIL, urlEncode(email))
				.httpOnly(false)
				.maxAge(ttl).path("/").build());

			addCookie(response, createCookie(env, OAUTH_NAME, urlEncode(name))
				.httpOnly(false)
				.maxAge(ttl).path("/").build());

			addCookie(response, createCookie(env, OAUTH_ID, urlEncode(oauthId))
				.httpOnly(false)
				.maxAge(ttl).path("/").build());

			addCookie(response, createCookie(env, OAUTH_PROVIDER, urlEncode(oauthProvider))
				.httpOnly(false)
				.maxAge(ttl).path("/").build());

			log.debug("[Cookie] OAuth temp info set (URL encoded)");
		} catch (Exception e) {
			log.error("[Cookie] Failed to set OAuth temp info", e);
			throw new RuntimeException("Failed to encode OAuth temp info", e);
		}
	}

	/**
	 * OAuth 임시 정보 조회 (서버 사이드에서만)
	 * URL 디코딩 처리
	 */
	public static OAuthTempInfo getOAuthTempInfo(HttpServletRequest request) {
		try {
			return new OAuthTempInfo(
				urlDecode(getCookieValue(request, OAUTH_EMAIL)),
				urlDecode(getCookieValue(request, OAUTH_NAME)),
				urlDecode(getCookieValue(request, OAUTH_ID)),
				urlDecode(getCookieValue(request, OAUTH_PROVIDER))
			);
		} catch (Exception e) {
			log.error("[Cookie] Failed to decode OAuth temp info", e);
			return new OAuthTempInfo(null, null, null, null);
		}
	}

	public record OAuthTempInfo(String email, String name, String oauthId, String provider) {
		public boolean isValid() {
			return email != null && oauthId != null && provider != null;
		}
	}

	// ==================== 쿠키 삭제 ====================

	/**
	 * 로그아웃 시 모든 인증 쿠키 삭제
	 */
	public static void clearAuthCookies(HttpServletResponse response, HttpServletRequest request) {
		deleteCookie(response, request, ACCESS_TOKEN, "/");
		deleteCookie(response, request, REFRESH_TOKEN, "/");
		deleteCookie(response, request, REFRESH_TOKEN, "/api/auth/reissue");
		deleteCookie(response, request, ROLE, "/");
		deleteCookie(response, request, USER_ID, "/");
	}

	/**
	 * OAuth 임시 정보 쿠키 삭제
	 */
	public static void clearOAuthTempCookies(HttpServletResponse response, HttpServletRequest request) {
		deleteCookie(response, request, OAUTH_EMAIL, "/");
		deleteCookie(response, request, OAUTH_NAME, "/");
		deleteCookie(response, request, OAUTH_ID, "/");
		deleteCookie(response, request, OAUTH_PROVIDER, "/");
	}

	// ==================== Helper Methods ====================

	/**
	 * URL 인코딩 (UTF-8)
	 * null-safe
	 */
	private static String urlEncode(String value) {
		if (value == null || value.isBlank()) {
			return "";
		}
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			// UTF-8은 항상 지원되므로 이 예외는 발생하지 않음
			log.error("[Cookie] UTF-8 encoding not supported", e);
			return value;
		}
	}

	/**
	 * URL 디코딩 (UTF-8)
	 * null-safe
	 */
	private static String urlDecode(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			log.error("[Cookie] UTF-8 decoding not supported", e);
			return value;
		}
	}

	/**
	 * 쿠키 빌더 생성 (환경별 설정 자동 적용)
	 */
	private static ResponseCookie.ResponseCookieBuilder createCookie(
		EnvironmentProfile env, String name, String value) {

		ResponseCookie.ResponseCookieBuilder builder = ResponseCookie
			.from(name, value != null ? value : "")
			.secure(env.secure)
			.sameSite(env.sameSite);

		if (env.cookieDomain != null) {
			builder.domain(env.cookieDomain);
		}

		return builder;
	}

	/**
	 * 쿠키 값 조회
	 */
	private static String getCookieValue(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}

		for (Cookie cookie : cookies) {
			if (name.equals(cookie.getName())) {
				String value = cookie.getValue();
				if (value != null && !value.isBlank()) {
					return value;
				}
			}
		}
		return null;
	}

	/**
	 * 쿠키 삭제
	 */
	private static void deleteCookie(HttpServletResponse response, HttpServletRequest request,
		String name, String path) {
		EnvironmentProfile env = EnvironmentDetector.detectEnvironment(request);

		ResponseCookie cookie = createCookie(env, name, "")
			.maxAge(Duration.ZERO)
			.path(path)
			.build();
		addCookie(response, cookie);
	}

	/**
	 * 응답에 쿠키 추가
	 */
	private static void addCookie(HttpServletResponse response, ResponseCookie cookie) {
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
		log.debug("[Cookie] Set: name={}, domain={}, path={}, maxAge={}s, httpOnly={}, secure={}, sameSite={}",
			cookie.getName(), cookie.getDomain(), cookie.getPath(),
			cookie.getMaxAge() != null ? cookie.getMaxAge().getSeconds() : "session",
			cookie.isHttpOnly(), cookie.isSecure(), cookie.getSameSite());
	}
}
