package com.grabpt.repository.ChatRepository;

import com.grabpt.domain.entity.UserChatRoom;
import com.grabpt.dto.response.ChatResponse;
import com.grabpt.dto.response.ChatRoomPreviewDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
	@Query("SELECT ucr FROM UserChatRoom ucr WHERE ucr.user.id = :userId AND ucr.otherUser.id = :proId")
	Optional<UserChatRoom> findChatRoomByUserPair(@Param("userId") Long userId, @Param("proId") Long proId);

	@Query("""
   SELECT NEW com.grabpt.dto.response.ChatResponse.ChatRoomPreviewDto(
       cr.id,
       ucr.user.id,
       ou.id,
       ou.profileImageUrl,
       ucr.roomName,
       cr.lastMessage,
       cr.lastMessageTime
   )
   FROM UserChatRoom ucr
   JOIN ucr.chatRoom cr
   JOIN ucr.otherUser ou
   WHERE ucr.user.id = :userId
   AND (:keyword IS NULL OR :keyword LIKE %:keyword%)
   ORDER BY cr.lastMessageTime DESC
	""")
	List<ChatRoomPreviewDto> findChatRoomPreviewsByUserId(@Param("userId") Long userId, @Param("keyword") String keyword);

	@Query("SELECT ucr.chatRoom.id FROM UserChatRoom ucr WHERE ucr.user.id = :userId")
	List<Long> findChatRoomIdsByUserId(@Param("userId") Long userId);

	@Query("SELECT ucr FROM UserChatRoom ucr WHERE ucr.chatRoom.id = :roomId AND ucr.user.id = :userId")
	Optional<UserChatRoom> findByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

	@Query("SELECT u.otherUser.id FROM UserChatRoom u WHERE u.chatRoom.id = :roomId AND u.user.id = :userId")
	Long getOtherUserId(@Param("userId") Long userId, @Param("roomId") Long roomId);

//	@Query("SELECT SUM(ucr.unreadCount) FROM UserChatRoom ucr WHERE ucr.user.id = :userId")
//	Long sumUnreadCountByUserId(@Param("userId") Long userIdZZ);
}
