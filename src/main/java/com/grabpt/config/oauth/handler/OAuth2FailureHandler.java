package com.grabpt.config.oauth.handler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

	// 성공 핸들러의 resolveClientBase와 동일한 방식이면 OK
	private static String resolveClientBase(HttpServletRequest req) {
		String xfHost = req.getHeader("X-Forwarded-Host");
		String xfProto = req.getHeader("X-Forwarded-Proto");
		String xfPort = req.getHeader("X-Forwarded-Port");

		String host = (xfHost != null && !xfHost.isBlank()) ? xfHost : req.getServerName();
		String proto = (xfProto != null && !xfProto.isBlank()) ? xfProto : (req.isSecure() ? "https" : "http");
		int port = req.getServerPort();
		if (xfPort != null && !xfPort.isBlank()) {
			try {
				port = Integer.parseInt(xfPort);
			} catch (Exception ignored) {
			}
		}
		boolean standard = ("http".equalsIgnoreCase(proto) && port == 80)
			|| ("https".equalsIgnoreCase(proto) && port == 443);
		String base = proto + "://" + host + (standard ? "" : ":" + port);

		// 운영 고정 필요 시: host가 api.grabpt.com이면 https://www.grabpt.com 반환 등 커스텀 가능
		return "api.grabpt.com".equalsIgnoreCase(host) ? "https://www.grabpt.com" : base;
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException ex) throws IOException {
		// 1) 원인 로깅
		log.warn("[OAUTH][FAIL] {}: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);

		// 2) 프론트 에러 페이지로 리다이렉트
		String clientBase = resolveClientBase(request);
		String reason = Optional.ofNullable(ex.getMessage()).orElse("oauth_error");
		String q = URLEncoder.encode(reason, StandardCharsets.UTF_8);
		response.sendRedirect(clientBase + "/auth/error?reason=" + q);
	}
}
