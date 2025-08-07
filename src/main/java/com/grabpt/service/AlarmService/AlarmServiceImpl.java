package com.grabpt.service.AlarmService;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.converter.AlarmConverter;
import com.grabpt.domain.entity.Alarm;
import com.grabpt.domain.entity.Users;
import com.grabpt.repository.AlarmRepository.AlarmRepository;
import com.grabpt.repository.UserRepository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmServiceImpl implements AlarmService {

	private final UserRepository userRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final AlarmRepository alarmRepository;

	@Transactional
	public void sendAlarm(Long userId, String type, String title, String content, String redirectUrl) {

		Users user = userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		Alarm alarm = Alarm.builder()
			.user(user)
			.isRead(false)
			.type(type)
			.title(title)
			.content(content)
			.redirectUrl(redirectUrl)
			.build();
		alarmRepository.save(alarm);
		messagingTemplate.convertAndSend("/user/" + user.getId() + "/alarm", AlarmConverter.toAlarmResponseDto(alarm));
		log.info("Alarm sent to user: {}", user.getId());
	}
}
