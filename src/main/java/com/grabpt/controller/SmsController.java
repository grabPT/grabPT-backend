package com.grabpt.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.AuthHandler;
import com.grabpt.config.sms.SmsCertificationStorage;
import com.grabpt.dto.request.SmsRequestDto;
import com.grabpt.dto.response.SmsVerifyResponseDto;
import com.grabpt.service.SmsService.SmsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {

	private final SmsService smsService;
	private final SmsCertificationStorage smsCertificationStorage;

	@Operation(
		summary = "사용자 전화번호 인증 - 전화번호 발송",
		description = "사용자 전화번호 전달 시 그 번호로 인증 번호 전송"
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "수신 전화번호 입력",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = SmsRequestDto.class)
		)
	)
	@PostMapping("/send")
	public ApiResponse<String> SendSMS(@RequestBody @Valid SmsRequestDto smsRequestDto) {
		smsService.SendSms(smsRequestDto);
		return ApiResponse.onSuccess("번호 전송 완료");
	}

	@Operation(
		summary = "사용자 전화번호 인증 - 인증번호 검증",
		description = "사용자가 전달받은 인증번호를 받아 인증 과정 수행"
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "수신 전화번호와 인증번호 입력",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = SmsVerifyResponseDto.class)
		)
	)
	@PostMapping("/verify-sms")
	public ApiResponse<String> verifySms(@RequestBody SmsVerifyResponseDto dto) {
		String savedCode = smsCertificationStorage.getCertificationCode(dto.getPhoneNum());
		if (savedCode != null && savedCode.equals(dto.getInputCode())) {
			return ApiResponse.onSuccess("인증 성공");
		} else {
			throw new AuthHandler(ErrorStatus.UNAUTHORIZED_SMS);
		}
	}
}
