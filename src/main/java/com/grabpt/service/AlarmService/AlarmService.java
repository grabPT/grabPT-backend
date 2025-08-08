package com.grabpt.service.AlarmService;

import com.grabpt.domain.entity.Alarm;
import com.grabpt.dto.response.AlarmResponseDto;

import java.util.List;

public interface AlarmService {
	public void sendAlarm(Long userId, String type, String title, String message, String redirectUrl);

	public AlarmResponseDto readAlarm(Long alarmId);

	public List<Alarm> findAllByUserId(Long userId);
}
