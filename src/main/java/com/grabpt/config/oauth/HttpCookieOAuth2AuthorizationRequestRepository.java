package com.grabpt.config.oauth;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import com.nimbusds.oauth2.sdk.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpCookieOAuth2AuthorizationRequestRepository
	implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
	public static final String REDIRECT_URI_PARAM_COOKIE_NAME = RedirectTargetResolver.REDIRECT_URI_COOKIE;
	private static final int COOKIE_EXPIRE_SECONDS = 180; // 3분

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		try {
			return CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
				.map(c -> CookieUtils.deserialize(c, OAuth2AuthorizationRequest.class))
				.orElse(null);
		} catch (Exception e) {
			log.warn("[OAUTH][LOAD] deserialize failed: {}", e.toString(), e);
			return null;
		}
	}

	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
		HttpServletRequest request,
		HttpServletResponse response) {
		if (authorizationRequest == null) {
			return; // 조기 삭제 방지: 실제 삭제는 remove에서
		}

		// 1) OAuth2AuthorizationRequest 자체를 크로스사이트 쿠키로 저장
		String serialized = CookieUtils.serialize(authorizationRequest);
		ResponseCookie c1 = DynamicCookieSupport
			.newCrossSiteAuthCookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serialized, request)
			.maxAge(Duration.ofSeconds(COOKIE_EXPIRE_SECONDS))
			.build();
		response.addHeader("Set-Cookie", c1.toString());

		// 2) redirect 힌트: 세션 + 쿠키 저장
		String redirectBase = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
		if (StringUtils.isNotBlank(redirectBase)) {
			// 세션 저장 (쿠키가 안 와도 복구)
			request.getSession(true).setAttribute(REDIRECT_URI_PARAM_COOKIE_NAME, redirectBase);

			ResponseCookie c2 = DynamicCookieSupport
				.newCrossSiteAuthCookie(REDIRECT_URI_PARAM_COOKIE_NAME, redirectBase, request)
				.maxAge(Duration.ofSeconds(COOKIE_EXPIRE_SECONDS))
				.build();
			response.addHeader("Set-Cookie", c2.toString());
		}
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
		HttpServletResponse response) {
		var req = loadAuthorizationRequest(request);
		CookieUtils.deleteCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
		CookieUtils.deleteCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME);
		return req;
	}
}
