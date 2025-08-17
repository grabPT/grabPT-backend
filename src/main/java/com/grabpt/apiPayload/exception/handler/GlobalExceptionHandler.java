package com.grabpt.apiPayload.exception.handler;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class)
	public ResponseEntity<?> handle401(Exception e) {
		return ResponseEntity.status(401).body(
			Map.of("code", "UNAUTHORIZED", "message", "인증이 필요합니다.")
		);
	}
}
