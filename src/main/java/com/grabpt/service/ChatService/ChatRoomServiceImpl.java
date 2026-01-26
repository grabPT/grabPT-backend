package com.grabpt.service.ChatService;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.converter.ChatConverter;
import com.grabpt.domain.entity.ChatRooms;
import com.grabpt.domain.entity.UserChatRoom;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;
import com.grabpt.dto.response.ChatRoomPreviewDto;
import com.grabpt.repository.ChatRepository.ChatRoomRepository;
import com.grabpt.repository.ChatRepository.UserChatRoomRepository;
import com.grabpt.service.UserService.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService{

	private final ChatRoomRepository chatRoomRepository;
	private final UserChatRoomService userChatRoomService;
	private final UserQueryService userQueryService;
	private final MessageService messageService;

	@Override //ChatRoom
	public ChatResponse.CreateChatRoomResponseDto getOrcreateChatRoom(ChatRequest.CreateChatRoomRequestDto request){
		Optional<UserChatRoom> chatRoom = userChatRoomService.
			findChatRoomByUserPair(request.getUserId(), request.getProId());

		Users pro = userQueryService.findById(request.getProId()).orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));
		Users user = userQueryService.findById(request.getUserId()).orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

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
			userChatRoomService.save(room1);
			userChatRoomService.save(room2);
			return ChatConverter.toCreateChatRoomResponseDto(room1);
		}
	}

	@Override //fetch join고려 //ChatRoom
	@Transactional
	public List<ChatRoomPreviewDto> getChatRoomList(Long userId, String keyword) {
		long start = System.currentTimeMillis();
		List<ChatRoomPreviewDto> chatRoomPreviews = userChatRoomService.findChatRoomPreviewsByUserId(userId, keyword);

		if (chatRoomPreviews.isEmpty()) {
			return Collections.emptyList();
		}

		List<Long> roomIds = chatRoomPreviews.stream()
			.map(chatRoom -> chatRoom.getRoomId())
			.toList();
		Map<Long, Long> unreadMessageCount = messageService.getUnreadMessageCount(roomIds, userId);

		chatRoomPreviews.forEach(preview ->
			preview.setUnreadCount(unreadMessageCount.getOrDefault(preview.getRoomId(), 0L))
		);

		long end = System.currentTimeMillis();
		long time = end-start;
		log.info("실행 시간:{}",time);
		return chatRoomPreviews;
	}

	@Override
	public Optional<ChatRooms> findById(Long id) {
		return chatRoomRepository.findById(id);
	}
}
