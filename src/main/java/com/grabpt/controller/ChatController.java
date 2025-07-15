package com.grabpt.controller;

import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.domain.entity.Messages;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;
import com.grabpt.service.ChatService.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/ws")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	@PostMapping("/chatRoom/request")
	public ChatResponse.CreateChatRoomResponseDto createChatRoom(@RequestBody ChatRequest.CreateChatRoomRequestDto request){
		return chatService.getOrcreateChatRoom(request);
	}
}
