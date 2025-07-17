package com.grabpt.converter;

import com.grabpt.domain.entity.ChatRooms;
import com.grabpt.domain.entity.Messages;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.MessageType;
import com.grabpt.dto.request.ChatRequest;
import com.grabpt.dto.response.ChatResponse;

import java.time.LocalDateTime;

public class ChatConverter {
	public static ChatResponse.CreateChatRoomResponseDto toCreateChatRoomResponseDto(ChatRooms room) {
		return ChatResponse.CreateChatRoomResponseDto.builder()
			.chatRoomId(room.getId())
			.userId(room.getUser().getId())
			.proId(room.getPro().getId())
			.build();
	}

	public static Messages toMessage(ChatRequest.MessageRequestDto request, Users sender, ChatRooms chatRoom){
		return Messages.builder()
			.sender(sender)
			.chatRoom(chatRoom)
			.type(MessageType.fromString(request.getMessageType()))
			.content(request.getContent())
			.sentAt(LocalDateTime.now())
			.build();
	}

	public static ChatResponse.MessageResponseDto toMessageResponseDto(Messages messages){
		return ChatResponse.MessageResponseDto.builder()
			.roomId(messages.getChatRoom().getId())
			.senderId(messages.getSender().getId())
			.type(messages.getType().toString())
			.content(messages.getContent())
			.sendAt(messages.getSentAt())
			.build();
	}

	public static ChatResponse.ChatRoomPreviewDto toChatRoomPreviewDto(ChatRooms chatRoom, Long unreadCount){
		return ChatResponse.ChatRoomPreviewDto.builder()
			.chatRoomId(chatRoom.getId())
			.unreadCount(unreadCount)
			.roomName(chatRoom.getRoomName())
			.lastMessage(chatRoom.getLastMessage())
			.lastMessageTime(chatRoom.getLastMessageTime())
			.build();
	}
}
