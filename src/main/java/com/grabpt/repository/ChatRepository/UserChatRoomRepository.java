package com.grabpt.repository.ChatRepository;

import com.grabpt.domain.entity.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
	@Query("SELECT ucr FROM UserChatRoom ucr WHERE ucr.user.id = :userId AND ucr.otherUser.id = :proId")
	Optional<UserChatRoom> findChatRoomByUserPair(@Param("userId") Long userId, @Param("proId") Long proId);

	@Query("""
    SELECT ucr FROM UserChatRoom ucr
    JOIN FETCH ucr.chatRoom cr
    JOIN FETCH ucr.user u
    JOIN FETCH ucr.otherUser ou
    WHERE u.id = :userId
    AND (:keyword IS NULL OR :keyword = '' OR ucr.roomName LIKE %:keyword%)
    ORDER BY cr.lastMessageTime DESC
	""") //N+1문제 방지
	List<UserChatRoom> findByUserId(Long userId, String keyword);

	@Query("SELECT ucr FROM UserChatRoom ucr WHERE ucr.chatRoom.id = :roomId AND ucr.user.id = :userId")
	Optional<UserChatRoom> findByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

}
