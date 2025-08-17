package com.grabpt.apiPayload.exception.handler;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
	public ResponseEntity<?> handle401(Exception e) {
		return ResponseEntity.status(401).body(Map.of("code", "UNAUTHORIZED", "message", "인증이 필요합니다."));
	}

	// 임시 안전망: 인증 객체 NPE도 401로
	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<?> handleNpe(NullPointerException e) {
		if (e.getMessage() != null && e.getMessage().contains("getPrincipal()")) {
			return ResponseEntity.status(401).body(Map.of("code", "UNAUTHORIZED", "message", "인증이 필요합니다."));
		}
		// 그 외 NPE는 기존 처리
		return ResponseEntity.status(500).body(Map.of("code", "COMMON500", "message", "서버 에러"));
	}
}
