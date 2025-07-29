package com.grabpt.dto.response;

import java.time.LocalDate;

import com.grabpt.domain.entity.ContractInfo;
import com.grabpt.domain.enums.MatchingStatus;

import lombok.Builder;
import lombok.Getter;

public class ContractResponse {

	@Getter
	@Builder
	public static class ContractResponseDto {
		ContractInfo userInfo;
		ContractInfo proInfo;
		Integer totalSession;
		Integer price;
		LocalDate startDate;
		String ptAddress;
		MatchingStatus status;
	}
}
