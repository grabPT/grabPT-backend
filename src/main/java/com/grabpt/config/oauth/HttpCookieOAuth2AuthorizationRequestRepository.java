package com.grabpt.config.oauth;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import com.grabpt.config.oauth.support.RedirectTargetResolver;
import com.nimbusds.oauth2.sdk.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpCookieOAuth2AuthorizationRequestRepository
	implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
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
			return; // 삭제는 remove에서
		}

		// 1) auth request 자체 저장 (크로스사이트 전송 보장)
		String serialized = CookieUtils.serialize(authorizationRequest);
		ResponseCookie c1 = DynamicCookieSupport
			.newCrossSiteAuthCookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serialized, request)
			.maxAge(Duration.ofSeconds(COOKIE_EXPIRE_SECONDS))
			.build();
		response.addHeader("Set-Cookie", c1.toString());

		// 2) redirect 힌트: ?redirect_uri=... 또는 ?redirect_uri_hint=... 둘 다 허용
		String redirectBase = request.getParameter(RedirectTargetResolver.REDIRECT_URI_COOKIE);
		if (!StringUtils.isNotBlank(redirectBase)) {
			redirectBase = request.getParameter(RedirectTargetResolver.ALT_REDIRECT_URI_COOKIE);
		}
		if (StringUtils.isNotBlank(redirectBase)) {
			// 세션에도 저장(쿠키 미전송 대비)
			request.getSession(true).setAttribute(RedirectTargetResolver.REDIRECT_URI_COOKIE, redirectBase);

			// 쿠키 저장(이름은 'redirect_uri' 로 통일)
			ResponseCookie c2 = DynamicCookieSupport
				.newCrossSiteAuthCookie(RedirectTargetResolver.REDIRECT_URI_COOKIE, redirectBase, request)
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
		CookieUtils.deleteCookie(response, RedirectTargetResolver.REDIRECT_URI_COOKIE);
		CookieUtils.deleteCookie(response, RedirectTargetResolver.ALT_REDIRECT_URI_COOKIE); // 호환 삭제
		return req;
	}
}
