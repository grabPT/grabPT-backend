package com.grabpt.service.ChatService;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.ChatHandler;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.converter.ChatConverter;
import com.grabpt.domain.entity.ChatRooms;
import com.grabpt.domain.entity.Messages;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.service.AlarmService.AlarmService;
import com.grabpt.service.ChatService.redis.UnreadCountChatService;
import com.grabpt.service.UserService.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatFacade {
	private final MessageService messageService;
	private final UserQueryService userQueryService;
	private final ChatRoomService chatRoomService;
	private final UserChatRoomService userChatRoomService;
	private final AlarmService alarmService;
	private final UnreadCountChatService unreadCountChatService;
	private final SimpMessagingTemplate messagingTemplate;

	@Transactional //Message
	public Messages createChatMessage(ChatRequest.MessageRequestDto request) {

		Users sender = userQueryService.findById(request.getSenderId()).orElseThrow(
			() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		ChatRooms chatRoom = chatRoomService.findById(request.getRoomId()).orElseThrow(
			() -> new ChatHandler(ErrorStatus.CHATROOM_NOT_FOUND));

		Messages newMessage = ChatConverter.toMessage(request, sender, chatRoom);
		Messages save = messageService.save(newMessage);

		Long otherUserId = userChatRoomService.getOtherUserId(sender.getId(), chatRoom.getId());

		// 안읽음 카운트 조회 및 전달
		unreadCountChatService.incrementUnreadCount(chatRoom.getId(), otherUserId);
		Long allUnreadMessageCount = messageService.getAllUnreadMessageCount(otherUserId);
		messagingTemplate.convertAndSend("/subscribe/chat/"+otherUserId+"/unread-count", allUnreadMessageCount);

		if(chatRoom.getLastMessage().equals("")){
			alarmService.sendAlarm(otherUserId,"MESSAGE","메시지 도착",
				sender.getNickname()+"님이 채팅을 시작했어요", "/chat");
		}

		chatRoom.setLastMessage(save.getContent());
		chatRoom.setLastMessageTime(save.getSentAt());
		return newMessage;
	}
}
