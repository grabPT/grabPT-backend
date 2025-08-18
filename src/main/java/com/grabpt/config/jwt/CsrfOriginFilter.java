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
		"https://api.grabpt.com",
		"http://localhost:5173",
		"https://localhost:5173",
		"http://localhost:3000",
		"https://localhost:3000"
	);

	private static final Set<String> STATE_CHANGING = Set.of("POST", "PUT", "PATCH", "DELETE", "OPTIONS");

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
		throws ServletException, IOException {

		String method = req.getMethod();

		// 1) Preflight는 무조건 통과
		if ("OPTIONS".equalsIgnoreCase(method)) {
			chain.doFilter(req, res);
			return;
		}

		// 2) 상태 변경이 아니면 통과
		if (!STATE_CHANGING.contains(method)) {
			chain.doFilter(req, res);
			return;
		}

		// 3) 브라우저가 아닌 요청(Origin/Referer 둘 다 없음)은 통과
		String origin = req.getHeader("Origin");
		String referer = req.getHeader("Referer");
		if ((origin == null || origin.isBlank()) && (referer == null || referer.isBlank())) {
			chain.doFilter(req, res);
			return;
		}

		// 4) 같은 호스트(동일 도메인)에서 온 Referer는 허용 (Swagger 등)
		String self = req.getScheme() + "://" + req.getServerName()
			+ (req.getServerPort() == 80 || req.getServerPort() == 443 ? "" : ":" + req.getServerPort());
		if (referer != null && referer.startsWith(self)) {
			chain.doFilter(req, res);
			return;
		}

		// 5) 화이트리스트 검사
		boolean ok = (origin != null && TRUSTED.contains(origin))
			|| (referer != null && TRUSTED.stream().anyMatch(referer::startsWith));

		if (!ok) {
			res.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		chain.doFilter(req, res);
	}
}
