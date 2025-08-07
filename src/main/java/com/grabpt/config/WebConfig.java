package com.grabpt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**") // 또는 "/**" 전체 허용도 가능
			.allowedOrigins("http://localhost:5173")
			.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
			.allowCredentials(true);
	}
}
