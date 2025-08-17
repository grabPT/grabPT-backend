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

		String token = JwtTokenProvider.resolveToken(request);

		// 토큰 없거나 무효 → 인증 세팅 없이 통과 (예외 절대 금지)
		if (!StringUtils.hasText(token) || !jwtTokenProvider.validateToken(token)) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			Authentication authentication = jwtTokenProvider.getAuthentication(token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			log.debug("JWT 인증 완료: isAuthenticated={}, authorities={}",
				authentication.isAuthenticated(), authentication.getAuthorities());
		} catch (Exception e) {
			// 토큰 파싱/로드 중 문제 → 인증 세팅하지 않고 통과
			log.warn("JWT 인증 설정 중 예외 발생: {}", e.getMessage());
		}

		Authentication after = SecurityContextHolder.getContext().getAuthentication();

		log.debug("요청 종료 시 SecurityContext: {}", after);
	}
	
}

