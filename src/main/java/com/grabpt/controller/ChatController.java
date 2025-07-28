package com.grabpt.controller;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.converter.ChatConverter;
import com.grabpt.domain.entity.Messages;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;
import com.grabpt.service.ChatService.ChatService;
import com.grabpt.service.UserService.UserQueryService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;
	private final UserQueryService userQueryService;
	private final SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/chat/{roomId}")
	public void sendMessage(@DestinationVariable Long roomId, ChatRequest.MessageRequestDto request, Principal principal){
		Messages newMessage = chatService.createChatMessage(request);
		ChatResponse.MessageResponseDto response = ChatConverter.toMessageResponseDto(newMessage);
		log.info("채팅 메시지 브로드캐스트", response.getContent());
		messagingTemplate.convertAndSend("/subscribe/chat/"+roomId, response);
		//return ChatConverter.toMessageResponseDto(newMessage);
	}

	@PostMapping("/chatRoom/{roomId}/readWhenExist")
	@ResponseBody
	public ApiResponse<String> updateLastReadMessageWhenExist(@PathVariable Long roomId, HttpServletRequest request) throws IllegalAccessException {
		Long userId = userQueryService.getUserId(request);
		chatService.updateLastReadMessageWhenExist(roomId,userId);
		log.info("마지막으로 읽은 메시지 업데이트");
		return ApiResponse.onSuccess("채팅방을 나갑니다. 마지막으로 읽은 메시지 업데이트");
	}

	@PostMapping("/chatRoom/{roomId}/readWhenEnter")
	@ResponseBody
	public ApiResponse<String> updateLastReadMessageWhenEnter(@PathVariable Long roomId, HttpServletRequest request) throws IllegalAccessException {
		Long userId = userQueryService.getUserId(request);
		chatService.updateLastReadMessageWhenEnter(roomId, userId);  // 한명만 있을 때 처리 서비스 호출
		return ApiResponse.onSuccess("한 명만 접속 상태 읽음 처리 완료");
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
	public ApiResponse<List<ChatResponse.ChatRoomPreviewDto>> getChatRoomList(@RequestParam(name = "keyword", required = false) String keyword,
																			  HttpServletRequest request) throws IllegalAccessException {
		Long userId = userQueryService.getUserId(request);
		return ApiResponse.onSuccess(chatService.getChatRoomList(userId, keyword));
	}

	@GetMapping("/chat-test")
	public String chatTest(HttpServletRequest request) throws IllegalAccessException {
		String email = userQueryService.getUserInfo(request).getEmail();
		return "chat-test";
	}
}
