package com.grabpt.converter;

import com.grabpt.domain.entity.ChatRooms;
import com.grabpt.domain.entity.Messages;
import com.grabpt.domain.entity.UserChatRoom;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.MessageType;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;

import java.time.LocalDateTime;

public class ChatConverter {
	public static ChatResponse.CreateChatRoomResponseDto toCreateChatRoomResponseDto(UserChatRoom room) {
		return ChatResponse.CreateChatRoomResponseDto.builder()
			.chatRoomId(room.getChatRoom().getId())
			.build();
	}

	public static Messages toMessage(ChatRequest.MessageRequestDto request, Users sender, ChatRooms chatRoom){
		return Messages.builder()
			.sender(sender)
			.chatRoom(chatRoom)
			.type(MessageType.fromString(request.getMessageType()))
			.content(request.getContent())
			.sentAt(LocalDateTime.now())
			.readCount(1)
			.build();
	}

	public static ChatResponse.MessageResponseDto toMessageResponseDto(Messages messages){
		return ChatResponse.MessageResponseDto.builder()
			.messageId(messages.getId())
			.roomId(messages.getChatRoom().getId())
			.senderId(messages.getSender().getId())
			.messageType(messages.getType().toString())
			.content(messages.getContent())
			.sendAt(messages.getSentAt())
			.readCount(messages.getReadCount())
			.build();
	}

	public static ChatResponse.ChatRoomPreviewDto toChatRoomPreviewDto(UserChatRoom userChatRoom, Long unreadCount){
		return ChatResponse.ChatRoomPreviewDto.builder()
			.chatRoomId(userChatRoom.getChatRoom().getId())
			.userId(userChatRoom.getUser().getId())
			.unreadCount(unreadCount)
			.otherUserProfile(userChatRoom.getOtherUser().getProfileImageUrl())
			.roomName(userChatRoom.getRoomName())
			.lastMessage(userChatRoom.getChatRoom().getLastMessage())
			.lastMessageTime(userChatRoom.getChatRoom().getLastMessageTime())
			.build();
	}
}
