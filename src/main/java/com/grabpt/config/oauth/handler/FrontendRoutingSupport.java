package com.grabpt.config.oauth.handler;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.HttpServletRequest;

public class FrontendRoutingSupport {
	private FrontendRoutingSupport() {
	}

	public static String forwarded(HttpServletRequest req, String name) {
		String v = req.getHeader(name);
		if (v == null || v.isBlank())
			return null;
		return v.split(",")[0].trim();
	}

	public static String detectHost(HttpServletRequest req) {
		String h = forwarded(req, "X-Forwarded-Host");
		if (h == null)
			h = req.getServerName();
		int colon = h.indexOf(':');
		if (colon >= 0)
			h = h.substring(0, colon);
		return h.toLowerCase();
	}

	public static String detectProto(HttpServletRequest req) {
		String p = forwarded(req, "X-Forwarded-Proto");
		if (p != null)
			return p;
		return req.isSecure() ? "https" : "http";
	}

	public static String detectPort(HttpServletRequest req) {
		String p = forwarded(req, "X-Forwarded-Port");
		if (p != null)
			return p;
		return String.valueOf(req.getServerPort());
	}

	/** 백엔드의 baseUrl (예: https://api.grabpt.com  /  http://localhost:8080) */
	public static String backendBase(HttpServletRequest req) {
		String host = detectHost(req);
		String proto = detectProto(req);
		String port = detectPort(req);

		boolean addPort = port != null
			&& !("https".equals(proto) && "443".equals(port))
			&& !("http".equals(proto) && "80".equals(port));

		return proto + "://" + host + (addPort ? (":" + port) : "");
	}

	/** 프론트의 baseUrl (로컬 ↔ 운영 분기) */
	public static String frontendBase(HttpServletRequest req) {
		return isLocal(req) ? "http://localhost:5173" : "https://www.grabpt.com";
	}

	public static boolean isLocal(HttpServletRequest req) {
		String h = detectHost(req);
		return "localhost".equals(h) || h.startsWith("127.");
	}

	/** state/redirect_uri 같은 임시 쿠키용 – 요청 호스트에 맞춰 굽기 */
	public static void addTempCookie(HttpServletRequest req, jakarta.servlet.http.HttpServletResponse res,
		String name, String value, int maxAgeSeconds, boolean httpOnly) {
		ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, value == null ? "" : value)
			.path("/")
			.maxAge(maxAgeSeconds)
			.httpOnly(httpOnly)
			.sameSite("Lax");
		if (isLocal(req)) {
			b.secure(false);            // 로컬: http 지원
			// Domain 미설정(host-only)
		} else {
			b.secure(true);             // 운영: https
			// Domain은 보통 host-only로 충분 (필요 시 .grabpt.com 대신 api.grabpt.com 권장)
		}
		res.addHeader(HttpHeaders.SET_COOKIE, b.build().toString());
	}

	public static void deleteTempCookie(HttpServletRequest req, jakarta.servlet.http.HttpServletResponse res,
		String name) {
		ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, "")
			.path("/")
			.maxAge(0)
			.httpOnly(true)
			.sameSite("Lax");
		if (isLocal(req))
			b.secure(false);
		else
			b.secure(true);
		res.addHeader(HttpHeaders.SET_COOKIE, b.build().toString());
	}
}
