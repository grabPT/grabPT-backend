package com.grabpt.service.AlarmService;

public interface AlarmService {
	public void sendAlarm(Long userId, String type, String title, String message, String redirectUrl);
}
