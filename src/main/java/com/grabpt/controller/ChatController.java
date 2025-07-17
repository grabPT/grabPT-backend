package com.grabpt.controller;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.converter.ChatConverter;
import com.grabpt.domain.entity.Messages;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;
import com.grabpt.service.ChatService.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;
	private final SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/chat/{roomId}")
	public void sendMessage(@DestinationVariable Long roomId, ChatRequest.MessageRequestDto request, Principal principal){
		Messages newMessage = chatService.createChatMessage(request);
		ChatResponse.MessageResponseDto response = ChatConverter.toMessageResponseDto(newMessage);
		log.info("채팅 메시지 브로드캐스트", response.getContent());
		messagingTemplate.convertAndSend("/subscribe/chat/"+roomId, response);
		//return ChatConverter.toMessageResponseDto(newMessage);
	}

	@PostMapping("/chatRoom/request")
	@ResponseBody
	public ApiResponse<ChatResponse.CreateChatRoomResponseDto> createChatRoom(@RequestBody ChatRequest.CreateChatRoomRequestDto request){
		return ApiResponse.onSuccess(chatService.getOrcreateChatRoom(request));
	}

	@GetMapping("/chatRoom/{roomId}/messages")
	@ResponseBody
	public ApiResponse<List<ChatResponse.MessageResponseDto>> getMessagesByChatRoom(@PathVariable(name = "roomId") Long roomId){
		return ApiResponse.onSuccess(chatService.getMessagesByChatRoom(roomId));
	}

	@GetMapping("/chatRoom/list") //로그인 유저 정보
	@ResponseBody
	public ApiResponse<List<ChatResponse.ChatRoomPreviewDto>> getChatRoomList(@RequestParam(name = "userId") Long userId){ //임
		return ApiResponse.onSuccess(chatService.getChatRoomList(userId));
	}

	@GetMapping("/chat-test")
	public String chatTest(){
		return "chat-test";
	}
}
