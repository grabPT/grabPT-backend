package com.grabpt.config.jwt.properties;

import static com.grabpt.config.jwt.properties.CookieConstants.*;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 통합 쿠키 관리 클래스
 * - 환경별(로컬/운영) 쿠키 설정 자동화
 * - 일관된 쿠키 생성/조회/삭제 인터페이스
 */
@Slf4j
public final class CookieManager {

	private static final String PRODUCTION_DOMAIN = "grabpt.com";

	private CookieManager() {
		throw new AssertionError("Cannot instantiate utility class");
	}

	/**
	 * 환경 프로필 결정
	 */
	private static class Environment {
		final boolean isLocal;
		final String domain;
		final String sameSite;
		final boolean secure;

		Environment(HttpServletRequest req) {
			String host = getHeader(req, "X-Forwarded-Host", req.getServerName());
			this.isLocal = host != null && (host.contains("localhost") || host.contains("127.0.0.1"));

			if (isLocal) {
				this.domain = null;  // host-only
				this.sameSite = "Lax";
				this.secure = false;
			} else {
				this.domain = PRODUCTION_DOMAIN;
				this.sameSite = "None";
				this.secure = true;
			}
		}

		private String getHeader(HttpServletRequest req, String name, String fallback) {
			String value = req.getHeader(name);
			return value != null ? value : fallback;
		}
	}

	// ==================== JWT 토큰 쿠키 ====================

	/**
	 * Access Token 쿠키 생성 (15분)
	 */
	public static void setAccessToken(HttpServletResponse response, HttpServletRequest request, String token) {
		ResponseCookie cookie = createCookie(request, ACCESS_TOKEN, token)
			.httpOnly(true)
			.maxAge(Duration.ofMinutes(15))
			.path("/")
			.build();
		addCookie(response, cookie);
	}

	/**
	 * Refresh Token 쿠키 생성 (7일)
	 */
	public static void setRefreshToken(HttpServletResponse response, HttpServletRequest request, String token) {
		Environment env = new Environment(request);

		ResponseCookie cookie = createCookie(request, REFRESH_TOKEN, token)
			.httpOnly(true)
			.maxAge(Duration.ofDays(7))
			.path(env.isLocal ? "/" : "/api/auth/reissue")  // 운영: 특정 경로만
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
		ResponseCookie cookie = createCookie(request, ROLE, role)
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
		ResponseCookie cookie = createCookie(request, USER_ID, userId)
			.httpOnly(false)
			.maxAge(Duration.ofDays(30))
			.path("/")
			.build();
		addCookie(response, cookie);
	}

	// ==================== OAuth 임시 정보 쿠키 ====================

	/**
	 * OAuth 회원가입용 임시 정보 설정 (5분)
	 * HttpOnly로 설정하여 XSS 방지
	 */
	public static void setOAuthTempInfo(HttpServletResponse response, HttpServletRequest request,
		String email, String name, String oauthId, String provider) {
		Duration ttl = Duration.ofMinutes(5);

		addCookie(response, createCookie(request, OAUTH_EMAIL, email)
			.httpOnly(true)  // ⚠️ XSS 방지를 위해 HttpOnly 설정
			.maxAge(ttl).path("/").build());

		addCookie(response, createCookie(request, OAUTH_NAME, name)
			.httpOnly(true)
			.maxAge(ttl).path("/").build());

		addCookie(response, createCookie(request, OAUTH_ID, oauthId)
			.httpOnly(true)
			.maxAge(ttl).path("/").build());

		addCookie(response, createCookie(request, OAUTH_PROVIDER, provider)
			.httpOnly(true)
			.maxAge(ttl).path("/").build());
	}

	/**
	 * OAuth 임시 정보 조회 (서버 사이드에서만)
	 */
	public static OAuthTempInfo getOAuthTempInfo(HttpServletRequest request) {
		return new OAuthTempInfo(
			getCookieValue(request, OAUTH_EMAIL),
			getCookieValue(request, OAUTH_NAME),
			getCookieValue(request, OAUTH_ID),
			getCookieValue(request, OAUTH_PROVIDER)
		);
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
	 * 쿠키 빌더 생성 (환경별 설정 자동 적용)
	 */
	private static ResponseCookie.ResponseCookieBuilder createCookie(
		HttpServletRequest request, String name, String value) {
		Environment env = new Environment(request);

		ResponseCookie.ResponseCookieBuilder builder = ResponseCookie
			.from(name, value != null ? value : "")
			.secure(env.secure)
			.sameSite(env.sameSite);

		if (env.domain != null) {
			builder.domain(env.domain);
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
		ResponseCookie cookie = createCookie(request, name, "")
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
		log.debug("Cookie set: name={}, path={}, maxAge={}, httpOnly={}, secure={}, sameSite={}",
			cookie.getName(), cookie.getPath(), cookie.getMaxAge(),
			cookie.isHttpOnly(), cookie.isSecure(), cookie.getSameSite());
	}
}
