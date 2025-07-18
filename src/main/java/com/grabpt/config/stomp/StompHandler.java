//package com.grabpt.config.stomp;
//
//import com.grabpt.service.ChatService.ChatService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class StompHandler implements ChannelInterceptor {
//
//	private final ChatService chatService;
//
//	public Message<?> preSend(Message<?> message, MessageChannel channel) {
//
//		return message;
//	}
//
//}
