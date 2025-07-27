package com.grabpt.domain.entity;

import java.time.LocalDateTime;

import com.grabpt.domain.common.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserChatRoom extends BaseEntity {
	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRooms chatRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "other_user_id", nullable = false)
	private Users otherUser;

	private String roomName;
	private Long lastReadMessageId; // 마지막으로 읽은 메시지 ID 저장
	private LocalDateTime lastReadAt;
}
