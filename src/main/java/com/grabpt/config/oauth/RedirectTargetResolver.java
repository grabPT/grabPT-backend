package com.grabpt.config.oauth;

import java.util.Set;

import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

public class RedirectTargetResolver {

	/** 힌트 쿠키/세션 키 (실제 저장은 'redirect_uri' 로 통일, 아래 ALT도 호환 읽기) */
	public static final String REDIRECT_URI_COOKIE = "redirect_uri";
	public static final String ALT_REDIRECT_URI_COOKIE = "redirect_uri_hint";

	public enum EnvTarget {
		LOCAL_FE("http://localhost:5173"),
		LOCAL_BE("http://localhost:8080"),
		PROD_FE("https://www.grabpt.com"),
		DEV_FE("https://grabpt-dev.vercel.app");

		public final String base;

		EnvTarget(String base) {
			this.base = base;
		}
	}

	/** 명시적으로 허용한 프론트엔드 베이스 목록(정확 일치) */
	private static final Set<String> ALLOWED_BASES = Set.of(
		EnvTarget.PROD_FE.base,
		EnvTarget.DEV_FE.base,
		EnvTarget.LOCAL_FE.base,
		EnvTarget.LOCAL_BE.base,
		"http://127.0.0.1:3000",
		"http://localhost:3000",
		"http://127.0.0.1:5173"
	);

	/**
	 * 최종 프론트엔드 베이스 URL을 결정한다.
	 * 우선순위: 명시 힌트(cookie/session) → Referer/Origin → 호스트 기반 추론 → 기본(PROD_FE)
	 */
	public static String resolveFrontendBase(HttpServletRequest request, String cookieOrSessionHint) {
		// 1) 힌트가 있고 허용 목록이면 그대로 사용
		if (StringUtils.hasText(cookieOrSessionHint) && isAllowedRedirectBase(cookieOrSessionHint.trim())) {
			return normalize(cookieOrSessionHint.trim());
		}

		// 2) 헤더 기반 추론
		String referer = header(request, "Referer");
		String origin = header(request, "Origin");
		String host = firstNonEmpty(request.getHeader("X-Forwarded-Host"), request.getServerName());

		if (contains(referer, "grabpt-dev.vercel.app") || contains(origin, "grabpt-dev.vercel.app")) {
			return EnvTarget.DEV_FE.base;
		}
		if (contains(referer, "www.grabpt.com") || contains(origin, "www.grabpt.com")) {
			return EnvTarget.PROD_FE.base;
		}
		if (contains(referer, "localhost:5173") || contains(origin, "localhost:5173")) {
			return EnvTarget.LOCAL_FE.base;
		}
		if (contains(referer, "localhost:8080") || contains(origin, "localhost:8080")) {
			return EnvTarget.LOCAL_BE.base;
		}

		// 3) 호스트 기반(개발 서버 등)
		if (contains(host, "localhost")) {
			return EnvTarget.LOCAL_BE.base;
		}

		// 4) 기본은 PROD
		return EnvTarget.PROD_FE.base;
	}

	/** 허용된 redirect base 인지 여부 (ALLOWED_BASES만 참조) */
	public static boolean isAllowedRedirectBase(String base) {
		if (!StringUtils.hasText(base))
			return false;
		return ALLOWED_BASES.contains(normalize(base));
	}

	// ===== helpers =====
	private static String normalize(String s) {
		if (s == null)
			return null;
		String t = s.trim();
		if (t.endsWith("/"))
			t = t.substring(0, t.length() - 1);
		return t;
	}

	private static String header(HttpServletRequest req, String name) {
		String v = req.getHeader(name);
		return v == null ? null : v.trim();
	}

	private static boolean contains(String s, String needle) {
		return s != null && s.toLowerCase().contains(needle.toLowerCase());
	}

	private static String firstNonEmpty(String... ss) {
		for (String s : ss)
			if (StringUtils.hasText(s))
				return s;
		return null;
	}
}
