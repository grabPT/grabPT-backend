package com.grabpt.service.ChatService;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.ChatHandler;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.config.auth.PrincipalDetails;
import com.grabpt.converter.ChatConverter;
import com.grabpt.domain.entity.ChatRooms;
import com.grabpt.domain.entity.MessageRead;
import com.grabpt.domain.entity.Messages;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.MessageType;
import com.grabpt.domain.enums.Role;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;
import com.grabpt.repository.ChatRepository.ChatRoomRepository;
import com.grabpt.repository.ChatRepository.MessageReadRepository;
import com.grabpt.repository.ChatRepository.MessageRepository;
import com.grabpt.repository.UserRepository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = false)
public class ChatServiceImpl implements ChatService{

	private final UserRepository userRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final MessageRepository messageRepository;
	private final MessageReadRepository messageReadRepository;

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
			.map(chatRoom -> {
				Long unreadCount = getUnreadMessageCount(chatRoom.getId(), userId);
				return ChatConverter.toChatRoomPreviewDto(chatRoom, unreadCount);
			})
			.toList();
	}

	//Message Count 관련
	//채팅방에서 유저가 마지막으로 읽은 메시지의 아이디 가져옴
	@Override
	public Long getLastUnReadMessageId(Long roomId, Long userId){
		return messageReadRepository.findByRoomIdAndUserId(roomId, userId).map(MessageRead::getLastReadMessageId)
			.orElse(null);
	}

	//채팅방에서 유저가 마지막으로 읽은 메시지보다 id가 큰 메시지의 개수
	@Override
	public Long getUnreadMessageCount(Long roomId, Long userId){
		Long lastUnReadMessageId = getLastUnReadMessageId(roomId, userId);
		if(lastUnReadMessageId == null){
			return messageRepository.countByRoomId(roomId);
		}
		return messageRepository.countByRoomIdAndIdGreaterThan(roomId, lastUnReadMessageId);
	}

	//가장 최근에 읽은 메시지 id update
	@Override
	public void updateLastReadMessage(Long roomId, Long userId) {
		Long recentMessageId = messageRepository.findTopByRoomIdOrderByIdDesc(roomId)
			.map(Messages::getId)
			.orElse(null);
		MessageRead messageRead = messageReadRepository.findByRoomIdAndUserId(roomId, userId)
			.orElse(MessageRead.builder()
				.roomId(roomId)
				.userId(userId)
				.build()
			);
		messageRead.setLastReadMessageId(recentMessageId);
		messageRead.setLastReadAt(LocalDateTime.now());
		messageReadRepository.save(messageRead);
	}
}
