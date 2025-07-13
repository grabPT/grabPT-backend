package com.grabpt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.grabpt.config.auth.PrincipalDetailsService;
import com.grabpt.config.jwt.JwtAuthenticationFilter;
import com.grabpt.config.jwt.JwtTokenProvider;
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
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(principalDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
				UsernamePasswordAuthenticationFilter.class)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/user/**").authenticated()
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.requestMatchers("/swagger", "/swagger-ui.html", "/swagger-ui/**", "/api-docs", "/api-docs/**",
					"/v3/api-docs/**").permitAll()
				.requestMatchers("/api/**").permitAll()
				.anyRequest().permitAll()
			)
			.authenticationProvider(authenticationProvider())
			.oauth2Login(oauth2 -> oauth2
				// .loginPage("/login")
				.userInfoEndpoint(userInfo -> userInfo
					.userService(principalOauth2UserService)
				)
				.successHandler(oauth2SuccessHandler)
			);
		return http.build();
	}
}
