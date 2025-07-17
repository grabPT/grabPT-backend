package com.grabpt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class ChatResponse {
	@AllArgsConstructor
	@Setter
	@Getter
	@Builder
	public static class MessageResponseDto{
		Long roomId;
		Long senderId;
		String content;
		String type;
		LocalDateTime sendAt;
	}

	@AllArgsConstructor
	@Setter
	@Getter
	@Builder
	public static class CreateChatRoomResponseDto{
		Long chatRoomId;
	}

	@AllArgsConstructor
	@Setter
	@Getter
	@Builder
	public static class ChatRoomPreviewDto{
		Long chatRoomId;
		Long unreadCount; //추가
		String roomName;
		String lastMessage;
		LocalDateTime lastMessageTime;
	}
}
