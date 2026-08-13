package com.grabpt.service.ChatService.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/*
* lastMessageId를 통해 readCount를 캐싱합니다
* */

@Service
@RequiredArgsConstructor
public class ReadPointerCacheService {

	private final StringRedisTemplate redisTemplate;

	public Map<Long, Long> getPointers(Long roomId){
		Map<Object, Object> cached
			= redisTemplate.opsForHash().entries(generateKey(roomId));
		return cached.entrySet()
			.stream()
			.collect(Collectors.toMap(
				entry -> Long.valueOf(
					entry.getKey().toString()
				),
				entry -> Long.valueOf(
					entry.getValue().toString()
				)
			));
	}

	public void savePointers(Long roomId, Map<Long, Long> pointers){
		if (pointers == null || pointers.isEmpty()) {
			return;
		}

		Map<String, String> values =
			pointers.entrySet()
				.stream()
				.collect(Collectors.toMap(
					entry ->
						entry.getKey().toString(),
					entry ->
						entry.getValue().toString()
				));

		redisTemplate.opsForHash()
			.putAll(generateKey(roomId), values);
	}

	public void savePointer(Long roomId, Long userId, Long lastReadMessageId){
		redisTemplate.opsForHash().put(
			generateKey(roomId), userId.toString(), lastReadMessageId.toString()
		);
	}

	public void deletePointers(Long roomId) {
		redisTemplate.delete(generateKey(roomId));
	}

	private String generateKey(Long roomId){
		return "chat:room_read_pointer:"+roomId;
	}
}
