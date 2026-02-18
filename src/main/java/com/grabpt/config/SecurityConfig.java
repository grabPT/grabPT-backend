package com.grabpt.config;

import java.util.List;

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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.grabpt.config.auth.PrincipalDetailsService;
import com.grabpt.config.jwt.CsrfOriginFilter;
import com.grabpt.config.jwt.JwtAuthenticationFilter;
import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.config.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.grabpt.config.oauth.PrincipalOauth2UserService;
import com.grabpt.config.oauth.handler.OAuth2FailureHandler;
import com.grabpt.config.oauth.handler.OAuth2SuccessHandlerImproved;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final PrincipalOauth2UserService principalOauth2UserService;
	private final PrincipalDetailsService principalDetailsService;
	private final JwtTokenProvider jwtTokenProvider;
	private final OAuth2SuccessHandlerImproved oauth2SuccessHandler;
	private final OAuth2FailureHandler oAuth2FailureHandler;

	@Bean
	@Order(0)
	public SecurityFilterChain wsFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher(req -> {
			String uri = req.getRequestURI();            // 예: //ws-connect/info
			if (uri == null)
				return false;
			String norm = uri.replaceAll("/{2,}", "/");  // // -> /
			return norm.startsWith("/ws-connect/");
		});
		http.authorizeHttpRequests(a -> a.anyRequest().permitAll())
			.csrf(AbstractHttpConfigurer::disable)
			.cors(AbstractHttpConfigurer::disable)
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		// 여기에는 커스텀 필터(JWT/CSRF) 절대 추가 X
		return http.build();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public HttpCookieOAuth2AuthorizationRequestRepository authRequestRepository() {
		return new HttpCookieOAuth2AuthorizationRequestRepository();
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
			.securityContext(sc -> sc
				.securityContextRepository(new org.springframework.security.web.context.NullSecurityContextRepository())
			)

			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)

			.addFilterBefore(new CsrfOriginFilter(), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

			.authorizeHttpRequests(auth -> auth
				// 1) 공개 엔드포인트(화이트리스트) — 반드시 위쪽에!
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers("/ws-connect/**").permitAll()

				.requestMatchers(HttpMethod.GET, "/reviews/**").permitAll()

				// 온보딩/인증 관련
				.requestMatchers(
					"/favicon.ico",
					"/api/auth/reissue",
					"/api/auth/logout",
					"/api/auth/user-signup",
					"/api/auth/pro-signup",
					"/api/auth/check-nickname",
					"/api/auth/api/temp-info",
					"/swagger", "/swagger-ui.html", "/swagger-ui/**",
					"/api-docs", "/api-docs/**", "/v3/api-docs/**",
					"/api/v1/**",
					"/api/users/**",
					"/api/auth/**",
					"/matching/**",
					"/payment/**",
					"/api/sms/**",
					"/api/category-proprofile/**",
					"/api/*/reviews",
					"/api/allAlarmList",
					"/api/unreadAlarmList",
					"/api/auth/reissue",
					"/login/**"
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
				.authorizationEndpoint(a -> a.authorizationRequestRepository(authRequestRepository()))
				.successHandler(oauth2SuccessHandler)
				.failureHandler(oAuth2FailureHandler)
			);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// 패턴 기반(서브도메인/포트 허용)
		configuration.setAllowedOriginPatterns(List.of(
			"https://www.grabpt.com",
			"https://grabpt.com",
			// --- local dev ---
			"http://localhost:5173",
			"http://127.0.0.1:5173",
			"http://localhost:3000",
			"http://127.0.0.1:3000",
			"https://grabpt-dev.vercel.app",
			"https://dev-grabpt.vercel.app"
		));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);
		configuration.setExposedHeaders(List.of("Authorization", "Location", "Content-Disposition", "Set-Cookie"));
		configuration.setMaxAge(3600L);
		configuration.addAllowedOrigin("http://localhost:5173");
		configuration.addAllowedOrigin("http://localhost:8080");

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
