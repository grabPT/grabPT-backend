package com.grabpt.service.ChatService.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grabpt.dto.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecentMessageCacheService {

	private final RedisTemplate<String, Object>	redisTemplate;
	private final ObjectMapper objectMapper;

	public void saveMessage(Long roomId, ChatResponse.MessageResponseDto dto){
		String key = generateKey(roomId);

		redisTemplate.opsForList().leftPush(key, dto);
		redisTemplate.opsForList().trim(key, 0, 49);
	}

	public List<ChatResponse.MessageResponseDto> getRecentMessages(Long roomId){
		String key = generateKey(roomId);
		List<Object> messages = redisTemplate.opsForList().range(key, 0, 19);

		if(messages == null || messages.isEmpty()){
			return new ArrayList<>();
		}

		return messages.stream()
			.map(obj -> objectMapper.convertValue(obj, ChatResponse.MessageResponseDto.class))
			.toList();
	}


	public void loadMessageToCache(Long roomId, List<ChatResponse.MessageResponseDto> messages){
		if(messages == null || messages.isEmpty()){
			return;
		}
		String key = generateKey(roomId);
		redisTemplate.delete(key);
		redisTemplate.opsForList().rightPushAll(key, messages.toArray());
	}

	private String generateKey(Long roomId){
		return "chat:room_messages:"+roomId;
	}
}
