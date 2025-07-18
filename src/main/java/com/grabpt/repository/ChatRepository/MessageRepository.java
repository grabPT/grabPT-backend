package com.grabpt.repository.ChatRepository;

import com.grabpt.domain.entity.Messages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Messages, Long> {
	@Query("SELECT m FROM Messages m WHERE m.chatRoom.id = :roomId")
	List<Messages> findAllByChatRoom(@Param("roomId") Long roomId);

	Optional<Messages> findTopByChatRoom_IdOrderByIdDesc(Long roomId); //가장 최근 메시지

	// ChatRead 없는 경우: 전체 메시지 개수
	@Query("SELECT COUNT(m) FROM Messages m WHERE m.chatRoom.id = :roomId")
	Long countByRoomId(@Param("roomId") Long roomId);

	// lastReadMessageId보다 큰(=안읽은) 메시지 개수
	@Query("""
    SELECT COUNT(m) FROM Messages m WHERE m.chatRoom.id = :roomId
    AND m.id > :lastReadMessageId
    """)
	Long countByRoomIdAndIdGreaterThan(
		@Param("roomId") Long roomId,
		@Param("lastReadMessageId") Long lastReadMessageId
	);

}


