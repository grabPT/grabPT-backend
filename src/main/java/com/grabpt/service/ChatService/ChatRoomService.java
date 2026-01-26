package com.grabpt.service.ChatService;

import com.grabpt.domain.entity.ChatRooms;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;
import com.grabpt.dto.response.ChatRoomPreviewDto;

import java.util.List;
import java.util.Optional;

public interface ChatRoomService {
	 ChatResponse.CreateChatRoomResponseDto getOrcreateChatRoom(ChatRequest.CreateChatRoomRequestDto request);
	 List<ChatRoomPreviewDto> getChatRoomList(Long userId, String keyword);
	 Optional<ChatRooms> findById(Long id);
}
