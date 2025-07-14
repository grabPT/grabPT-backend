package com.grabpt.config.jwt;

import static com.grabpt.config.jwt.properties.Constants.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.grabpt.config.auth.PrincipalDetails;
import com.grabpt.config.auth.PrincipalDetailsService;
import com.grabpt.config.jwt.properties.Constants;
import com.grabpt.config.jwt.properties.JwtProperties;
import com.grabpt.domain.entity.Users;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private final JwtProperties jwtProperties;
	private final PrincipalDetailsService principalDetailsService;

	// jwtProperties.getSecretKey()를 byte[]로 변환하여 HMAC-SHA에 사용할 수 있는 Key 객체를 생성
	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
	}

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
			.claim("role", user.getRole().name()) // enum인 경우 .name() 권장
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration().getAccess()))
			.signWith(getSigningKey(), SignatureAlgorithm.HS256)
			.compact();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public Authentication getAuthentication(String token) {
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(getSigningKey())
			.build()
			.parseClaimsJws(token)
			.getBody();

		String email = claims.getSubject();
		String role = claims.get("role", String.class);

		UserDetails userDetails = principalDetailsService.loadUserByUsername(email);
		log.info("Authorities 검증" + userDetails.getAuthorities().toString());
		return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());

	}

	public static String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(Constants.AUTH_HEADER);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(Constants.TOKEN_PREFIX)) {
			return bearerToken.substring(Constants.TOKEN_PREFIX.length());
		}
		return null;
	}

	public Authentication extractAuthentication(HttpServletRequest request) throws IllegalAccessException {
		String accessToken = resolveToken(request);
		if (accessToken == null || !validateToken(accessToken)) {
			throw new IllegalAccessException("token null");
		}
		return getAuthentication(accessToken);
	}

	public String createRefreshToken(String email) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + REFRESH_TOKEN_VALIDITY); // 예: 7일

		return Jwts.builder()
			.setSubject(email)
			.setIssuedAt(now)
			.setExpiration(expiry)
			.signWith(getSigningKey(), SignatureAlgorithm.HS512)
			.compact();
	}

	public String getUserEmail(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(getSigningKey())
			.build()
			.parseClaimsJws(token)
			.getBody()
			.getSubject();
	}

}
