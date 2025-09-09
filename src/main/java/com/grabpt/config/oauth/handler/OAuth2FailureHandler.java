package com.grabpt.config.oauth.handler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OAuth2FailureHandler implements AuthenticationFailureHandler {
	@Override
	public void onAuthenticationFailure(HttpServletRequest req, HttpServletResponse res,
		AuthenticationException ex) throws IOException {
		String host = FrontendRoutingSupport.detectHost(req);
		String code = (ex instanceof OAuth2AuthenticationException)
			? ((OAuth2AuthenticationException)ex).getError().getErrorCode()
			: ex.getClass().getSimpleName();
		log.error("OAuth2 FAIL host={} url={} code={} msg={}", host, req.getRequestURL(), code, ex.getMessage());

		String feBase = FrontendRoutingSupport.frontendBase(req);
		res.sendRedirect(feBase + "/auth/error?code=" + URLEncoder.encode(code, StandardCharsets.UTF_8));
	}
}
