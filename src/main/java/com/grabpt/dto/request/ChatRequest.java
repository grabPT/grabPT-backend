package com.grabpt.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ChatRequest {
	@AllArgsConstructor
	@Getter
	public static class MessageRequestDto{
		Long roomId;
		Long senderId;
		String content;
		String messageType;
	}

	@AllArgsConstructor
	@Getter
	public static class CreateChatRoomRequestDto{
		Long userId;
		Long proId;
	}
}
