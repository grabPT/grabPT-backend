package com.grabpt.service.ChatService;

import com.grabpt.domain.entity.UserChatRoom;
import com.grabpt.repository.ChatRepository.UserChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class UserChatRoomServiceImpl implements UserChatRoomService{

	private final UserChatRoomRepository userChatRoomRepository;

	@Override
	public Optional<UserChatRoom> findChatRoomByUserPair(Long userId, Long proId){
		return userChatRoomRepository.findChatRoomByUserPair(userId, proId);
	}

	@Override
	@Transactional
	public void save(UserChatRoom room){
		userChatRoomRepository.save(room);
	}

	@Override
	public List<UserChatRoom> findByUserId(Long userId, String keyword) {
		return userChatRoomRepository.findByUserId(userId, keyword);
	}

	@Override
	public Long getOtherUserId(Long userId, Long roomId) {
		return userChatRoomRepository.getOtherUserId(userId, roomId);
	}

	@Override
	public Optional<UserChatRoom> findByRoomIdAndUserId(Long roomId, Long userId) {
		return userChatRoomRepository.findByRoomIdAndUserId(roomId, userId);
	}

	@Override
	public List<Long> findChatRoomIdsByUserId(@Param("userId") Long userId){
		return userChatRoomRepository.findChatRoomIdsByUserId(userId);
	};

}
