package com.grabpt.service.SmsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grabpt.config.sms.SmsCertificationStorage;
import com.grabpt.config.sms.SmsCertificationUtil;
import com.grabpt.dto.request.SmsRequestDto;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {
	private final SmsCertificationUtil smsCertificationUtil;
	private final SmsCertificationStorage smsCertificationStorage;

	//의존성 주입
	public SmsServiceImpl(@Autowired SmsCertificationUtil smsCertificationUtil,
		@Autowired SmsCertificationStorage smsCertificationStorage) {
		this.smsCertificationUtil = smsCertificationUtil;
		this.smsCertificationStorage = smsCertificationStorage;
	}

	@Override // SmsService 인터페이스 메서드 구현
	public void SendSms(SmsRequestDto smsRequestDto) {
		String phoneNum = smsRequestDto.getPhoneNum(); // SmsrequestDto에서 전화번호를 가져온다.
		String certificationCode = Integer.toString(
			(int)(Math.random() * (999999 - 100000 + 1)) + 100000); // 6자리 인증 코드를 랜덤으로 생성
		log.info("CertificationCode = " + certificationCode);

		// 문자 발송
		smsCertificationUtil.sendSMS(phoneNum, certificationCode);
		// 인증번호 저장
		smsCertificationStorage.saveCertificationCode(phoneNum, certificationCode);

	}
}
