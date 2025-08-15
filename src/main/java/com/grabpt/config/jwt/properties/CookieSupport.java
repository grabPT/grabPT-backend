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

	/** role 쿠키 (프론트에서 읽어야 하므로 HttpOnly=false) */
	public static ResponseCookie roleCookie(String roleValueB64) {
		return ResponseCookie.from("role", roleValueB64 == null ? "" : roleValueB64)
			.httpOnly(false) // 프론트에서 읽음
			.secure(true)
			.sameSite("None")
			.domain("grabpt.com")
			.path("/auth")
			.maxAge(60 * 30) // 30분 (액세스 토큰 수명과 유사)
			.build();
	}

	public static ResponseCookie deleteCookie(String name) {
		return ResponseCookie.from(name, "")
			.httpOnly(true)
			.secure(true)
			.sameSite("None")
			.domain("grabpt.com")
			.path("/")
			.maxAge(0)
			.build();
	}

	/** 로그아웃 시 한번에 삭제할 세트 */
	public static ResponseCookie[] logoutDeletionSet() {
		return new ResponseCookie[] {
			// accessToken (path=/)
			deleteCookie("accessToken"),
			// refreshToken (path=/api/auth 로 발급했으니 동일 path 로 삭제)
			deleteCookie("refreshToken"),
			// role, oauth* (프론트에서 읽던 것들: httpOnly=false 로 내려도 되고 true 여도 삭제됨)
			deleteCookie("role")
		};
	}
}
