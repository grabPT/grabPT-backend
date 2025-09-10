package com.grabpt.config.oauth;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtils {

	public static Optional<jakarta.servlet.http.Cookie> getCookie(HttpServletRequest request, String name) {
		if (request.getCookies() == null) {
			return Optional.empty();
		}
		return Arrays.stream(request.getCookies())
			.filter(cookie -> cookie.getName().equals(name))
			.findFirst();
	}

	// 쿠키를 동적으로 설정하는 메서드
	public static void addCookie(HttpServletResponse res, String name, String value,
		Duration maxAge, boolean httpOnly, String domain) {
		ResponseCookie c = ResponseCookie.from(name, value == null ? "" : value)
			.domain(domain) // 도메인 설정
			.path("/")
			.maxAge(maxAge)
			.secure(true) // HTTPS 사용
			.httpOnly(httpOnly)
			.sameSite("None")
			.build();
		res.addHeader("Set-Cookie", c.toString());
	}

	// 쿠키 삭제 메서드
	public static void deleteCookie(HttpServletResponse res, String name, String domain) {
		ResponseCookie c = ResponseCookie.from(name, "")
			.domain(domain)
			.path("/")
			.maxAge(0)
			.secure(true)
			.httpOnly(true)
			.sameSite("None")
			.build();
		res.addHeader("Set-Cookie", c.toString());
	}

}
