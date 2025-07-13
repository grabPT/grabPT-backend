package com.grabpt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public class SmsVerifyResponseDto {
	@Schema(
		description = "수신 번호 입력",
		example = "01012345678"
	)
	private String phoneNum;
	@Schema(
		description = "인증 번호 입력",
		example = "001241"
	)
	private String inputCode;

	// 기본 생성자
	public SmsVerifyResponseDto() {
	}

	// 생성자
	public SmsVerifyResponseDto(String phoneNum, String inputCode) {
		this.phoneNum = phoneNum;
		this.inputCode = inputCode;
	}

	// Getter/Setter
	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public String getInputCode() {
		return inputCode;
	}

	public void setInputCode(String inputCode) {
		this.inputCode = inputCode;
	}
}
