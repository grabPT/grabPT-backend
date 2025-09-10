package com.grabpt.config.oauth;

import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.util.SerializationUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CookieUtils {

	// OAuth 상태 쿠키를 운영에서 찍을 도메인(백엔드 호스트)
	private static final String PROD_COOKIE_DOMAIN_FOR_OAUTH = "api.grabpt.com";

	private static final String COOKIE_DOMAIN = null; // 일반 쿠키용 host-only 기본 유지

	private static boolean isProdProfile() {
		String sp = System.getProperty("spring.profiles.active", "");
		if (sp != null && sp.toLowerCase().contains("prod"))
			return true;
		String ev = System.getenv("SPRING_PROFILES_ACTIVE");
		return ev != null && ev.toLowerCase().contains("prod");
	}

	private static String abbr(String s) {
		if (s == null)
			return "null";
		return s.substring(0, Math.min(24, s.length())) + (s.length() > 24 ? "..." : "");
	}

	public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			log.debug("[COOKIE][GET] reqUri={} -> no cookies", request.getRequestURI());
			return Optional.empty();
		}
		log.debug("[COOKIE][GET] reqUri={} cookieCount={}", request.getRequestURI(), cookies.length);
		Cookie best = null;
		for (Cookie c : cookies) {
			if (name.equals(c.getName())) {
				String v = c.getValue();
				log.debug("[COOKIE][GET] candidate name={} len={} prefix={}", c.getName(),
					v == null ? 0 : v.length(), v == null ? "null" : abbr(v));
				if (v != null && !v.isBlank() && (best == null || v.length() > best.getValue().length())) {
					best = c;
				}
			}
		}
		if (best != null) {
			log.debug("[COOKIE][GET] -> picked name={} len={} prefix={}",
				best.getName(), best.getValue().length(), abbr(best.getValue()));
		} else {
			log.debug("[COOKIE][GET] -> {} not found", name);
		}
		return Optional.ofNullable(best);
	}

	/** 일반 쿠키(기존 정책 유지): SameSite=Lax, Secure=true (host-only) */
	public static void addCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
		ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, value)
			.path("/")
			.httpOnly(true)
			.secure(true)
			.sameSite("Lax")
			.maxAge(Duration.ofSeconds(maxAgeSeconds));

		if (COOKIE_DOMAIN != null)
			b.domain(COOKIE_DOMAIN);

		var built = b.build();
		var header = built.toString();
		log.debug("[COOKIE][ADD] name={} len={} domain={} path={} sameSite={} secure={} httpOnly={} maxAge={} -> {}",
			name, value == null ? 0 : value.length(),
			built.getDomain(), built.getPath(), built.getSameSite(), built.isSecure(),
			built.isHttpOnly(), built.getMaxAge().getSeconds(), header);
		response.addHeader(HttpHeaders.SET_COOKIE, header);
	}

	/** OAuth state/redirect 전용: prod는 None+Secure+Domain, dev는 Lax+insecure+host-only */
	public static void addOAuthStateCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
		boolean prod = isProdProfile();
		ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, value)
			.path("/")
			.httpOnly(true)
			.maxAge(Duration.ofSeconds(maxAgeSeconds));

		if (prod) {
			b.secure(true)
				.sameSite("None")
				.domain(PROD_COOKIE_DOMAIN_FOR_OAUTH);
		} else {
			b.secure(false)
				.sameSite("Lax");
		}

		var built = b.build();
		log.debug(
			"[COOKIE][ADD][OAUTH] name={} len={} domain={} path={} sameSite={} secure={} httpOnly={} maxAge={} -> {}",
			name, value == null ? 0 : value.length(),
			built.getDomain(), built.getPath(), built.getSameSite(), built.isSecure(),
			built.isHttpOnly(), built.getMaxAge().getSeconds(), built.toString());
		response.addHeader(HttpHeaders.SET_COOKIE, built.toString());
	}

	private static void addDeletion(HttpServletResponse response, String name, String domainOrNull) {
		ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, "")
			.path("/")
			.httpOnly(true)
			.secure(true)
			.sameSite("Lax")
			.maxAge(Duration.ZERO);

		if (domainOrNull != null)
			b.domain(domainOrNull);
		var built = b.build();
		log.debug("[COOKIE][DEL] name={} domain={} -> {}", name, domainOrNull, built.toString());
		response.addHeader(HttpHeaders.SET_COOKIE, built.toString());
	}

	public static void deleteCookie(HttpServletResponse response, String name) {
		addDeletion(response, name, null);              // host-only
		addDeletion(response, name, "api.grabpt.com");  // 서브도메인
		addDeletion(response, name, "grabpt.com");      // 최상위
	}

	public static String serialize(Object object) {
		return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(object));
	}

	public static <T> T deserialize(Cookie cookie, Class<T> cls) {
		return cls.cast(SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie.getValue())));
	}
}
