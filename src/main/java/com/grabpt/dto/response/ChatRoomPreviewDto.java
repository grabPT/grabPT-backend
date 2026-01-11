package com.grabpt.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ChatRoomPreviewDto {
	Long roomId;
	Long userId;
	Long otherUserId;
	String otherUserProfileImageUrl;
	@Schema(example = "뎀프시롤")
	String roomName;
	@Schema(example = "ㅎㅇㅎㅇ")
	String lastMessage;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	LocalDateTime lastMessageTime;
	Long unreadCount; //추가

	public ChatRoomPreviewDto(Long roomId, Long userId, Long otherUserId, String otherUserProfileImageUrl, String roomName, String lastMessage, LocalDateTime lastMessageTime) {
		this.roomId = roomId;
		this.userId = userId;
		this.otherUserId = otherUserId;
		this.otherUserProfileImageUrl = otherUserProfileImageUrl;
		this.roomName = roomName;
		this.lastMessage = lastMessage;
		this.lastMessageTime = lastMessageTime;
	}
}
