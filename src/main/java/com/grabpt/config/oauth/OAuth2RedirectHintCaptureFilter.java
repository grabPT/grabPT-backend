package com.grabpt.config.oauth;

import java.time.Duration;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class OAuth2RedirectHintCaptureFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
		throws ServletException, java.io.IOException {

		String uri = req.getRequestURI();
		if (uri != null && uri.startsWith("/oauth2/authorization/")) {
			String hint = req.getParameter("redirect_uri");

			if (StringUtils.hasText(hint) && RedirectTargetResolver.isAllowedRedirectBase(hint)) {
				// 이번 플로우에서 명시된 hint 저장(세션)
				req.getSession(true).setAttribute(RedirectTargetResolver.REDIRECT_URI_COOKIE, hint.trim());
			} else {
				// 명시 파라미터가 없으면 기존 힌트를 정리(오염 방지)
				if (req.getSession(false) != null) {
					req.getSession(false).removeAttribute(RedirectTargetResolver.REDIRECT_URI_COOKIE);
				}
				// 퍼블릭 쿠키로 남아 있을 수도 있으니 같이 제거
				var del = DynamicCookieSupport.asPublic(
						DynamicCookieSupport.newCookie(RedirectTargetResolver.REDIRECT_URI_COOKIE, "", req))
					.maxAge(Duration.ZERO).build();
				res.addHeader(HttpHeaders.SET_COOKIE, del.toString());

				var delAlt = DynamicCookieSupport.asPublic(
						DynamicCookieSupport.newCookie(RedirectTargetResolver.ALT_REDIRECT_URI_COOKIE, "", req))
					.maxAge(Duration.ZERO).build();
				res.addHeader(HttpHeaders.SET_COOKIE, delAlt.toString());
			}
		}
		chain.doFilter(req, res);
	}
}
