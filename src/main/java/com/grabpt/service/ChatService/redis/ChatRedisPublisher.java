package com.grabpt.service.ChatService.redis;

import com.grabpt.dto.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRedisPublisher {

	private final RedisTemplate<String, Object> redisTemplate;

	public void publish(Long roomId, ChatResponse.MessageResponseDto message){
		redisTemplate.convertAndSend("chat:room:" + roomId, message);
	}
}

