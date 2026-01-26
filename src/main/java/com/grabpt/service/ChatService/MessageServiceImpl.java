package com.grabpt.service.ChatService;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.ChatHandler;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.converter.ChatConverter;
import com.grabpt.domain.entity.ChatRooms;
import com.grabpt.domain.entity.Messages;
import com.grabpt.domain.entity.UserChatRoom;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;
import com.grabpt.repository.ChatRepository.MessageRepository;
import com.grabpt.service.AlarmService.AlarmService;
import com.grabpt.service.UserService.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService{

	private final MessageRepository messageRepository;

	private final UserQueryService userQueryService;
	private final UserChatRoomService userChatRoomService;
	private final SimpMessagingTemplate messagingTemplate;
	private final AlarmService alarmService;

	@Override //Message
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

	//상대가 보낸 메시지중 lastReadMessageId보다 큰 메시지 수
	@Override //Message
	public Map<Long, Long> getUnreadMessageCount(List<Long> roomIds, Long userId){
		return messageRepository.getUnreadCountMap(roomIds, userId);
	}

	@Override //Message
	public Long getAllUnreadMessageCount(Long userId){
		Users user = userQueryService.findById(userId)
			.orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		List<Long> roomIds = userChatRoomService.findChatRoomIdsByUserId(userId);

		Map<Long, Long> unreadMessageCount = getUnreadMessageCount(roomIds, userId);
		return unreadMessageCount.values().stream()
			.mapToLong(Long::longValue)
			.sum();
	}


	//채팅방 접속상태에서 message 읽은 경우
	@Override
	@Transactional
	public void updateLastReadMessageWhenExist(Long roomId, Long userId) {
		Messages messages = messageRepository.findTopByChatRoom_IdOrderByIdDesc(roomId).orElseThrow(
			()->new ChatHandler(ErrorStatus.MESSAGE_NOT_FOUND));


		UserChatRoom chatRoom = userChatRoomService.findByRoomIdAndUserId(roomId, userId).orElseThrow(
			() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND));

		chatRoom.setLastReadMessageId(messages.getId());
		chatRoom.setLastReadAt(LocalDateTime.now());
		userChatRoomService.save(chatRoom);

		if (!messages.getSender().getId().equals(userId)) {
			if (messages.getReadCount() > 0) {
				messages.setReadCount(messages.getReadCount() - 1);
			}
			broadcastReadStatus(roomId, messages);
		}
		updateAllUnreadMessageCount(userId);
	}

	//채팅방 들어갈 시 message읽음 처리
	@Override
	@Transactional
	public void updateLastReadMessageWhenEnter(Long roomId, Long userId){
		long startTime = System.currentTimeMillis(); // 시작 시간 측정
		List<Messages> unreadMessages = messageRepository.findUnreadMessages(roomId, userId);

		if(!unreadMessages.isEmpty()){
			messageRepository.markAsReadAllInRoom(roomId,userId);

			for (Messages msg : unreadMessages) {
				broadcastReadStatus(roomId, msg);
			}
		}

		UserChatRoom chatRoom = userChatRoomService.findByRoomIdAndUserId(roomId, userId).orElseThrow(
			() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND));

		messageRepository.findTopByChatRoom_IdOrderByIdDesc(roomId)
			.ifPresent(lastMessage -> {
				chatRoom.setLastReadMessageId(lastMessage.getId());
				chatRoom.setLastReadAt(LocalDateTime.now());
			});


		updateAllUnreadMessageCount(userId);
		long endTime = System.currentTimeMillis(); // 종료 시간 측정
		long duration = endTime - startTime; // 실행 시간 계산
		log.info("실행시간:{}",duration);
	}

	@Override
	public Messages save(Messages message) {
		return messageRepository.save(message);
	}

	private void broadcastReadStatus(Long roomId, Messages message) {
		ChatResponse.ReadStatusUpdateDto dto = ChatResponse.ReadStatusUpdateDto.builder()
			.messageId(message.getId())
			.readCount(message.getReadCount())
			.build();
		messagingTemplate.convertAndSend("/subscribe/chat/" + roomId + "/read-status", dto);
	}

	private void updateAllUnreadMessageCount(Long userId){
		Long allUnreadMessageCount = getAllUnreadMessageCount(userId);
		messagingTemplate.convertAndSend("/subscribe/chat/" + userId + "/unread-count", allUnreadMessageCount);
	}
}
