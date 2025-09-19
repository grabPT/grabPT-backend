package com.grabpt.service.ChatService;

import com.grabpt.domain.entity.Messages;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;

import java.util.List;
import java.util.Map;

public interface MessageService {
	public List<ChatResponse.MessageResponseDto> getMessagesByChatRoom(Long roomId, Long cursor);
	public Long getAllUnreadMessageCount(Long userId);
	public Map<Long, Long> getUnreadMessageCount(List<Long> roomIds, Long userId);
	public void updateLastReadMessageWhenExist(Long roomId, Long userId);
	public void updateLastReadMessageWhenEnter(Long roomId, Long userId);
	public Messages save(Messages message);
}
