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
import com.grabpt.config.jwt.properties.JwtProperties;
import com.grabpt.domain.entity.Users;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
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
		if (!StringUtils.hasText(token))
			return false;  // null/empty 즉시 false
		try {
			Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
			return true;
		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			// 만료도 false (필터/EntryPoint가 401 처리)
			return false;
		} catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public Authentication getAuthentication(String token) {
		if (!StringUtils.hasText(token)) {
			// 컨트롤러 등에서 직접 호출돼도 500 안 나게 방어
			throw new org.springframework.security.authentication.BadCredentialsException("Missing token");
		}
		Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
			.parseClaimsJws(token).getBody();

		String email = claims.getSubject();
		UserDetails userDetails = principalDetailsService.loadUserByUsername(email);
		return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());

	}

	public static String resolveToken(HttpServletRequest req) {
		// 1) Authorization: Bearer ...
		String bearer = req.getHeader(HttpHeaders.AUTHORIZATION);
		if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
			return bearer.substring(7);
		}
		// 2) Cookie (신/구 이름 모두)
		if (req.getCookies() != null) {
			for (Cookie c : req.getCookies()) {
				if ("ACCESS_TOKEN".equals(c.getName()) || "accessToken".equals(c.getName())) {
					return c.getValue();
				}
			}
		}
		return null;
	}

	public Authentication extractAuthentication(HttpServletRequest request) {
		String accessToken = resolveToken(request);
		if (!validateToken(accessToken))
			return null;  // 무효/없음이면 null
		return getAuthentication(accessToken);
	}

	public String createRefreshToken(String email) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + REFRESH_TOKEN_VALIDITY); // 예: 7일

		return Jwts.builder()
			.setSubject(email)
			.setIssuedAt(now)
			.setExpiration(expiry)
			.signWith(getSigningKey(), SignatureAlgorithm.HS256)
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
