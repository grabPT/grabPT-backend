package com.grabpt.config.sms;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class SmsCertificationStorage {
	// 전화번호 → 인증번호
	private final ConcurrentHashMap<String, String> certificationMap = new ConcurrentHashMap<>();

	public void saveCertificationCode(String phoneNum, String code) {
		certificationMap.put(phoneNum, code);
	}

	public String getCertificationCode(String phoneNum) {
		return certificationMap.get(phoneNum);
	}

	public void removeCertificationCode(String phoneNum) {
		certificationMap.remove(phoneNum);
	}

	public boolean verifyCode(String phoneNum, String inputCode) {
		return inputCode.equals(certificationMap.get(phoneNum));
	}
}
