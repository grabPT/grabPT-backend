package com.grabpt.config.oauth;

import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

public class RedirectTargetResolver {
	public static final String REDIRECT_URI_COOKIE = "redirect_uri";

	public enum EnvTarget {
		LOCAL_FE("http://localhost:5173"),
		LOCAL_BE("http://localhost:8080"),
		PROD_FE("https://www.grabpt.com");
		public final String base;

		EnvTarget(String base) {
			this.base = base;
		}
	}

	public static String resolveFrontendBase(HttpServletRequest request, String cookieRedirectUri) {
		if (StringUtils.hasText(cookieRedirectUri) && isAllowedRedirectBase(cookieRedirectUri.trim())) {
			return cookieRedirectUri.trim();
		}
		String referer = request.getHeader("Referer");
		String origin = request.getHeader("Origin");
		String host = firstNonEmpty(request.getHeader("X-Forwarded-Host"), request.getServerName());

		if (contains(referer, "localhost:5173") || contains(origin, "localhost:5173"))
			return EnvTarget.LOCAL_FE.base;
		if (contains(referer, "localhost:8080") || contains(origin, "localhost:8080"))
			return EnvTarget.LOCAL_BE.base;
		if (contains(referer, "www.grabpt.com") || contains(origin, "www.grabpt.com"))
			return EnvTarget.PROD_FE.base;
		if (contains(host, "localhost"))
			return EnvTarget.LOCAL_BE.base; // 기본 로컬
		return EnvTarget.PROD_FE.base; // 기본 운영
	}

	public static boolean isAllowedRedirectBase(String base) {
		if (!StringUtils.hasText(base))
			return false;
		String b = base.toLowerCase();
		return b.startsWith(EnvTarget.LOCAL_FE.base)
			|| b.startsWith(EnvTarget.LOCAL_BE.base)
			|| b.startsWith(EnvTarget.PROD_FE.base);
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
