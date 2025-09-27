package com.grabpt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
		@Schema(description = "채팅을 요청하는 유저 ID", example = "2")
		Long userId;
		@Schema(description = "채팅을 요청하는 유저 ID", example = "3")
		Long proId;
	}
}
