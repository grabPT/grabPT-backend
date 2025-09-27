package com.grabpt.controller;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.config.SecurityUtils;
import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.converter.ChatConverter;
import com.grabpt.domain.entity.Messages;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;
import com.grabpt.service.ChatService.ChatService;
import com.grabpt.service.UserService.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
	}

	@Operation(
		description = "유저가 채팅방에 접속 상태일 경우 실시간으로 메시지를 읽음 처리합니다(인증 토큰 필요, roomId는 pathVariable",
		summary = "채팅방 접속 상태일 시 메시지 읽음 처리"
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "메시지 읽음 처리 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
	})
	@PostMapping("/chatRoom/{roomId}/readWhenExist")
	@ResponseBody
	public ApiResponse<String> updateLastReadMessageWhenExist(@PathVariable Long roomId){
		Long userId = SecurityUtils.currentUserIdOrThrow();
		chatService.updateLastReadMessageWhenExist(roomId,userId);
		return ApiResponse.onSuccess("채팅방 접속 상태일 때 메시지 읽음 처리");
	}

	@Operation(
		description = "유저가 채팅방에 입장 시 읽지 않은 메시지들을 읽음 처리합니다(인증 토큰 필요, roomId는 pathVariable",
		summary = "채팅방 입장 시 메시지 읽음 처리"
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "메시지 읽음 처리 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
	})
	@PostMapping("/chatRoom/{roomId}/readWhenEnter")
	@ResponseBody
	public ApiResponse<String> updateLastReadMessageWhenEnter(@PathVariable Long roomId){
		Long userId = SecurityUtils.currentUserIdOrThrow();
		chatService.updateLastReadMessageWhenEnter(roomId, userId);
		return ApiResponse.onSuccess("채팅방 입장 시 메시지 읽음 처리");
	}

	@Operation(
		description = "request로 userId와 proId를 받아 존재하는 채팅방을 가져오거나 채팅방을 생성합니다",
		summary = "채팅방 생성 API"
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "채팅방 불러오기 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
	})
	@PostMapping("/chatRoom/request")
	@ResponseBody
	public ApiResponse<ChatResponse.CreateChatRoomResponseDto> createChatRoom(@RequestBody ChatRequest.CreateChatRoomRequestDto request){
		return ApiResponse.onSuccess(chatService.getOrcreateChatRoom(request));
	}

	@Operation(
		description = "채팅방의 최근 메시지를 20개 조회합니다. roomId를 pathVariable로 전달받고, cursor id를 requestParam으로 받습니다. 인증 필요!!",
		summary = "채팅방의 메시지 20개 조회 API (cursor기반 기본값 0)"
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "채팅방 불러오기 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ChatResponse.MessageResponseByCursorDto.class)
			)),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
	})
	@GetMapping("/chatRoom/{roomId}/messages")
	@ResponseBody
	public ApiResponse<ChatResponse.MessageResponseByCursorDto> getMessagesByChatRoom(@PathVariable(name = "roomId") Long roomId,
																					  @RequestParam(name = "cursor", required = false, defaultValue = "0") long cursor){
		List<ChatResponse.MessageResponseDto> messageResponseDto = chatService.getMessagesByChatRoom(roomId, cursor);

		long newCursor = 0;
		if (!messageResponseDto.isEmpty()) {
			newCursor = messageResponseDto.get(messageResponseDto.size() - 1).getMessageId();
		}

		ChatResponse.MessageResponseByCursorDto messageResponseByCursorDto = ChatResponse.MessageResponseByCursorDto.builder()
			.cursor(newCursor)
			.messages(messageResponseDto)
			.build();

		return ApiResponse.onSuccess(messageResponseByCursorDto);
	}

	@Operation(
		description = "QueryParameter로 keyword를 넘기면 방 이름을 기준으로 채팅방을 가져오며 keyword가 없을 시 모두 가져옵니다",
		summary = "유저가 참여하는 채팅방 리스트를 가져옵니다(필터기능 존재)"
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "채팅방 불러오기 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ChatResponse.ChatRoomPreviewDto.class)
			)),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
	})
	@GetMapping("/chatRoom/list") //로그인 유저 정보
	@ResponseBody
	public ApiResponse<List<ChatResponse.ChatRoomPreviewDto>> getChatRoomList(@RequestParam(name = "keyword", required = false) String keyword){
		Long userId = SecurityUtils.currentUserIdOrThrow();
		return ApiResponse.onSuccess(chatService.getChatRoomList(userId, keyword));
	}

	@Operation(
		summary = "유저의 전체 안읽은 메시지 개수 조회"
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "전체 안읽은 메시지 개수 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = Long.class, example = "5")
			)
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
	})
	@GetMapping("chat/unreadCount")
	@ResponseBody
	public ApiResponse<Long> getUnreadCount(){
		Long userId = SecurityUtils.currentUserIdOrThrow();
		Long allUnreadMessageCount = chatService.getAllUnreadMessageCount(userId);
		return ApiResponse.onSuccess(allUnreadMessageCount);
	}
}
