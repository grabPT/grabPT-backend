package com.grabpt.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChatRequest {
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	@Builder
	public static class MessageRequestDto{
		Long roomId;
		Long senderId;
		String content;
		String messageType;
	}

	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	public static class CreateChatRoomRequestDto{
		Long userId;
		Long proId;
	}
}
