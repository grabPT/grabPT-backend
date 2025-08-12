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
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))  // 여기가 핵심
			.csrf(csrf -> csrf.disable())
			.formLogin(AbstractHttpConfigurer::disable)
			.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)  //  유지
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/auth/api/temp-info").permitAll() // 소셜로그인 get 요청 엔드포인트
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers("/ws-connect/**").permitAll()
				.requestMatchers("/user/**").authenticated()
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.requestMatchers("/swagger", "/swagger-ui.html", "/swagger-ui/**",
					"/api-docs", "/api-docs/**", "/v3/api-docs/**").permitAll()
				.requestMatchers("/auth/api/temp-info").permitAll()
				.requestMatchers("/api/**").permitAll()
				.anyRequest().permitAll()
			)
			.headers(h -> h
				.frameOptions(f -> f.disable()) // X-Frame-Options 제거
				.contentSecurityPolicy(csp -> csp
					.policyDirectives("frame-ancestors https://www.grabpt.com https://grabpt.com")
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
		configuration.setAllowedOrigins(List.of(
			"http://localhost:5173",
			"http://43.203.91.190",
			"http://43.203.91.190:8080",
			"http://grabpt.com",
			"https://grabpt.com",
			"https://www.grabpt.com",
			"wws://api.grabpt.com",
			"https://api.grabpt.com"    // 여기에 추가 필요
		));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
