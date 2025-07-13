package com.grabpt.config.jwt;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.grabpt.config.jwt.properties.Constants;

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

		String token = resolveToken(request);

		if (!StringUtils.hasText(token)) {
			filterChain.doFilter(request, response);
			return;
		}

		if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
			Authentication authentication = jwtTokenProvider.getAuthentication(token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		Authentication authentication = jwtTokenProvider.getAuthentication(token);
		log.info("JWT 인증 객체 생성: {}", authentication);
		log.info("Authorities: {}", authentication.getAuthorities());
		log.info("isAuthenticated: {}", authentication.isAuthenticated());

		filterChain.doFilter(request, response);

		log.info("SecurityContext에 인증 저장 완료: {}", SecurityContextHolder.getContext().getAuthentication());
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(Constants.AUTH_HEADER);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(Constants.TOKEN_PREFIX)) {
			return bearerToken.substring(Constants.TOKEN_PREFIX.length());
		}
		return null;
	}

}

