package com.grabpt.dto.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.grabpt.domain.entity.ContractInfo;
import com.grabpt.domain.enums.MatchingStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

public class ContractResponse {

	@Getter
	@Builder
	public static class ContractResponseDto {
		@Builder.Default
		@Schema(description = "사용자 계약 정보", implementation = ContractInfo.class)
		ContractInfo userInfo = new ContractInfo();

		@Builder.Default
		@Schema(description = "전문가 계약 정보", implementation = ContractInfo.class)
		ContractInfo proInfo = new ContractInfo();

		@Schema(description = "세션 횟수", example = "12")
		Integer contractSessionCount;

		@Schema(description = "총 계약 금액(원)", example = "600000")
		Integer contractPrice;

		@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
		@Schema(description = "시작일", example = "2025-10-01")
		LocalDate startDate;

		@Schema(description = "PT 장소 주소", example = "서울시 강남구 ...")
		String ptLocation;

		@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
		@Schema(description = "계약일", example = "2025-09-26")
		LocalDate expireDate;


		@Schema(description = "매칭 상태", example = "MATCHED")
		MatchingStatus status;

		@Schema(description = "매칭 ID", example = "101")
		Long matchingId;
	}

	@Getter
	@Builder
	public static class CreateMatchingAndContractResponseDto {
		@Schema(description = "매칭 ID", example = "101")
		Long matchingId;

		@Schema(description = "계약 ID", example = "555")
		Long contractId;
	}
}
