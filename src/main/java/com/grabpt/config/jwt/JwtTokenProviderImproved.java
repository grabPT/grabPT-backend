package com.grabpt.config.jwt;

import static com.grabpt.config.jwt.properties.Constants.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.grabpt.config.auth.PrincipalDetails;
import com.grabpt.config.auth.PrincipalDetailsService;
import com.grabpt.config.jwt.properties.CookieManager;
import com.grabpt.config.jwt.properties.JwtProperties;
import com.grabpt.domain.entity.Users;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProviderImproved {

	private final JwtProperties jwtProperties;
	private final PrincipalDetailsService principalDetailsService;

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
	}

	// ==================== 토큰 생성 ====================

	public String generateToken(Authentication authentication) {
		String email = ((PrincipalDetails)authentication.getPrincipal()).getUser().getEmail();

		return Jwts.builder()
			.setSubject(email)
			.claim("role", authentication.getAuthorities().iterator().next().getAuthority())
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration().getAccess()))
			.signWith(getSigningKey(), SignatureAlgorithm.HS256)
			.compact();
	}

	public String generateToken(Users user) {
		return Jwts.builder()
			.setSubject(user.getEmail())
			.claim("role", user.getRole().name())
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration().getAccess()))
			.signWith(getSigningKey(), SignatureAlgorithm.HS256)
			.compact();
	}

	public String createRefreshToken(String email) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + REFRESH_TOKEN_VALIDITY);

		return Jwts.builder()
			.setSubject(email)
			.setIssuedAt(now)
			.setExpiration(expiry)
			.signWith(getSigningKey(), SignatureAlgorithm.HS256)
			.compact();
	}

	// ==================== 토큰 검증 ====================

	public boolean validateToken(String token) {
		if (!StringUtils.hasText(token)) {
			return false;
		}
		try {
			Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token);
			return true;
		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			log.debug("Expired JWT token");
			return false;
		} catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
			log.debug("Invalid JWT token: {}", e.getMessage());
			return false;
		}
	}

	// ==================== 인증 정보 추출 ====================

	public Authentication getAuthentication(String token) {
		if (!StringUtils.hasText(token)) {
			throw new org.springframework.security.authentication.BadCredentialsException("Missing token");
		}

		Claims claims = Jwts.parserBuilder()
			.setSigningKey(getSigningKey())
			.build()
			.parseClaimsJws(token)
			.getBody();

		String email = claims.getSubject();
		UserDetails userDetails = principalDetailsService.loadUserByUsername(email);
		return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
	}

	public String getUserEmail(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(getSigningKey())
			.build()
			.parseClaimsJws(token)
			.getBody()
			.getSubject();
	}

	// ==================== 토큰 추출 (개선됨) ====================

	/**
	 * HTTP 요청에서 JWT 토큰 추출
	 * 우선순위: 1) Authorization 헤더, 2) 쿠키
	 */
	public static String resolveToken(HttpServletRequest request) {
		// 1. Authorization 헤더에서 Bearer 토큰 확인
		String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
			return bearerToken.substring(TOKEN_PREFIX.length());
		}

		// 2. 쿠키에서 액세스 토큰 확인 (CookieManager 사용)
		String cookieToken = CookieManager.getAccessToken(request);
		if (StringUtils.hasText(cookieToken)) {
			return cookieToken;
		}

		return null;
	}

	/**
	 * 요청에서 인증 정보 추출
	 */
	public Authentication extractAuthentication(HttpServletRequest request) {
		String accessToken = resolveToken(request);
		if (!validateToken(accessToken)) {
			return null;
		}
		return getAuthentication(accessToken);
	}
}
