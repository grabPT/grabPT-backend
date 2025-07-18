package com.grabpt.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserChatRoom {
	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable=false)
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id", nullable=false)
	private ChatRooms chatRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "other_user_id", nullable=false)
	private Users otherUser;

	private String roomName;
	private Long lastReadMessageId; // 마지막으로 읽은 메시지 ID 저장
	private LocalDateTime lastReadAt;
}
