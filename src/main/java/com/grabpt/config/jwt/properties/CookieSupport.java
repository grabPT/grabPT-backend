package com.grabpt.config.jwt.properties;

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
			.sameSite("Lax")
			.domain("grabpt.com")
			.path("/auth")         // 리프레시 전용 경로로 좁히기 권장
			.maxAge(60L * 60 * 24 * 7) // 7일
			.build();
	}

	public static ResponseCookie deleteCookie(String name) {
		return ResponseCookie.from(name, "")
			.httpOnly(true).secure(true).sameSite("Lax")
			.domain("grabpt.com").path("/").maxAge(0).build();
	}
}
