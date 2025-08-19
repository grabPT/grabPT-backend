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
		@Schema(description = "User contract info", implementation = ContractInfo.class)
		ContractInfo userInfo = new ContractInfo();
		@Builder.Default
		@Schema(description = "Pro contract info", implementation = ContractInfo.class)
		ContractInfo proInfo = new ContractInfo();
		Integer totalSession;
		Integer price;
		@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
		LocalDate startDate;
		String ptAddress;
		@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
		LocalDate contractDate;
		MatchingStatus status;
		Long matchingId;
	}

	@Getter
	@Builder
	public static class CreateMatchingAndContractResponseDto {
		Long matchingId;
		Long contractId;
	}
}
