package com.grabpt.service.ChatService;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.converter.ChatConverter;
import com.grabpt.domain.entity.ChatRooms;
import com.grabpt.domain.entity.UserChatRoom;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;
import com.grabpt.repository.ChatRepository.ChatRoomRepository;
import com.grabpt.repository.ChatRepository.UserChatRoomRepository;
import com.grabpt.service.UserService.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
	public List<ChatResponse.ChatRoomPreviewDto> getChatRoomList(Long userId, String keyword) {
		Users user = userQueryService.findById(userId)
			.orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		List<UserChatRoom> chatRooms = userChatRoomService.findByUserId(userId, keyword);

		List<Long> roomIds = chatRooms.stream()
			.map(chatRoom -> chatRoom.getChatRoom().getId())
			.toList();
		Map<Long, Long> unreadMessageCount = messageService.getUnreadMessageCount(roomIds, userId);

		return chatRooms.stream()
			.map(chatRoom -> {
				Long roomId = chatRoom.getChatRoom().getId();
				Long unreadCount = unreadMessageCount.getOrDefault(roomId, 0L);
				return ChatConverter.toChatRoomPreviewDto(chatRoom, unreadCount);
			})
			.toList();
	}

	@Override
	public Optional<ChatRooms> findById(Long id) {
		return chatRoomRepository.findById(id);
	}
}
