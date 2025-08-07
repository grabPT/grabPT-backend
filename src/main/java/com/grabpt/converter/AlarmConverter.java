package com.grabpt.converter;

import com.grabpt.domain.entity.Alarm;
import com.grabpt.dto.response.AlarmResponseDto;

import java.time.LocalDateTime;

public class AlarmConverter {
	public static AlarmResponseDto toAlarmResponseDto(Alarm alarm){
		return AlarmResponseDto.builder()
			.id(alarm.getId())
			.type(alarm.getType())
			.title(alarm.getTitle())
			.content(alarm.getContent())
			.createdAt(LocalDateTime.now())
			.redirectUrl(alarm.getRedirectUrl())
			.isRead(alarm.isRead())
			.build();
	}
}
