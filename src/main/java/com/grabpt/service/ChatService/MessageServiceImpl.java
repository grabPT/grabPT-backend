package com.grabpt.service.ChatService;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.ChatHandler;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.converter.ChatConverter;
import com.grabpt.domain.entity.Messages;
import com.grabpt.domain.entity.UserChatRoom;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.response.ChatResponse;
import com.grabpt.repository.ChatRepository.MessageRepository;
import com.grabpt.service.ChatService.redis.ReadPointerCacheService;
import com.grabpt.service.ChatService.redis.RecentMessageCacheService;
import com.grabpt.service.ChatService.redis.UnreadCountChatService;
import com.grabpt.service.UserService.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService{

	private final MessageRepository messageRepository;
	private final UserQueryService userQueryService;
	private final UserChatRoomService userChatRoomService;
	private final SimpMessagingTemplate messagingTemplate;
	private final UnreadCountChatService unreadCountChatService;
	private final RecentMessageCacheService recentMessageCacheService;
	private final ReadPointerCacheService readPointerCacheService;

	@Override //Message
	public List<ChatResponse.MessageResponseDto> getMessagesByChatRoom(Long roomId, Long cursor, Long currentUserId) {

		Map<Long, Long> readPointers = getReadPointers(roomId);

		// Redis Hash에 현재 사용자가 없다면 채팅방 참여자가 아님
		if (!readPointers.containsKey(currentUserId)) {
			throw new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND);
		}
		Long otherUserId = readPointers.keySet()
				.stream()
				.filter(userId ->
					!userId.equals(currentUserId)
				)
				.findFirst()
				.orElseThrow(
					() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND));

		List<ChatResponse.MessageResponseDto> messages;

		if(cursor == null || cursor == 0L){
			List<ChatResponse.MessageResponseDto> cachedMessages =
				recentMessageCacheService.getRecentMessages(roomId);
			// Cache hit
			if(!cachedMessages.isEmpty()){
				messages = cachedMessages;
			} else{
				// Cache miss
				Pageable cachePageable = PageRequest.of(0, 50);
				List<Messages> messagesByCursor = messageRepository.findMessagesByCursor(roomId, 0L, cachePageable);
				List<ChatResponse.MessageResponseDto> cacheDto =
					messagesByCursor.stream().map(ChatConverter::toMessageResponseDto).toList();
				recentMessageCacheService.loadMessageToCache(roomId, cacheDto);
				messages = cacheDto.stream().limit(20).toList();
			}
		} else{
			Pageable pageable = PageRequest.of(0, 20);
			messages = messageRepository
				.findMessagesByCursor(roomId, cursor, pageable)
				.stream()
				.map(ChatConverter::toMessageResponseDto)
				.toList();
		}

		for (ChatResponse.MessageResponseDto message : messages) {
			Long recipientId = message.getSenderId().equals(currentUserId) ? otherUserId : currentUserId;
			long recipientLastReadMessageId = readPointers.getOrDefault(recipientId,0L);
			message.setReadCount(message.getMessageId()<=recipientLastReadMessageId ? 0 : 1);
		}
		return messages;
	}

	//상대가 보낸 메시지중 lastReadMessageId보다 큰 메시지 수
	@Override //Message
	public Map<Long, Long> getUnreadMessageCount(List<Long> roomIds, Long userId){
		// regacy: return messageRepository.getUnreadCountMap(roomIds, userId);
		return unreadCountChatService.getUnreadCounts(roomIds, userId);
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

		unreadCountChatService.resetUnreadCount(roomId, userId);

		// 새로운 채팅방 생성 시 메시지 없을 때는 pass
		markMessagesAsReadUpToLatest(roomId, userId);
		updateAllUnreadMessageCount(userId);
	}

	//채팅방 들어갈 시 message읽음 처리
	@Override
	@Transactional
	public void updateLastReadMessageWhenEnter(Long roomId, Long userId){

		unreadCountChatService.resetUnreadCount(roomId, userId);

		markMessagesAsReadUpToLatest(roomId, userId);
		updateAllUnreadMessageCount(userId);
	}

	@Override
	public Messages save(Messages message) {
		return messageRepository.save(message);
	}

	private void broadcastReadStatus(Long roomId, Long messageId) {
		ChatResponse.ReadStatusUpdateDto dto = ChatResponse.ReadStatusUpdateDto.builder()
			.messageId(messageId)
			.readCount(0)
			.build();
		messagingTemplate.convertAndSend("/subscribe/chat/" + roomId + "/read-status", dto);
	}

	private void updateAllUnreadMessageCount(Long userId){
		Long allUnreadMessageCount = getAllUnreadMessageCount(userId);
		messagingTemplate.convertAndSend("/subscribe/chat/" + userId + "/unread-count", allUnreadMessageCount);
	}

	private Map<Long, Long> getReadPointers(Long roomId){
		Map<Long, Long> pointers
			= readPointerCacheService.getPointers(roomId);

		if(pointers.size() == 2){
			return pointers;
		}

		List<UserChatRoom> participants = userChatRoomService.findAllByRoomId(roomId);
		if(participants.isEmpty()){
			throw new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND);
		}

		Map<Long, Long> loadedPointers = participants.stream()
			.collect(Collectors.toMap(
				participant ->
					participant.getUser().getId(),
				participant -> {
					Long pointer = participant.getLastReadMessageId();
					return pointer == null ? 0L : pointer;
			}));
		readPointerCacheService.savePointers(roomId,loadedPointers);
		return loadedPointers;
	}

	private void markMessagesAsReadUpToLatest(Long roomId, Long userId) {
		UserChatRoom userChatRoom = userChatRoomService.findByRoomIdAndUserId(roomId, userId)
			.orElseThrow(() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND));

		long oldLastReadMessageId = userChatRoom.getLastReadMessageId() == null
			? 0L
			: userChatRoom.getLastReadMessageId();

		messageRepository.findTopByChatRoom_IdOrderByIdDesc(roomId)
			.ifPresent(lastMessage -> {
				long newLastReadMessageId = lastMessage.getId();
				if (newLastReadMessageId <= oldLastReadMessageId) {
					return;
				}

				List<Long> newlyReadMessageIds = messageRepository.findNewlyReadMessageIds(
					roomId,
					userId,
					oldLastReadMessageId,
					newLastReadMessageId
				);

				userChatRoom.setLastReadMessageId(newLastReadMessageId);
				userChatRoom.setLastReadAt(LocalDateTime.now());
				userChatRoomService.save(userChatRoom);

				updateReadStateAfterCommit(
					roomId,
					userId,
					newLastReadMessageId,
					newlyReadMessageIds
				);
			});
	}

	private void updateReadStateAfterCommit(
		Long roomId,
		Long userId,
		Long lastReadMessageId,
		List<Long> newlyReadMessageIds
	) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				try {
					readPointerCacheService.savePointer(roomId, userId, lastReadMessageId);
				} catch (RuntimeException exception) {
					try {
						readPointerCacheService.deletePointers(roomId);
					} catch (RuntimeException deleteException) {
						log.error("읽음 포인터 캐시 삭제 실패. roomId={}", roomId, deleteException);
					}
					log.error(
						"읽음 포인터 캐시 갱신 실패. roomId={}, userId={}, lastReadMessageId={}",
						roomId,
						userId,
						lastReadMessageId,
						exception
					);
				}

				newlyReadMessageIds.forEach(messageId -> broadcastReadStatus(roomId, messageId));
			}
		});
	}
}
