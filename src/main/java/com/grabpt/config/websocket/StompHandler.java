package com.grabpt.config.websocket;

import com.grabpt.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * STOMP CONNECT 시점에 JWT 토큰을 검증하고 Principal을 설정하는 인터셉터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserDetailsService userDetailsService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			String token = accessor.getFirstNativeHeader("Authorization");

			if (token != null && token.startsWith("Bearer ")) {
				token = token.substring(7);

				if (jwtTokenProvider.validateToken(token)) {
					String username = jwtTokenProvider.getUserEmail(token);
					UserDetails userDetails = userDetailsService.loadUserByUsername(username);
					UsernamePasswordAuthenticationToken auth =
						new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

					accessor.setUser(auth); // WebSocket Principal 설정
					log.debug("[STOMP] JWT 인증 성공: {}", username);
				} else {
					log.warn("[STOMP] JWT 토큰 유효하지 않음");
				}
			} else {
				log.warn("[STOMP] Authorization 헤더 없음 (비인증 연결 허용)");
			}
		}

		return message;
	}
}
