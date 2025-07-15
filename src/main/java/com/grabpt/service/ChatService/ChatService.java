package com.grabpt.service.ChatService;

import com.grabpt.domain.entity.Messages;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;

public interface ChatService {
	//public Messages saveMessage(Long roomId, MessageRequest.messageRequestDto request);
	public ChatResponse.CreateChatRoomResponseDto getOrcreateChatRoom(ChatRequest.CreateChatRoomRequestDto request);
}
