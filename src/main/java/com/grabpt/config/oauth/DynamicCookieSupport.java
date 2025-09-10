package com.grabpt.config.oauth;

import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 환경(로컬/프로덕션)에 따라 domain, SameSite, Secure 동적 적용 유틸.
 * - 일반 쿠키: newCookie(...)
 * - OAuth 시작 쿠키(크로스사이트 필수): newCrossSiteAuthCookie(...)
 * - 공개 쿠키 변환: asPublic(...)
 */
public class DynamicCookieSupport {

	public static class CookieProfile {
		public final String domain;   // null → host-only
		public final String sameSite; // "None" or "Lax"
		public final boolean secure;  // https only

		public CookieProfile(String domain, String sameSite, boolean secure) {
			this.domain = domain;
			this.sameSite = sameSite;
			this.secure = secure;
		}
	}

	private static String headerOr(HttpServletRequest req, String name, String fallback) {
		String v = req.getHeader(name);
		return v != null ? v : fallback;
	}

	/** 요청 기준 프로필 계산 */
	public static CookieProfile decideProfile(HttpServletRequest req) {
		String host = headerOr(req, "X-Forwarded-Host", req.getServerName());
		boolean isLocal = host != null && host.contains("localhost");
		if (isLocal) {
			// 로컬: HTTP, 호스트 전용, Lax, Secure=false
			return new CookieProfile(null, "Lax", false);
		} else {
			// 운영: grabpt.com 공유, None+Secure
			return new CookieProfile("grabpt.com", "None", true);
		}
	}

	/** 일반 쿠키(프로필 그대로) */
	public static ResponseCookie.ResponseCookieBuilder newCookie(String name, String value, HttpServletRequest req) {
		CookieProfile p = decideProfile(req);
		ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, value == null ? "" : value)
			.path("/")
			.httpOnly(true)
			.secure(p.secure)
			.sameSite(p.sameSite);
		if (p.domain != null)
			b.domain(p.domain);
		return b;
	}

	/**
	 * OAuth 시작용(authorization_request, redirect_uri_hint 등) - 콜백 시 반드시 전송되어야 하는 쿠키.
	 * 운영: None+Secure+Domain=grabpt.com / 로컬: Lax+insecure+host-only (http에서 None 불가)
	 */
	public static ResponseCookie.ResponseCookieBuilder newCrossSiteAuthCookie(String name, String value,
		HttpServletRequest req) {
		String host = headerOr(req, "X-Forwarded-Host", req.getServerName());
		boolean isLocal = host != null && host.contains("localhost");

		ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, value == null ? "" : value)
			.path("/")
			.httpOnly(true);
		if (isLocal) {
			b.sameSite("Lax").secure(false); // http 환경 제한
		} else {
			b.sameSite("None").secure(true).domain("grabpt.com");
		}
		return b;
	}

	/** 공개 쿠키로 전환(프론트에서 읽게) */
	public static ResponseCookie.ResponseCookieBuilder asPublic(ResponseCookie.ResponseCookieBuilder b) {
		return b.httpOnly(false);
	}
}
