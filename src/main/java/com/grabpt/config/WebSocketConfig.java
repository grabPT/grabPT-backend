package com.grabpt.config;

import com.grabpt.config.stomp.StompHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket/STOMP 설정
 * Issue #475: StompHandler(JWT 인터셉터)를 configureClientInboundChannel에 등록
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final StompHandler stompHandler;

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
				"http://localhost:8080",
				"http://localhost:8081",
				"http://127.0.0.1:5173",
				"https://grabpt.com",
				"https://www.grabpt.com",
				"https://api.grabpt.com",
				"*"
			)
			.withSockJS();
	}

	/**
	 * STOMP CONNECT 시 JWT 인증을 수행하는 StompHandler를 인바운드 채널 인터셉터로 등록
	 */
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(stompHandler);
	}
}
