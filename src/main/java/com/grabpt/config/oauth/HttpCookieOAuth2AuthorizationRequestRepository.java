package com.grabpt.config.oauth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.SerializationUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HttpCookieOAuth2AuthorizationRequestRepository
	implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
	public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
	private static final int COOKIE_EXPIRE_SECONDS = 180; // 3분 (충분)

	private final String cookieDomain = null; // <- 우리의 공용 도메인
	private final boolean secure = true;

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		return getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME)
			.map(this::deserialize)
			.orElse(null);
	}

	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authRequest,
		HttpServletRequest request,
		HttpServletResponse response) {
		if (authRequest == null) {
			deleteCookie(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
			deleteCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME);
			return;
		}

		// 인가요청 자체를 쿠키에 저장
		addCookie(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME, serialize(authRequest), COOKIE_EXPIRE_SECONDS);

		// 클라이언트에서 redirect_uri 쿼리로 넘기면 이것도 함께 저장
		String redirectUri = request.getParameter("redirect_uri");
		if (redirectUri != null && !redirectUri.isBlank()) {
			addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUri, COOKIE_EXPIRE_SECONDS);
		}
	}

	// Spring Security 5.7+ 시그니처
	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
		HttpServletResponse response) {
		OAuth2AuthorizationRequest req = loadAuthorizationRequest(request);
		deleteCookie(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
		deleteCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME);
		return req;
	}

	// ---- helpers ----

	private Optional<String> getCookie(HttpServletRequest request, String name) {
		if (request.getCookies() == null)
			return Optional.empty();
		for (var c : request.getCookies()) {
			if (c.getName().equals(name))
				return Optional.ofNullable(c.getValue());
		}
		return Optional.empty();
	}

	private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
		// ResponseCookie는 SameSite 지정 가능 (Servlet Cookie는 불가)
		ResponseCookie cookie = ResponseCookie.from(name, value)
			.domain(cookieDomain)
			.path("/")
			.httpOnly(true)
			.secure(secure)
			.sameSite("None")
			.maxAge(maxAge)
			.build();
		response.addHeader("Set-Cookie", cookie.toString());
	}

	private void deleteCookie(HttpServletResponse response, String name) {
		ResponseCookie cookie = ResponseCookie.from(name, "")
			.domain(cookieDomain)
			.path("/")
			.httpOnly(true)
			.secure(secure)
			.sameSite("None")
			.maxAge(0)
			.build();
		response.addHeader("Set-Cookie", cookie.toString());
	}

	private String serialize(OAuth2AuthorizationRequest obj) {
		byte[] bytes = SerializationUtils.serialize(obj);
		return Base64.getUrlEncoder().encodeToString(bytes);
	}

	private OAuth2AuthorizationRequest deserialize(String value) {
		byte[] bytes = Base64.getUrlDecoder().decode(value.getBytes(StandardCharsets.UTF_8));
		return (OAuth2AuthorizationRequest)SerializationUtils.deserialize(bytes);
	}
}
