package com.grabpt.service.ChatService.redis;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("GitHub Actions 등 외부 CI 환경에서는 DB/Redis가 없어 실패하므로 제외")
@SpringBootTest
class UnreadCountCacheServiceTest {

	@Autowired
	private UnreadCountChatService unreadCountChatService;

	@Autowired
	private StringRedisTemplate redisTemplate; // 검증용

	@Test
	@DisplayName("최초 메시지 수신(INCR) 후 읽음 처리(SET 0)를 하고, 다시 수신(INCR)해도 에러가 나지 않아야 한다.")
	void unreadCountLifeCycleTest() {
		Long roomId = 1L;
		Long userId = 1L;
		String key = "chat:room_unread:" + roomId + ":" + userId;

		redisTemplate.delete(key);

		try {
			// 1. 처음 메시지를 받음 (INCR) -> 성공해야 함
			unreadCountChatService.incrementUnreadCount(roomId, userId);

			// 2. 유저가 방에 들어와서 읽음 처리 (SET 0) -> 여기서 JSON으로 꼬이던 문제 발생 지점
			unreadCountChatService.resetUnreadCount(roomId, userId);

			// 3. 다시 메시지를 받음 (INCR) -> 에러가 터지지 않고 1이 되어야 함!
			unreadCountChatService.incrementUnreadCount(roomId, userId);

			// then (검증)
			Map<Long, Long> result = unreadCountChatService.getUnreadCounts(List.of(roomId), userId);

			// 최종 안읽음 개수는 1개여야 한다.
			assertThat(result.get(roomId)).isEqualTo(1L);

		} finally {
			// 테스트 끝난 후 캐시 정리
			redisTemplate.delete(key);
		}
	}
}
