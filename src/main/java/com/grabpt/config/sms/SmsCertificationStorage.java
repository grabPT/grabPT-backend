package com.grabpt.config.sms;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Component
public class SmsCertificationStorage {
	// 전화번호 → 인증번호 (5분 TTL)
	private final Cache<String, String> certificationCache = Caffeine.newBuilder()
		.expireAfterWrite(5, TimeUnit.MINUTES)
		.maximumSize(10000)
		.build();

	public void saveCertificationCode(String phoneNum, String code) {
		certificationCache.put(phoneNum, code);
	}

	public String getCertificationCode(String phoneNum) {
		return certificationCache.getIfPresent(phoneNum);
	}

	public void removeCertificationCode(String phoneNum) {
		certificationCache.invalidate(phoneNum);
	}

	public boolean verifyCode(String phoneNum, String inputCode) {
		String savedCode = certificationCache.getIfPresent(phoneNum);
		return inputCode != null && inputCode.equals(savedCode);
	}
}
