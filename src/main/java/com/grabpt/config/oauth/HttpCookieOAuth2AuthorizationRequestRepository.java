package com.grabpt.config.oauth;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import com.grabpt.config.oauth.handler.FrontendRoutingSupport;
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
	private static final int COOKIE_EXPIRE_SECONDS = 180; // 3분

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		log.debug("[OAUTH][LOAD] uri={} method={} cookieCount={}",
			request.getRequestURI(), request.getMethod(),
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
		log.debug("[OAUTH][SAVE] uri={} isNull={} ua={}",
			request.getRequestURI(), isNull, request.getHeader("User-Agent"));

		if (authorizationRequest == null) {
			// 성공/실패 시점에만 삭제
			return;
		}

		// redirectUri를 '백엔드 기준'으로 강제 교체
		String regId = (String)authorizationRequest.getAttributes().get(OAuth2ParameterNames.REGISTRATION_ID);
		if (regId == null || regId.isBlank())
			regId = "google"; // fallback
		String backendBase = FrontendRoutingSupport.backendBase(request);
		String fixedRedirect = backendBase + "/login/oauth2/code/" + regId;

		OAuth2AuthorizationRequest fixed =
			OAuth2AuthorizationRequest.from(authorizationRequest)
				.redirectUri(fixedRedirect)
				.build();

		log.debug("[OAUTH][SAVE] clientId={} state={} redirectUri={} (fixed)",
			fixed.getClientId(), fixed.getState(), fixed.getRedirectUri());

		// state(authorizationRequest) 쿠키 – 로컬/운영에 맞춰 굽기
		String serialized = CookieUtils.serialize(fixed);
		log.debug("[OAUTH][SAVE] serializedLen={}", serialized.length());
		FrontendRoutingSupport.addTempCookie(request, response,
			OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serialized, COOKIE_EXPIRE_SECONDS, true);

		// (옵션) 프론트가 넘긴 redirect_uri 파라미터도 보존하고 싶다면
		String redirectUriParam = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
		log.debug("[OAUTH][SAVE] redirect_uri param={}", redirectUriParam);
		if (StringUtils.isNotBlank(redirectUriParam)) {
			FrontendRoutingSupport.addTempCookie(request, response,
				REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriParam, COOKIE_EXPIRE_SECONDS, true);
		}
	}

	// 실제 삭제는 여기서만 수행
	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
		HttpServletResponse response) {
		log.debug("[OAUTH][REMOVE] uri={}", request.getRequestURI());
		var req = loadAuthorizationRequest(request);
		FrontendRoutingSupport.deleteTempCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
		FrontendRoutingSupport.deleteTempCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
		return req;
	}
}
