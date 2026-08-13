package com.grabpt.service.ChatService;

import com.grabpt.domain.entity.UserChatRoom;
import com.grabpt.dto.response.ChatResponse;
import com.grabpt.dto.response.ChatRoomPreviewDto;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserChatRoomService {
	Optional<UserChatRoom> findChatRoomByUserPair(Long userId, Long proId);
	void save(UserChatRoom room);
	List<ChatRoomPreviewDto> findChatRoomPreviewsByUserId(Long userId, String keyword);
	Long getOtherUserId(Long userId, Long roomId);
	Optional<UserChatRoom> findByRoomIdAndUserId(Long roomId, Long userId);
	List<Long> findChatRoomIdsByUserId(@Param("userId") Long userId);
	List<UserChatRoom> findAllByRoomId(Long roomId);
}
