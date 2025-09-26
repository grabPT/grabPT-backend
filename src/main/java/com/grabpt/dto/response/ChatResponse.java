package com.grabpt.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
	public static class ChatRoomPreviewDto{
		Long roomId;
		Long userId;
		Long otherUserId;
		Long unreadCount; //추가
		String roomName;
		String lastMessage;
		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
		LocalDateTime lastMessageTime;
		String otherUserProfileImageUrl;
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
