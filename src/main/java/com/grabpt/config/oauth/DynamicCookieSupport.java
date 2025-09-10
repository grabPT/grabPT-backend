package com.grabpt.config.oauth;

import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 환경(로컬/프로덕션)에 따라 domain, SameSite, Secure 속성을 동적으로 결정하는 유틸
 */
public class DynamicCookieSupport {

	public static class CookieProfile {
		public final String domain;     // null → host-only
		public final String sameSite;   // "None" for cross-site, "Lax" for same-site
		public final boolean secure;    // https only

		public CookieProfile(String domain, String sameSite, boolean secure) {
			this.domain = domain;
			this.sameSite = sameSite;
			this.secure = secure;
		}
	}

	/** 요청 기준으로 쿠키 프로파일 결정 */
	public static CookieProfile decideProfile(HttpServletRequest req) {
		String scheme = req.getHeader("X-Forwarded-Proto");
		if (scheme == null)
			scheme = req.getScheme();
		String host = req.getHeader("X-Forwarded-Host");
		if (host == null)
			host = req.getServerName();

		boolean isHttps = "https".equalsIgnoreCase(scheme);
		boolean isLocal = host != null && host.contains("localhost");

		if (isLocal) {
			// 로컬 개발 환경
			return new CookieProfile(null, "Lax", false);
		} else {
			// 운영 환경 (api.grabpt.com ↔ www.grabpt.com 공유 필요)
			return new CookieProfile("grabpt.com", "None", true);
		}
	}

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
}
