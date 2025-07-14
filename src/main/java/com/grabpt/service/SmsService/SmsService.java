package com.grabpt.service.SmsService;

import com.grabpt.dto.request.SmsRequestDto;

public interface SmsService {

	void SendSms(SmsRequestDto smsRequestDto);
}
