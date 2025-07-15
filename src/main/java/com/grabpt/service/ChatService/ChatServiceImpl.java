package com.grabpt.service.ChatService;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.config.auth.PrincipalDetails;
import com.grabpt.domain.entity.ChatRooms;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Role;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;
import com.grabpt.repository.ChatRepository.ChatRoomRepository;
import com.grabpt.repository.UserRepository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService{

	private final UserRepository userRepository;
	private final ChatRoomRepository chatRoomRepository;

	@Override
	public ChatResponse.CreateChatRoomResponseDto getOrcreateChatRoom(ChatRequest.CreateChatRoomRequestDto request){
		Optional<ChatRooms> chatRoom = chatRoomRepository.findByUserIdAndProId(request.getUserId(), request.getProId());

		Users pro = userRepository.findById(request.getProId()).orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));
		Users user = userRepository.findById(request.getUserId()).orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		if(chatRoom.isPresent()){
			ChatRooms findRoom = chatRoom.get();
			return toDto(findRoom);
		} else{
			ChatRooms newRoom = ChatRooms.builder()
				.pro(pro)
				.user(user)
				.build();
			chatRoomRepository.save(newRoom);
			return toDto(newRoom);
		}
	}

	private ChatResponse.CreateChatRoomResponseDto toDto(ChatRooms room) {
		return ChatResponse.CreateChatRoomResponseDto.builder()
			.chatRoomId(room.getId())
			.userId(room.getUser().getId())
			.proId(room.getPro().getId())
			.build();
	}

}
