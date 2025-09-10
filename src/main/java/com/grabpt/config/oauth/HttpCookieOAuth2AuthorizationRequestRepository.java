package com.grabpt.config.oauth;

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
	public static final String REDIRECT_URI_PARAM_COOKIE_NAME = com.grabpt.config.oauth.RedirectTargetResolver.REDIRECT_URI_COOKIE;
	private static final int COOKIE_EXPIRE_SECONDS = 180; // 3분

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		// (원문 로깅/예외 처리 유지)
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
			return;
		}

		String serialized = CookieUtils.serialize(authorizationRequest);
		CookieUtils.addCookie(response,
			OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
			serialized,
			COOKIE_EXPIRE_SECONDS);

		// 프론트 최종 리다이렉트를 위해 명시 파라미터가 있으면 쿠키로 보존
		String redirectBase = request.getParameter("redirect_uri");
		if (StringUtils.isNotBlank(redirectBase)) {
			ResponseCookie c = ResponseCookie.from(REDIRECT_URI_PARAM_COOKIE_NAME, redirectBase)
				.httpOnly(true).secure(false).sameSite("Lax")
				.path("/")
				.maxAge(COOKIE_EXPIRE_SECONDS)
				.build();
			response.addHeader("Set-Cookie", c.toString());
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
