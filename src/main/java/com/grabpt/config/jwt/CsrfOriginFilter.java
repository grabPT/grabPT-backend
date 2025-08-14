package com.grabpt.config.jwt;

import java.io.IOException;
import java.util.Set;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CsrfOriginFilter extends OncePerRequestFilter {
	private static final Set<String> TRUSTED = Set.of(
		"https://grabpt.com",
		"https://www.grabpt.com",
		"https://api.grabpt.com"
	);

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
		throws ServletException, IOException {
		String m = req.getMethod();
		boolean stateChanging = "POST".equals(m) || "PUT".equals(m) || "PATCH".equals(m) || "DELETE".equals(m);
		if (stateChanging) {
			String origin = req.getHeader("Origin");
			String referer = req.getHeader("Referer");
			boolean ok = (origin != null && TRUSTED.contains(origin))
				|| (referer != null && TRUSTED.stream().anyMatch(referer::startsWith));
			if (!ok) {
				res.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
		}
		chain.doFilter(req, res);
	}
}
