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

	private static final String COOKIE_DOMAIN = null;

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
				log.debug("[COOKIE][GET]   candidate name={} len={} prefix={}",
					c.getName(), v == null ? 0 : v.length(), v == null ? "null" : abbr(v));
				if (v != null && !v.isBlank() && (best == null || v.length() > best.getValue().length())) {
					best = c;
				}
			}
		}
		if (best != null) {
			log.debug("[COOKIE][GET]   -> picked name={} len={} prefix={}",
				best.getName(), best.getValue().length(), abbr(best.getValue()));
		} else {
			log.debug("[COOKIE][GET]   -> {} not found", name);
		}
		return Optional.ofNullable(best);
	}

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
			built.getDomain(), built.getPath(), built.getSameSite(),
			built.isSecure(), built.isHttpOnly(), built.getMaxAge().getSeconds(), header);

		response.addHeader(HttpHeaders.SET_COOKIE, header);
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
		var header = built.toString();
		log.debug("[COOKIE][DEL]  name={} domain={} -> {}", name, domainOrNull, header);
		response.addHeader(HttpHeaders.SET_COOKIE, header);
	}

	public static void deleteCookie(HttpServletResponse response, String name) {
		addDeletion(response, name, null);
	}

	public static String serialize(Object object) {
		return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(object));
	}

	public static <T> T deserialize(Cookie cookie, Class<T> cls) {
		return cls.cast(SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie.getValue())));
	}
}
