package com.grabpt.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.grabpt.config.auth.PrincipalDetailsService;
import com.grabpt.config.jwt.CsrfOriginFilter;
import com.grabpt.config.jwt.JwtAuthenticationFilter;
import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.config.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.grabpt.config.oauth.PrincipalOauth2UserService;
import com.grabpt.config.oauth.handler.OAuth2SuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final PrincipalOauth2UserService principalOauth2UserService;
	private final PrincipalDetailsService principalDetailsService;
	private final JwtTokenProvider jwtTokenProvider;
	private final OAuth2SuccessHandler oauth2SuccessHandler;

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
		var repo = new HttpCookieOAuth2AuthorizationRequestRepository();
		org.slf4j.LoggerFactory.getLogger(SecurityConfig.class)
			.warn("[SECURITY] Using {}", repo.getClass().getName());
		return repo;
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(jwtTokenProvider);
	}

	@Bean
	public CorsFilter corsFilter() {
		return new CorsFilter(corsConfigurationSource());
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(principalDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)

			.addFilterBefore(new CsrfOriginFilter(), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

			.authorizeHttpRequests(auth -> auth
				// 1) 공개 엔드포인트(화이트리스트) — 반드시 위쪽에!
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

				// 온보딩/인증 관련
				.requestMatchers(
					"/api/auth/reissue",
					"/api/auth/logout",
					"/api/auth/user-signup",
					"/api/auth/pro-signup",
					"/api/auth/check-nickname",
					"/api/auth/api/temp-info",
					"/swagger", "/swagger-ui.html", "/swagger-ui/**",
					"/api-docs", "/api-docs/**", "/v3/api-docs/**",
					"/ws-connect/**",
					"/api/v1/**",
					"/api/users/**",
					"/api/auth/**",
					"/matching/**",
					"/payment/**",
					"/api/sms/**",
					"/api/category-proprofile/**",
					"/api/*/reviews"
				).permitAll()
				.requestMatchers("/mypage", "/mypage/**").authenticated()
				.anyRequest().authenticated()
			)

			// 401/403 명확화
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint(new org.springframework.security.web.authentication.HttpStatusEntryPoint(
					org.springframework.http.HttpStatus.UNAUTHORIZED)) // 인증 없음 → 401
				.accessDeniedHandler((req, res, e) -> res.setStatus(403)) // 인증됐지만 권한 없음 → 403
			)

			.headers(h -> h
				.frameOptions(f -> f.disable())
				.contentSecurityPolicy(csp -> csp
					.policyDirectives(
						"frame-ancestors https://www.grabpt.com https://grabpt.com https://api.grabpt.com")
				)
			)
			.authenticationProvider(authenticationProvider())
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo.userService(principalOauth2UserService))
				.authorizationEndpoint(a -> a.authorizationRequestRepository(authorizationRequestRepository()))
				.successHandler(oauth2SuccessHandler)
			);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// ✔ 패턴 기반(서브도메인/포트 허용)
		configuration.setAllowedOriginPatterns(List.of(
			"https://www.grabpt.com",
			"http://api.grabpt.com",
			"https://grabpt.com",
			"http://grabpt.com",
			"http://localhost:5137",
			"http://43.203.91.190:8080"
		));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}

