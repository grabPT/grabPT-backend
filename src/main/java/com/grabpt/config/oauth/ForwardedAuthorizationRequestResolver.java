package com.grabpt.config.oauth;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import com.grabpt.config.oauth.handler.FrontendRoutingSupport;

import jakarta.servlet.http.HttpServletRequest;

public class ForwardedAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

	private final DefaultOAuth2AuthorizationRequestResolver delegate;

	public ForwardedAuthorizationRequestResolver(ClientRegistrationRepository repo) {
		// "/oauth2/authorization/{registrationId}" 기본 엔드포인트 사용
		this.delegate = new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
		OAuth2AuthorizationRequest req = delegate.resolve(request);
		return (req == null) ? null : fixRedirectUri(request, req);
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String registrationId) {
		OAuth2AuthorizationRequest req = delegate.resolve(request, registrationId);
		return (req == null) ? null : fixRedirectUri(request, req);
	}

	private OAuth2AuthorizationRequest fixRedirectUri(HttpServletRequest request, OAuth2AuthorizationRequest req) {
		String regId = (String)req.getAttributes().get(OAuth2ParameterNames.REGISTRATION_ID);
		if (regId == null || regId.isBlank())
			regId = "google";

		// 백엔드 기준 baseUrl 계산 (X-Forwarded-* 신뢰)
		String backendBase = FrontendRoutingSupport.backendBase(request);
		String redirect = backendBase + "/login/oauth2/code/" + regId;

		return OAuth2AuthorizationRequest.from(req)
			.redirectUri(redirect)   // Google로 나가는 redirect_uri 자체를 교체
			.build();
	}
}
