package com.grabpt.config.oauth;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import com.nimbusds.oauth2.sdk.util.StringUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpCookieOAuth2AuthorizationRequestRepository
	implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
	public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
	private static final int COOKIE_EXPIRE_SECONDS = 180;

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		log.debug("[OAUTH][LOAD] uri={} method={} sessionId={} cookieCount={}",
			request.getRequestURI(), request.getMethod(),
			request.getRequestedSessionId(),
			request.getCookies() == null ? 0 : request.getCookies().length);

		if (request.getCookies() != null) {
			for (Cookie c : request.getCookies()) {
				if (c.getName().equals(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
					|| c.getName().equals(REDIRECT_URI_PARAM_COOKIE_NAME)
					|| c.getName().equals("JSESSIONID")) {
					String v = c.getValue();
					log.debug("[OAUTH][LOAD]   {} len={} prefix={}",
						c.getName(), v == null ? 0 : v.length(),
						v == null ? "null" : v.substring(0, Math.min(24, v.length())));
				}
			}
		}

		try {
			var req = CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
				.map(c -> CookieUtils.deserialize(c, OAuth2AuthorizationRequest.class))
				.orElse(null);
			log.debug("[OAUTH][LOAD] found? {}", req != null);
			if (req != null) {
				log.debug("[OAUTH][LOAD] clientId={} state={} redirectUri={}",
					req.getClientId(), req.getState(), req.getRedirectUri());
			}
			return req;
		} catch (Exception e) {
			log.warn("[OAUTH][LOAD] deserialize failed: {}", e.toString(), e);
			return null;
		}
	}

	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
		HttpServletRequest request,
		HttpServletResponse response) {
		boolean isNull = (authorizationRequest == null);
		log.debug("[OAUTH][SAVE] uri={} isNull={} referer={} ua={}",
			request.getRequestURI(), isNull,
			request.getHeader("Referer"), request.getHeader("User-Agent"));

		if (authorizationRequest == null) {
			return;
		}

		log.debug("[OAUTH][SAVE] clientId={} state={} redirectUri={}",
			authorizationRequest.getClientId(),
			authorizationRequest.getState(),
			authorizationRequest.getRedirectUri());

		String serialized = CookieUtils.serialize(authorizationRequest);
		log.debug("[OAUTH][SAVE] serializedLen={}", serialized.length());

		// OAuth state/redirect는 전용 발급자 사용 (SameSite=None;Secure;Domain=api.grabpt.com in prod)
		CookieUtils.addOAuthStateCookie(response,
			OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serialized, COOKIE_EXPIRE_SECONDS);

		String redirectUri = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
		log.debug("[OAUTH][SAVE] redirect_uri param={}", redirectUri);
		if (StringUtils.isNotBlank(redirectUri)) {
			CookieUtils.addOAuthStateCookie(response,
				REDIRECT_URI_PARAM_COOKIE_NAME, redirectUri, COOKIE_EXPIRE_SECONDS);
		}
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
		HttpServletResponse response) {
		log.debug("[OAUTH][REMOVE] uri={}", request.getRequestURI());
		var req = loadAuthorizationRequest(request);
		CookieUtils.deleteCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
		CookieUtils.deleteCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME);
		return req;
	}
}
