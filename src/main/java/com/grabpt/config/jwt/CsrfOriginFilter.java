package com.grabpt.config.jwt;

import java.io.IOException;
import java.util.Set;

import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CsrfOriginFilter extends OncePerRequestFilter {

	private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

	private static final Set<String> TRUSTED = Set.of(
		"https://grabpt.com",
		"https://www.grabpt.com",
		"https://api.grabpt.com",
		"http://localhost:5173",
		"https://localhost:5173",
		"http://localhost:3000",
		"https://localhost:3000"
	);

	// 상태 변경만 검사 (OPTIONS는 프리플라이트라서 제외하는 편이 낫습니다)
	private static final Set<String> STATE_CHANGING = Set.of("POST", "PUT", "PATCH", "DELETE");

	// 제외 경로는 아예 필터 미적용
	@Override
	protected boolean shouldNotFilter(HttpServletRequest req) {
		String uri = req.getRequestURI();
		if (uri == null)
			return false;
		String norm = uri.replaceAll("/{2,}", "/");  // // -> /
		return norm.startsWith("/ws-connect/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
		throws ServletException, IOException {

		String method = req.getMethod();

		// 1) 프리플라이트는 무조건 통과
		if ("OPTIONS".equalsIgnoreCase(method)) {
			chain.doFilter(req, res);
			return;
		}

		// 2) 상태 변경이 아니면 통과 (GET/HEAD 등은 검사 안 함)
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

		// 4) 동일 호스트(자기 자신에서 온 Referer)는 허용
		String self = req.getScheme() + "://" + req.getServerName()
			+ ((req.getServerPort() == 80 || req.getServerPort() == 443) ? "" : ":" + req.getServerPort());
		if (referer != null && referer.startsWith(self)) {
			chain.doFilter(req, res);
			return;
		}

		// 5) 화이트리스트 검사
		boolean ok = (origin != null && TRUSTED.contains(origin))
			|| (referer != null && TRUSTED.stream().anyMatch(referer::startsWith));

		if (!ok) {
			// 차단 응답에도 CORS 헤더를 달아 브라우저가 'CORS 에러'로 오해하지 않게
			if (origin != null && TRUSTED.contains(origin)) {
				res.setHeader("Access-Control-Allow-Origin", origin);
				res.setHeader("Vary", "Origin");
				res.setHeader("Access-Control-Allow-Credentials", "true");
			}
			res.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		chain.doFilter(req, res);
	}

}
