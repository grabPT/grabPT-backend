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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional(readOnly = false)
public class ChatServiceImpl implements ChatService{

	private final UserRepository userRepository;
	private final UserChatRoomRepository userChatRoomRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final MessageRepository messageRepository;
	private final SimpMessagingTemplate messagingTemplate;

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
		return newMessage;
	}

	@Override
	public List<ChatResponse.MessageResponseDto> getMessagesByChatRoom(Long roomId, Long cursor) {
		if(cursor == null){
			cursor = 0L;
		}
		Pageable pageable = PageRequest.of(0, 20);

		List<Messages> messagesByChatRoom = messageRepository.findMessagesByCursor(roomId, cursor, pageable);
		List<ChatResponse.MessageResponseDto> messageResponseDto = messagesByChatRoom.stream().map(
			message-> ChatConverter.toMessageResponseDto(message)).collect(Collectors.toList());
		return messageResponseDto;
	}

	@Override //fetch join고려
	public List<ChatResponse.ChatRoomPreviewDto> getChatRoomList(Long userId, String keyword) {
		Users user = userRepository.findById(userId)
			.orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		List<UserChatRoom> chatRooms = userChatRoomRepository.findByUserId(userId, keyword);

		List<Long> roomIds = chatRooms.stream()
			.map(chatRoom -> chatRoom.getChatRoom().getId())
			.toList();
		Map<Long, Long> unreadMessageCount = getUnreadMessageCount(roomIds, userId);

		return chatRooms.stream()
			.map(chatRoom -> {
				Long roomId = chatRoom.getChatRoom().getId();
				Long unreadCount = unreadMessageCount.getOrDefault(roomId, 0L);				return ChatConverter.toChatRoomPreviewDto(chatRoom, unreadCount);
			})
			.toList();
	}


	//상대가 보낸 메시지중 lastReadMessageId보다 큰 메시지 수
	@Override
	public Map<Long, Long> getUnreadMessageCount(List<Long> roomIds, Long userId){
		return messageRepository.getUnreadCountMap(roomIds, userId);
	}

	//채팅방 접속상태에서 message 읽은 경우
	@Override
	@Transactional
	public void updateLastReadMessageWhenExist(Long roomId, Long userId) {
		Messages messages = messageRepository.findTopByChatRoom_IdOrderByIdDesc(roomId).orElseThrow(
			()->new ChatHandler(ErrorStatus.MESSAGE_NOT_FOUND));

		Long recentMessageId = messages.getId();
		int currentReadCount = messages.getReadCount();
		if (currentReadCount > 0) {
			messages.setReadCount(currentReadCount - 1);
		}
		log.info("메시지 읽음 처리 when exist");
		UserChatRoom chatRoom = userChatRoomRepository.findByRoomIdAndUserId(roomId, userId).orElseThrow(
			() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND));

		chatRoom.setLastReadMessageId(recentMessageId);
		chatRoom.setLastReadAt(LocalDateTime.now());
		userChatRoomRepository.save(chatRoom);

		ChatResponse.ReadStatusUpdateDto dto  = ChatResponse.ReadStatusUpdateDto.builder()
			.messageId(messages.getId())
			.readCount(messages.getReadCount())
			.build();
		messagingTemplate.convertAndSend("/subscribe/chat/"+roomId+"/read-status", dto);
	}

	//채팅방 들어갈 시 message읽음 처리
	@Override
	@Transactional
	public void updateLastReadMessageWhenEnter(Long roomId, Long userId){
		List<Messages> unreadMessages = messageRepository.findUnreadMessages(roomId, userId);
		for (Messages msg : unreadMessages) {
			msg.setReadCount(0);
		}
		log.info("메시지 읽음 처리 when enter");
		messageRepository.saveAll(unreadMessages);

		for (Messages msg : unreadMessages) {
			ChatResponse.ReadStatusUpdateDto dto = ChatResponse.ReadStatusUpdateDto.builder()
				.messageId(msg.getId())
				.readCount(msg.getReadCount())
				.build();
			messagingTemplate.convertAndSend("/subscribe/chat/" + roomId + "/read-status", dto);
		}

		UserChatRoom chatRoom = userChatRoomRepository.findByRoomIdAndUserId(roomId, userId).orElseThrow(
			() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND));
		Long lastMessageId = messageRepository.findTopByChatRoom_IdOrderByIdDesc(roomId)
			.map(Messages::getId)
			.orElse(null);
		chatRoom.setLastReadMessageId(lastMessageId);
		chatRoom.setLastReadAt(LocalDateTime.now());
		userChatRoomRepository.save(chatRoom);
	}
}
