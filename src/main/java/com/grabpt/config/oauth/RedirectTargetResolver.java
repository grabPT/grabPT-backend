package com.grabpt.config.oauth;

import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

public class RedirectTargetResolver {
	public static final String REDIRECT_URI_COOKIE = "redirect_uri_hint";

	public enum EnvTarget {
		LOCAL_FE("http://localhost:5173"),
		LOCAL_BE("http://localhost:8080"),
		PROD_FE("https://www.grabpt.com");

		public final String base;

		EnvTarget(String base) {
			this.base = base;
		}
	}

	/** 초기 요청의 Host/Referer/Origin 또는 저장된 쿠키를 바탕으로 프론트 기준 base URL을 결정 */
	public static String resolveFrontendBase(HttpServletRequest request, String cookieRedirectUri) {
		// 1) 우선순위: 명시 쿠키 혹은 파라미터(쿠키에 저장되어 옴)
		if (StringUtils.hasText(cookieRedirectUri)) {
			return normalize(cookieRedirectUri);
		}

		// 2) Referer / Origin 에서 판별
		String referer = request.getHeader("Referer");
		String origin = request.getHeader("Origin");

		String hint = firstNonEmpty(referer, origin);
		if (contains(hint, "localhost:5173"))
			return EnvTarget.LOCAL_FE.name().equals("LOCAL_FE") ?
				EnvTarget.LOCAL_FE.toString().replace("LOCAL_FE", "http://localhost:5173") :
				"http://localhost:5173"; // toString 보호
		if (contains(hint, "localhost:8080"))
			return EnvTarget.LOCAL_BE.toString().replace("LOCAL_BE", "http://localhost:8080");
		if (contains(hint, "www.grabpt.com"))
			return EnvTarget.PROD_FE.toString().replace("PROD_FE", "https://www.grabpt.com");

		// 3) Host 로 판별 (백엔드 접근 호스트)
		String host = firstNonEmpty(request.getHeader("X-Forwarded-Host"), request.getServerName());
		if (contains(host, "localhost")) {
			// 백엔드를 직접 두 포트 중 어디서 눌렀는지 모르면 BE로
			return "http://localhost:8080";
		}
		// api.grabpt.com 등 → 프로덕션 프론트로
		return "https://www.grabpt.com";
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

	private static String normalize(String v) {
		// base 주소만 들어오지 않고 /authcallback 같이 붙어 들어오면 base만 뽑고 싶다면 여기 처리
		// 지금은 들어온 값을 그대로 신뢰(화이트리스트 추가로 보호)
		return v.trim();
	}

	/** 오픈 리다이렉트 방지: 허용된 도메인만 통과 */
	public static boolean isAllowedRedirectBase(String base) {
		if (!StringUtils.hasText(base))
			return false;
		String b = base.toLowerCase();
		return b.startsWith("http://localhost:5173")
			|| b.startsWith("http://localhost:8080")
			|| b.startsWith("https://www.grabpt.com");
	}
}
