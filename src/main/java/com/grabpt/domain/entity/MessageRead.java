package com.grabpt.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Table(
	name = "message_read",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"roomId", "userId"})
	}
)
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MessageRead {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long roomId;

	private Long userId;

	private Long lastReadMessageId;

	private LocalDateTime lastReadAt;
}
