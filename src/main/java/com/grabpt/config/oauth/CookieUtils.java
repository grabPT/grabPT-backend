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

public class CookieUtils {

	private static final String COOKIE_DOMAIN = "grabpt.com"; // 앞에 점(.) 붙이지 마세요

	public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return Optional.empty();
		Cookie best = null;
		for (Cookie c : cookies) {
			if (!name.equals(c.getName()))
				continue;
			String v = c.getValue();
			if (v == null || v.isBlank())
				continue;
			if (best == null || v.length() > best.getValue().length())
				best = c;
		}
		return Optional.ofNullable(best);
	}

	public static void addCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
		ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, value)
			.path("/")
			.httpOnly(true)
			.secure(true)
			.sameSite("None")
			.maxAge(Duration.ofSeconds(maxAgeSeconds));
		if (COOKIE_DOMAIN != null)
			b.domain(COOKIE_DOMAIN);
		response.addHeader(HttpHeaders.SET_COOKIE, b.build().toString());
	}

	private static void addDeletion(HttpServletResponse response, String name, String domainOrNull) {
		ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, "")
			.path("/")
			.httpOnly(true)
			.secure(true)
			.sameSite("None")
			.maxAge(Duration.ZERO);
		if (domainOrNull != null)
			b.domain(domainOrNull);
		response.addHeader(HttpHeaders.SET_COOKIE, b.build().toString());
	}

	public static void deleteCookie(HttpServletResponse response, String name) {
		// 다양한 변형으로 저장됐을 가능성 대비해서 3가지 모두 만료
		addDeletion(response, name, null);            // host-only
		addDeletion(response, name, "api.grabpt.com");
		addDeletion(response, name, "grabpt.com");
	}

	public static String serialize(Object object) {
		return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(object));
	}

	public static <T> T deserialize(Cookie cookie, Class<T> cls) {
		return cls.cast(SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie.getValue())));
	}
}
