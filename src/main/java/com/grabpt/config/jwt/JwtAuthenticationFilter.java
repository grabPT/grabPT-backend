package com.grabpt.config.jwt;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain)
		throws ServletException, IOException {

		String token = JwtTokenProviderImproved.resolveToken(request);

		// 토큰 없거나 무효 → 인증 세팅 없이 통과 (예외 절대 금지)
		if (!StringUtils.hasText(token) || !jwtTokenProvider.validateToken(token)) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
				Authentication authentication = jwtTokenProvider.getAuthentication(token);
				SecurityContextHolder.getContext().setAuthentication(authentication);
				log.debug("JWT 인증 완료: isAuthenticated={}, authorities={}",
					authentication.isAuthenticated(), authentication.getAuthorities());
			} else {
				// 토큰 없음/무효 -> 인증 세팅 없이 통과 (예외/응답쓰기 금지)
				log.debug("JWT 토큰 없음 또는 무효. 체인 계속.");
			}
		} catch (Exception e) {
			// 예외 발생 시 인증 컨텍스트 정리하고 그냥 체인 계속
			SecurityContextHolder.clearContext();
			log.warn("JWT 처리 중 예외: {}", e.getMessage());
		} finally {
			filterChain.doFilter(request, response);
		}
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest req) {
		String uri = req.getRequestURI();
		if (uri == null)
			return false;
		String norm = uri.replaceAll("/{2,}", "/");  // // -> /
		return norm.startsWith("/ws-connect/");
	}
}

