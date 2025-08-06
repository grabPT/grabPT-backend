package com.grabpt.service.ChatService;

import com.grabpt.domain.entity.Messages;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;

import java.util.List;
import java.util.Map;

public interface ChatService {
	//public Messages saveMessage(Long roomId, MessageRequest.messageRequestDto request);
	public ChatResponse.CreateChatRoomResponseDto getOrcreateChatRoom(ChatRequest.CreateChatRoomRequestDto request);
	public Messages createChatMessage(ChatRequest.MessageRequestDto request);
	public List<ChatResponse.MessageResponseDto> getMessagesByChatRoom(Long roomId, Long cursor);
	public List<ChatResponse.ChatRoomPreviewDto> getChatRoomList(Long userId, String keyword);
	public Long getAllUnreadMessageCount(Long userId);
	public Map<Long, Long> getUnreadMessageCount(List<Long> roomIds, Long userId);
	public void updateLastReadMessageWhenExist(Long roomId, Long userId);
	public void updateLastReadMessageWhenEnter(Long roomId, Long userId);

}
