package com.grabpt.config.oauth.handler;

import java.time.Duration;

import org.springframework.http.ResponseCookie;

import com.grabpt.config.oauth.CookieProfiles.CookieProfile;

/**
 * 리다이렉트 대상 환경(로컬/배포)에 맞춰 쿠키 속성을 동적으로 구성하는 헬퍼.
 * - 배포: domain=grabpt.com, Secure=true, SameSite=None
 * - 로컬: domain=null(host-only), Secure=false, SameSite=Lax
 *
 * 쿠키명은 기존 컨벤션을 보수적으로 가정:
 *  - access: "Authorization" (HttpOnly)
 *  - refresh: "Refresh-Token" (HttpOnly)
 *  - role: "role" (프론트에서 읽을 값, HttpOnly=false)
 *  - userId: "userId" (프론트에서 읽을 값, HttpOnly=false)
 *
 * 필요하면 프로젝트 내 기존 명칭에 맞게 자유롭게 조정하세요.
 */
public final class DynamicCookieSupport {

	private DynamicCookieSupport() {
	}

	public static ResponseCookie accessCookie(String token, Duration maxAge, CookieProfile prof) {
		return ResponseCookie.from("Authorization", token == null ? "" : token)
			.domain(prof.domain())
			.path("/")
			.maxAge(maxAge)
			.secure(prof.secure())
			.httpOnly(true)
			.sameSite(prof.sameSite())
			.build();
	}

	public static ResponseCookie refreshCookie(String token, Duration maxAge, CookieProfile prof) {
		return ResponseCookie.from("Refresh-Token", token == null ? "" : token)
			.domain(prof.domain())
			.path("/")
			.maxAge(maxAge)
			.secure(prof.secure())
			.httpOnly(true)
			.sameSite(prof.sameSite())
			.build();
	}

	/** 필요시 배포에서 루트 경로/도메인 치유를 위해 동일값 중복 발급 */
	public static ResponseCookie refreshCookieAtRoot(String token, Duration maxAge, CookieProfile prof) {
		return ResponseCookie.from("Refresh-Token", token == null ? "" : token)
			.domain(prof.domain())
			.path("/")
			.maxAge(maxAge)
			.secure(prof.secure())
			.httpOnly(true)
			.sameSite(prof.sameSite())
			.build();
	}

	public static ResponseCookie roleCookie(String b64Role, Duration maxAge, CookieProfile prof) {
		return ResponseCookie.from("role", b64Role == null ? "" : b64Role)
			.domain(prof.domain())
			.path("/")
			.maxAge(maxAge)
			.secure(prof.secure())
			.httpOnly(false) // 프론트에서 읽어야 하므로
			.sameSite(prof.sameSite())
			.build();
	}

	public static ResponseCookie userIdCookie(String b64UserId, Duration maxAge, CookieProfile prof) {
		return ResponseCookie.from("userId", b64UserId == null ? "" : b64UserId)
			.domain(prof.domain())
			.path("/")
			.maxAge(maxAge)
			.secure(prof.secure())
			.httpOnly(false) // 프론트에서 읽어야 하므로
			.sameSite(prof.sameSite())
			.build();
	}
}
