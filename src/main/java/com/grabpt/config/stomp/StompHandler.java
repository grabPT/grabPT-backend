//package com.grabpt.config.stomp;
//
//import com.grabpt.config.jwt.JwtTokenProvider;
//import com.grabpt.service.ChatService.ChatService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class StompHandler implements ChannelInterceptor {
//
//	private final JwtTokenProvider jwtTokenProvider;
//	private final UserDetailsService userDetailsService;
//
//	@Override
//	public Message<?> preSend(Message<?> message, MessageChannel channel) {
//		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//
//		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//			String token = accessor.getFirstNativeHeader("Authorization");
//
//			if (token != null && token.startsWith("Bearer ")) {
//				token = token.substring(7); // Bearer 제거
//
//				if (jwtTokenProvider.validateToken(token)) {
//					String username = jwtTokenProvider.getUserEmail(token);
//					UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//					UsernamePasswordAuthenticationToken auth =
//						new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//
//					accessor.setUser(auth); // 여기서 Principal 설정!
//				}
//			}
//		}
//
//		return message;
//	}
//}
//
