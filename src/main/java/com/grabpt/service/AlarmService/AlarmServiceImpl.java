package com.grabpt.service.AlarmService;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.AlarmHandler;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.config.auth.PrincipalDetails;
import com.grabpt.converter.AlarmConverter;
import com.grabpt.domain.entity.Alarm;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.response.AlarmResponseDto;
import com.grabpt.repository.AlarmRepository.AlarmRepository;
import com.grabpt.repository.UserRepository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmServiceImpl implements AlarmService {

	private final UserRepository userRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final AlarmRepository alarmRepository;

	@Override
	@Transactional
	public void sendAlarm(Long userId, String type, String title, String content, String redirectUrl) {

		Users user = userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));
		Alarm alarm = Alarm.builder()
			.user(user)
			.isRead(false)
			.type(type)
			.title(title)
			.content(content)
			.sentAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
			.redirectUrl(redirectUrl)
			.build();
		alarmRepository.save(alarm);

		messagingTemplate.convertAndSend("/subscribe/alarm/"+user.getId(), countUnReadAlarmByUserId(userId));
		log.info("Alarm sent to user: {}", user.getId());
		log.info("currentTime:{}", LocalDateTime.now());
	}


	@Override
	@Transactional
	public AlarmResponseDto readAlarm(Long alarmId) {
		Alarm alarm = alarmRepository.findById(alarmId).orElseThrow(
			()->new AlarmHandler(ErrorStatus.ALARM_NOT_FOUND)
		);
		Long userId = alarm.getUser().getId();
		alarm.setRead(true);
		messagingTemplate.convertAndSend("/subscribe/alarm/"+userId, countUnReadAlarmByUserId(userId));
		return AlarmConverter.toAlarmResponseDto(alarm);
	}

	@Override
	public List<Alarm> findAllUnReadAlarmByUserId(Long userId){
		return alarmRepository.findAllUnReadAlarmByUserId(userId);
	}

	@Override
	public Long countUnReadAlarmByUserId(Long userId){
		return alarmRepository.countUnReadAlarmByUserId(userId);
	}

}
