package com.grabpt.converter;

import com.grabpt.domain.entity.Contract;
import com.grabpt.dto.response.ContractResponse;

public class ContractConverter {
	public static ContractResponse.ContractResponseDto toContractResponseDto(Contract contract){
		return ContractResponse.ContractResponseDto.builder()
			.userInfo(contract.getUserInfo())
			.proInfo(contract.getProInfo())
			.price(contract.getPrice())
			.ptAddress(contract.getPtAddress())
			.startDate(contract.getStartDate())
			.totalSession(contract.getTotalSession())
			.status(contract.getMatching().getStatus())
			.build();
	}
}
