package com.grabpt.apiPayload.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.grabpt.apiPayload.code.BaseErrorCode;
import com.grabpt.apiPayload.code.ErrorReasonDTO;
import com.grabpt.apiPayload.exception.handler.AuthHandler;
import com.grabpt.config.oauth.CookieUtils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class AuthExceptionHandler {

	@ExceptionHandler(AuthHandler.class)
	public ResponseEntity<Void> handleAuth(AuthHandler ex, HttpServletResponse res) {
		BaseErrorCode ec = ex.getCode();
		ErrorReasonDTO reason = ec.getReasonHttpStatus(); // <-- BaseErrorCode 규약 사용

		// 공통 쿠키 삭제 (레거시 포함)
		CookieUtils.deleteCookie(res, "ACCESS_TOKEN");
		CookieUtils.deleteCookie(res, "REFRESH_TOKEN");
		CookieUtils.deleteCookie(res, "accessToken");
		CookieUtils.deleteCookie(res, "refreshToken");

		// 디버깅용 사유 헤더
		res.setHeader("X-Reason", reason.getCode());

		log.warn("[AUTH] {} - {}", reason.getCode(), reason.getMessage());
		return ResponseEntity.status(reason.getHttpStatus()).build();
	}
}
