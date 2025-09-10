package com.grabpt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.grabpt.config.auth.PrincipalDetailsService;
import com.grabpt.config.jwt.CsrfOriginFilter;
import com.grabpt.config.jwt.JwtAuthenticationFilter;
import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.config.oauth.PrincipalOauth2UserService;
import com.grabpt.config.oauth.handler.OAuth2FailureHandler;
import com.grabpt.config.oauth.handler.OAuth2SuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final PrincipalOauth2UserService principalOauth2UserService;
	private final PrincipalDetailsService principalDetailsService;
	private final JwtTokenProvider jwtTokenProvider;
	private final OAuth2SuccessHandler oauth2SuccessHandler;
	private final OAuth2FailureHandler oauth2FailureHandler;

	@Bean
	@Order(0)
	public SecurityFilterChain wsFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher(req -> {
			String uri = req.getRequestURI();
			if (uri == null)
				return false;
			String norm = uri.replaceAll("/{2,}", "/");
			return norm.startsWith("/ws-connect/");
		});
		http.authorizeHttpRequests(a -> a.anyRequest().permitAll())
			.csrf(AbstractHttpConfigurer::disable)
			.cors(AbstractHttpConfigurer::disable)
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		return http.build();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// OAuth2 AuthorizationRequest를 세션에 저장 (쿠키 의존 제거)
	@Bean
	public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
		return new HttpSessionOAuth2AuthorizationRequestRepository();
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(jwtTokenProvider);
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(principalDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	@Bean
	@Order(1)
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.securityContext(sc -> sc.securityContextRepository(
				new org.springframework.security.web.context.NullSecurityContextRepository()))
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.addFilterBefore(new CsrfOriginFilter(), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers("/ws-connect/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/reviews/**").permitAll()
				// 공개 엔드포인트들
				.requestMatchers(
					"/favicon.ico",
					"/api/auth/reissue", "/api/auth/logout",
					"/api/auth/user-signup", "/api/auth/pro-signup",
					"/api/auth/check-nickname", "/api/auth/api/temp-info",
					"/swagger", "/swagger-ui.html", "/swagger-ui/**",
					"/api-docs", "/api-docs/**", "/v3/api-docs/**",
					"/api/v1/**", "/api/users/**", "/api/auth/**",
					"/matching/**", "/payment/**", "/api/sms/**",
					"/api/category-proprofile/**", "/api/*/reviews",
					"/api/alarmList", "/api/auth/reissue",
					// 실패 URL을 프론트로 보내지 않는다 해도, 기본 실패 URL을 쓴다면 허용
					"/login", "/login/**"
				).permitAll()
				.requestMatchers("/mypage", "/mypage/**").authenticated()
				.anyRequest().authenticated()
			)
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint(
					new org.springframework.security.web.authentication.HttpStatusEntryPoint(
						org.springframework.http.HttpStatus.UNAUTHORIZED))
				.accessDeniedHandler((req, res, e) -> res.setStatus(403))
			)
			.headers(h -> h
				.frameOptions(f -> f.disable())
				.contentSecurityPolicy(csp -> csp.policyDirectives(
					"frame-ancestors https://www.grabpt.com https://grabpt.com https://api.grabpt.com"))
			)
			.authenticationProvider(authenticationProvider())
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo.userService(principalOauth2UserService))
				.authorizationEndpoint(a -> a.authorizationRequestRepository(authorizationRequestRepository()))
				.successHandler(oauth2SuccessHandler)
				.failureHandler(oauth2FailureHandler) // 있으면 유지, 없으면 생략 가능
			);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(java.util.List.of(
			"https://www.grabpt.com", "https://grabpt.com",
			"http://localhost:5173", "http://127.0.0.1:5173",
			"http://localhost:3000", "http://127.0.0.1:3000"
		));
		configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		configuration.setAllowedHeaders(java.util.List.of("*"));
		configuration.setAllowCredentials(true);
		configuration.setExposedHeaders(
			java.util.List.of("Authorization", "Location", "Content-Disposition", "Set-Cookie"));
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
