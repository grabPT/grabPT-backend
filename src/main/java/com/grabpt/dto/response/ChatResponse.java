package com.grabpt.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class ChatResponse {
	@AllArgsConstructor
	@NoArgsConstructor
	@Setter
	@Getter
	@Builder
	public static class MessageResponseDto{
		Long messageId;
		Long roomId;
		Long senderId;
		String content;
		@Schema(description = "메시지의 타입(TEXT,IMAGE,FILE)", example = "TEXT")
		String messageType;
		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
		LocalDateTime sentAt;
		Integer readCount;
	}

	@AllArgsConstructor
	@NoArgsConstructor
	@Setter
	@Getter
	@Builder
	public static class MessageResponseByCursorDto{
		List<MessageResponseDto> messages;
		Long cursor;
	}

	@AllArgsConstructor
	@NoArgsConstructor
	@Setter
	@Getter
	@Builder
	public static class CreateChatRoomResponseDto{
		Long roomId;
	}

	@AllArgsConstructor
	@NoArgsConstructor
	@Setter
	@Getter
	@Builder
	public static class ReadStatusUpdateDto{
		Long messageId;
		int readCount;
	}
}
