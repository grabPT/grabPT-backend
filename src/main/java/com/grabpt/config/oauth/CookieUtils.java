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

	public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return Optional.of(cookie);
				}
			}
		}
		return Optional.empty();
	}

	public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
		ResponseCookie cookie = ResponseCookie.from(name, value)
			.path("/")
			.httpOnly(true)
			.secure(true)            // HTTPS 전용
			.sameSite("None")        // 크로스사이트 리다이렉트 허용
			.maxAge(Duration.ofSeconds(maxAge))
			// .domain("api.grabpt.com") // (옵션) 굳이 필요 없으면 host-only 유지
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
		ResponseCookie cookie = ResponseCookie.from(name, "")
			.path("/")
			.httpOnly(true)
			.secure(true)
			.sameSite("None")
			.maxAge(Duration.ZERO)   // 즉시 만료
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	public static String serialize(Object object) {
		return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(object));
	}

	public static <T> T deserialize(Cookie cookie, Class<T> cls) {
		return cls.cast(SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie.getValue())));
	}
}
