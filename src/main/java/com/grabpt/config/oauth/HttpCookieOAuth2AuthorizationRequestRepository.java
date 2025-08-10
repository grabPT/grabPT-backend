package com.grabpt.config.oauth;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import com.nimbusds.oauth2.sdk.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HttpCookieOAuth2AuthorizationRequestRepository
	implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
	public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
	private static final int COOKIE_EXPIRE_SECONDS = 180; // 3분

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		return CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
			.map(c -> CookieUtils.deserialize(c, OAuth2AuthorizationRequest.class))
			.orElse(null);
	}

	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
		HttpServletRequest request,
		HttpServletResponse response) {
		// ★ null이어도 삭제하지 않는다 (중복/조기 삭제 방지)
		if (authorizationRequest == null)
			return;

		CookieUtils.addCookie(response,
			OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
			CookieUtils.serialize(authorizationRequest),
			COOKIE_EXPIRE_SECONDS);

		String redirectUri = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
		if (StringUtils.isNotBlank(redirectUri)) {
			CookieUtils.addCookie(response,
				REDIRECT_URI_PARAM_COOKIE_NAME,
				redirectUri,
				COOKIE_EXPIRE_SECONDS);
		}
	}

	// ★ 실제 삭제는 이 오버로드에서만 수행
	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
		HttpServletResponse response) {
		OAuth2AuthorizationRequest req = loadAuthorizationRequest(request);
		removeAuthorizationRequestCookies(response);
		return req;
	}

	public void removeAuthorizationRequestCookies(HttpServletResponse response) {
		CookieUtils.deleteCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
		CookieUtils.deleteCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME);
	}
}
