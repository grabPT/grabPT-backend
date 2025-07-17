package com.grabpt.service.ChatService;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.ChatHandler;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.converter.ChatConverter;
import com.grabpt.domain.entity.*;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;
import com.grabpt.repository.ChatRepository.ChatRoomRepository;
import com.grabpt.repository.ChatRepository.MessageRepository;
import com.grabpt.repository.ChatRepository.UserChatRoomRepository;
import com.grabpt.repository.UserRepository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = false)
public class ChatServiceImpl implements ChatService{

	private final UserRepository userRepository;
	private final UserChatRoomRepository userChatRoomRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final MessageRepository messageRepository;

	@Override
	public ChatResponse.CreateChatRoomResponseDto getOrcreateChatRoom(ChatRequest.CreateChatRoomRequestDto request){
		Optional<UserChatRoom> chatRoom = userChatRoomRepository.
			findChatRoomByUserPair(request.getUserId(), request.getProId());

		Users pro = userRepository.findById(request.getProId()).orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));
		Users user = userRepository.findById(request.getUserId()).orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		if(chatRoom.isPresent()){
			UserChatRoom findRoom = chatRoom.get();
			return ChatConverter.toCreateChatRoomResponseDto(findRoom);
		} else{
			ChatRooms newRoom = ChatRooms.builder()
				.lastMessage("")
				.lastMessageTime(LocalDateTime.now())
				.userChatRooms(new ArrayList<>())
				.build();

			UserChatRoom room1 = UserChatRoom.builder()
				.chatRoom(newRoom)
				.user(user)
				.otherUser(pro)
				.roomName(pro.getNickname())
				.lastReadMessageId(0L)
				.build();

			UserChatRoom room2 = UserChatRoom.builder()
				.chatRoom(newRoom)
				.user(pro)
				.otherUser(user)
				.roomName(user.getNickname())
				.lastReadMessageId(0L)
				.build();

			newRoom.addUserChatRoom(room1);
			newRoom.addUserChatRoom(room2);
			chatRoomRepository.save(newRoom);
			userChatRoomRepository.save(room1);
			userChatRoomRepository.save(room2);
			return ChatConverter.toCreateChatRoomResponseDto(room1);
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

		List<UserChatRoom> chatRooms = userChatRoomRepository.findByUserId(userId);

		return chatRooms.stream()
			.map(chatRoom -> {
				Long unreadCount = getUnreadMessageCount(chatRoom.getChatRoom().getId(), userId);
				return ChatConverter.toChatRoomPreviewDto(chatRoom, unreadCount);
			})
			.toList();
	}

	//Message Count 관련
	//채팅방에서 유저가 마지막으로 읽은 메시지의 아이디 가져옴
	@Override
	public Long getLastUnReadMessageId(Long roomId, Long userId){
		UserChatRoom chatRoom = userChatRoomRepository.findByRoomIdAndUserId(roomId, userId).orElseThrow(
			() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND));
		return chatRoom.getLastReadMessageId();
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
		Long recentMessageId = messageRepository.findTopByChatRoom_IdOrderByIdDesc(roomId)
			.map(Messages::getId)
			.orElse(null);

		UserChatRoom chatRoom = userChatRoomRepository.findByRoomIdAndUserId(roomId, userId).orElseThrow(
			() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND));

		chatRoom.setLastReadMessageId(recentMessageId);
		chatRoom.setLastReadAt(LocalDateTime.now());
		userChatRoomRepository.save(chatRoom);
	}
}
