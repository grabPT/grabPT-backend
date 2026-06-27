package com.grabpt.service.ChatService.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grabpt.dto.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ChatRedisSubscriber implements MessageListener {

	private final SimpMessagingTemplate messagingTemplate;
	private final ObjectMapper objectMapper;


	// Redis에 새로운 메시지 도착했을 때 실행할 로직
	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			ChatResponse.MessageResponseDto dto =
				objectMapper.readValue(message.getBody(), ChatResponse.MessageResponseDto.class);
			messagingTemplate.convertAndSend("/subscribe/chat/"+dto.getRoomId(), dto);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
