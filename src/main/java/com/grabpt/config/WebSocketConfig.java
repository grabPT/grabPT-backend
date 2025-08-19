package com.grabpt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/subscribe");
		config.setApplicationDestinationPrefixes("/publish");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws-connect")
			.setAllowedOriginPatterns(
				"http://localhost:5173",
				"http://127.0.0.1:5173",
				"https://grabpt.com",
				"https://www.grabpt.com",
				"https://api.grabpt.com",
				"*"
			)
			.withSockJS();
	}
}
