package com.grabpt.service.ChatService.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnreadCountChatService {

	//private final RedisTemplate<String, Object> redisTemplate;
	private final StringRedisTemplate redisTemplate;

	private String generateKey(Long roomId, Long userId) {
		return "chat:room_unread:" + roomId + ":" + userId;
	}
	public void incrementUnreadCount(Long roomId, Long userId){
		String key = generateKey(roomId, userId);
		redisTemplate.opsForValue().increment(key);
		log.info("Redis Unread Increment: [{}]", key);
	}

	public void resetUnreadCount(Long roomId, Long userId){
		String key = generateKey(roomId, userId);
		redisTemplate.opsForValue().set(key, "0");
		log.info("Redis Unread Reset: [{}]", key);
	}

	public Map<Long, Long> getUnreadCounts(List<Long> roomIds, Long userId){
		if(roomIds==null || roomIds.isEmpty()){
			return new HashMap<>();
		}
		List<String> keys = roomIds.stream()
			.map(roomId -> generateKey(roomId, userId))
			.toList();

		// Redis MGET
		List<String> values = redisTemplate.opsForValue().multiGet(keys);

		Map<Long, Long> resultMap = new HashMap<>();
		for(int i=0; i<roomIds.size(); i++){
			Long roomId = roomIds.get(i);
			Object value = values != null ? values.get(i) : null;

			Long unreadCount = (value == null) ? 0L : Long.parseLong(value.toString());
			resultMap.put(roomId, unreadCount);
		}

		return resultMap;
	}
}
