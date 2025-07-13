package com.grabpt.config.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SmsCertificationUtil {

	@Value("${coolsms.apiKey}") // coolsms의 API 키 주입
	private String apiKey;

	@Value("${coolsms.apiSecret}") // coolsms의 API 비밀키 주입
	private String apiSecret;

	@Value("${coolsms.fromNumber}") // 발신자 번호 주입
	private String fromNumber;

	DefaultMessageService messageService; // 메시지 서비스를 위한 객체

	@PostConstruct // 의존성 주입이 완료된 후 초기화를 수행하는 메서드
	public void init() {
		this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret,
			"https://api.coolsms.co.kr"); // 메시지 서비스 초기화
	}

	// 단일 메시지 발송
	public void sendSMS(String to, String certificationCode) {
		log.info("발신번호 = " + fromNumber);
		log.info("수신번호 = " + to);
		Message message = new Message(); // 새 메시지 객체 생성
		message.setFrom("01063506423"); // 발신자 번호 설정
		message.setTo(to); // 수신자 번호 설정
		message.setText("본인확인 인증번호는 " + certificationCode + "입니다."); // 메시지 내용 설정

		this.messageService.sendOne(new SingleMessageSendingRequest(message)); // 메시지 발송 요청
	}
}
