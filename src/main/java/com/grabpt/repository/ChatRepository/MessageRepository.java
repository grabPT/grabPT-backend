package com.grabpt.repository.ChatRepository;

import com.grabpt.domain.entity.Messages;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Messages, Long> {
	@Query("""
	SELECT m FROM Messages m WHERE m.chatRoom.id = :roomId
	AND (:cursor = 0 OR m.id < :cursor)
	ORDER BY m.id DESC
	""")
	List<Messages> findMessagesByCursor(@Param("roomId") Long roomId, @Param("cursor") Long cursor, Pageable pageable);

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
		@Param("roomId") Long roomId, @Param("lastReadMessageId") Long lastReadMessageId);

	@Query("""
    SELECT m.id
    FROM Messages m
    WHERE m.chatRoom.id = :roomId
      AND m.sender.id <> :readerId
      AND m.id > :oldLastReadMessageId
      AND m.id <= :newLastReadMessageId
    ORDER BY m.id ASC
    """)
	List<Long> findNewlyReadMessageIds(
		@Param("roomId") Long roomId,
		@Param("readerId") Long readerId,
		@Param("oldLastReadMessageId")
		Long oldLastReadMessageId,
		@Param("newLastReadMessageId")
		Long newLastReadMessageId
	);
}


