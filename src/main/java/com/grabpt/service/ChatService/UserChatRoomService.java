package com.grabpt.service.ChatService;

import com.grabpt.domain.entity.UserChatRoom;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserChatRoomService {
	Optional<UserChatRoom> findChatRoomByUserPair(Long userId, Long proId);
	void save(UserChatRoom room);
	List<UserChatRoom> findByUserId(Long userId, String keyword);
	Long getOtherUserId(Long userId, Long roomId);
	Optional<UserChatRoom> findByRoomIdAndUserId(Long roomId, Long userId);
	List<Long> findChatRoomIdsByUserId(@Param("userId") Long userId);
}
