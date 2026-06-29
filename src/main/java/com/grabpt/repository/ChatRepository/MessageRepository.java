package com.grabpt.repository.ChatRepository;

import com.grabpt.domain.entity.Messages;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

	@Query("SELECT m FROM Messages m WHERE m.chatRoom.id = :roomId AND m.readCount = 1 AND m.sender.id <> :userId")
	List<Messages> findUnreadMessages(@Param("roomId") Long roomId, @Param("userId") Long userId);

	@Modifying(clearAutomatically = true)
	@Query("""
	UPDATE Messages m SET m.readCount = 0
	WHERE m.chatRoom.id = :roomId
	AND m.sender.id <> :userId
	AND m.readCount = 1
	""")
	void markAsReadAllInRoom(@Param("roomId") Long roomId, @Param("userId") Long userId);

	// legacy
//	@Query("""
//    SELECT m.chatRoom.id, COUNT(m)
//       FROM Messages m
//       JOIN m.chatRoom r
//       WHERE r.id IN :roomIds
//       AND m.id > (
//            SELECT COALESCE(ucr.lastReadMessageId, 0)
//            FROM UserChatRoom ucr
//            WHERE ucr.chatRoom.id = m.chatRoom.id AND ucr.user.id = :userId
//       )
//       AND m.sender.id <> :userId
//       GROUP BY m.chatRoom.id
//	""")
//	List<Object[]> countUnreadMessages(@Param("roomIds") List<Long> roomIds, @Param("userId") Long userId);
//
//	default Map<Long, Long> getUnreadCountMap(List<Long> roomIds, Long userId) {
//		List<Object[]> results = countUnreadMessages(roomIds, userId);
//		return results.stream()
//			.collect(Collectors.toMap(
//				result -> (Long) result[0],  // roomId
//				result -> (Long) result[1]   // count
//			));
//	}
}


