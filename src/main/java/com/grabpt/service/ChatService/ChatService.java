package com.grabpt.service.ChatService;

import com.grabpt.domain.entity.Messages;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;

import java.util.List;

public interface ChatService {
	//public Messages saveMessage(Long roomId, MessageRequest.messageRequestDto request);
	public ChatResponse.CreateChatRoomResponseDto getOrcreateChatRoom(ChatRequest.CreateChatRoomRequestDto request);
	public Messages createChatMessage(ChatRequest.MessageRequestDto request);
	public List<ChatResponse.MessageResponseDto> getMessagesByChatRoom(Long roomId);
	public List<ChatResponse.ChatRoomPreviewDto> getChatRoomList(Long userId);
	public Long getLastUnReadMessageId(Long roomId, Long userId);
	public Long getUnreadMessageCount(Long roomId, Long userId);
	public void updateLastReadMessage(Long roomId, Long userId);
}
