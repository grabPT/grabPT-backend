package com.grabpt.dto.request;

import java.time.LocalDate;

import com.grabpt.domain.enums.Gender;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class ContractRequest {

	@Getter
	@Setter
	@Schema(description = "계약 정보 DTO")
	public static class ContractInfoDto {

		@Schema(description = "이름", example = "홍길동")
		private String name;

		@Schema(description = "생년월일", example = "1990-01-01")
		private LocalDate birth;

		@Schema(description = "전화번호", example = "010-1234-5678")
		private String phoneNumber;

		@Schema(description = "성별", example = "MALE")
		private Gender gender;

		@Schema(description = "주소", example = "서울시 강남구")
		private String address;

		// @Schema(description = "전자서명 이미지")
		// private String signUrl;
	}
}
