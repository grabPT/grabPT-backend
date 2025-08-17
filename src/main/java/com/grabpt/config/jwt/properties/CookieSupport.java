package com.grabpt.config.jwt.properties;

import java.time.Duration;

import org.springframework.http.ResponseCookie;

public final class CookieSupport {

	private CookieSupport() {
	}

	public static ResponseCookie accessCookie(String token) {
		return ResponseCookie.from("accessToken", token)
			.httpOnly(true)        // JS 접근 차단(XSS 방어)
			.secure(true)          // HTTPS 전용
			.sameSite("Lax")       // same-site라 Lax면 충분 (필요시 "None")
			.domain("grabpt.com")
			.path("/")
			.maxAge(60 * 15)       // 예: 15분
			.build();
	}

	public static ResponseCookie refreshCookie(String token) {
		return ResponseCookie.from("refreshToken", token)
			.httpOnly(true)
			.secure(true)
			.sameSite("None")
			.domain("grabpt.com")      // 이미 사용 중인 도메인과 동일하게
			.path("/api/auth/reissue")
			.maxAge(Duration.ofDays(7)) // 현재 유효기간 정책 유지
			.build();
	}

	/** role 쿠키 (프론트에서 읽어야 하므로 HttpOnly=false) */
	public static ResponseCookie roleCookie(String roleValueB64) {
		return ResponseCookie.from("role", roleValueB64 == null ? "" : roleValueB64)
			.httpOnly(false) // 프론트에서 읽음
			.secure(true)
			.sameSite("None")
			.domain("grabpt.com")
			.path("/")
			.maxAge(60 * 30) // 30분 (액세스 토큰 수명과 유사)
			.build();
	}

	public static ResponseCookie deleteCookie(String name, String path) {
		return ResponseCookie.from(name, "")
			.httpOnly(true)
			.secure(true)
			.sameSite("None")
			.domain("grabpt.com")
			.path(path)
			.maxAge(0)
			.build();
	}

	/** 로그아웃 시 한번에 삭제할 세트 */
	public static ResponseCookie[] logoutDeletionSet() {
		return new ResponseCookie[] {
			deleteAccessCookie(),
			deleteRefreshCookie(),
			// role은 프론트에서 읽는 쿠키: 발급 속성(sameSite=None, path="/")에 맞춰 삭제
			ResponseCookie.from("role", "")
				.httpOnly(false).secure(true).sameSite("None")
				.domain("grabpt.com").path("/").maxAge(0).build()
		};
	}

	public static ResponseCookie deleteAccessCookie() {
		return ResponseCookie.from("accessToken", "")
			.httpOnly(true).secure(true).sameSite("Lax")
			.domain("grabpt.com").path("/")
			.maxAge(0).build();
	}

	public static ResponseCookie deleteRefreshCookie() {
		return ResponseCookie.from("refreshToken", "")
			.httpOnly(true).secure(true).sameSite("None")
			.domain("grabpt.com").path("/api/auth/reissue")
			.maxAge(0).build();
	}

	public static ResponseCookie deleteRefreshCookieAtRoot() {
		return ResponseCookie.from("refreshToken", "")
			.httpOnly(true).secure(true).sameSite("None")
			.domain("grabpt.com").path("/")
			.maxAge(0).build();
	}

	public static ResponseCookie refreshCookieAtRoot(String token) {
		return ResponseCookie.from("refreshToken", token)
			.httpOnly(true).secure(true).sameSite("None")
			.domain("grabpt.com").path("/")
			.maxAge(Duration.ofDays(7)).build();
	}
}
