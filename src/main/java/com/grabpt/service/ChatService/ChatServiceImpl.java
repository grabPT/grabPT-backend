package com.grabpt.service.ChatService;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.ChatHandler;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.config.auth.PrincipalDetails;
import com.grabpt.converter.ChatConverter;
import com.grabpt.domain.entity.ChatRooms;
import com.grabpt.domain.entity.Messages;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.MessageType;
import com.grabpt.domain.enums.Role;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;
import com.grabpt.repository.ChatRepository.ChatRoomRepository;
import com.grabpt.repository.ChatRepository.MessageRepository;
import com.grabpt.repository.UserRepository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService{

	private final UserRepository userRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final MessageRepository messageRepository;

	@Override
	public ChatResponse.CreateChatRoomResponseDto getOrcreateChatRoom(ChatRequest.CreateChatRoomRequestDto request){
		Optional<ChatRooms> chatRoom = chatRoomRepository.findByUserIdAndProId(request.getUserId(), request.getProId());

		Users pro = userRepository.findById(request.getProId()).orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));
		Users user = userRepository.findById(request.getUserId()).orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		if(chatRoom.isPresent()){
			ChatRooms findRoom = chatRoom.get();
			return ChatConverter.toCreateChatRoomResponseDto(findRoom);
		} else{
			ChatRooms newRoom = ChatRooms.builder()
				.pro(pro)
				.user(user)
				.roomName(pro.getNickname() + "의 pt")
				.build();
			chatRoomRepository.save(newRoom);
			return ChatConverter.toCreateChatRoomResponseDto(newRoom);
		}
	}

	@Override
	public Messages createChatMessage(ChatRequest.MessageRequestDto request) {

		Users sender = userRepository.findById(request.getSenderId()).orElseThrow(
			() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		ChatRooms chatRoom = chatRoomRepository.findById(request.getRoomId()).orElseThrow(
			() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND));

		Messages newMessage = ChatConverter.toMessage(request, sender, chatRoom);
		Messages save = messageRepository.save(newMessage);

		chatRoom.setLastMessage(save.getContent());
		chatRoom.setLastMessageTime(save.getSentAt());
		chatRoomRepository.save(chatRoom);

		return newMessage;
	}

	@Override
	public List<ChatResponse.MessageResponseDto> getMessagesByChatRoom(Long roomId) {
		List<Messages> messagesByChatRoom = messageRepository.findAllByChatRoom(roomId);
		List<ChatResponse.MessageResponseDto> messageResponseDto = messagesByChatRoom.stream().map(
			message-> ChatConverter.toMessageResponseDto(message)).collect(Collectors.toList());
		return messageResponseDto;
	}

	@Override
	public List<ChatResponse.ChatRoomPreviewDto> getChatRoomList(Long userId) {
		Users user = userRepository.findById(userId)
			.orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		List<ChatRooms> chatRooms = (user.getRole() == Role.USER)
			? chatRoomRepository.findAllByUserId(userId)
			: chatRoomRepository.findAllByProId(userId);

		return chatRooms.stream()
			.map(ChatConverter::toChatRoomPreviewDto)
			.toList();
	}
}
