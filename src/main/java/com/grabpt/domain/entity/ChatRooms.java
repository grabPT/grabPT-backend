package com.grabpt.domain.entity;

import com.grabpt.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRooms extends BaseEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
	private List<UserChatRoom> userChatRooms = new ArrayList<>();

	@Column
	private String lastMessage;

	@Column
	private LocalDateTime lastMessageTime;

	public void addUserChatRoom(UserChatRoom userChatRoom) {
		userChatRooms.add(userChatRoom);
		userChatRoom.setChatRoom(this);
	}
}
