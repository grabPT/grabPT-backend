package com.grabpt.converter;

import com.grabpt.domain.entity.Contract;
import com.grabpt.domain.entity.ContractInfo;
import com.grabpt.dto.response.ContractResponse;

public class ContractConverter {
	public static ContractResponse.ContractResponseDto toContractResponseDto(Contract contract){
		return ContractResponse.ContractResponseDto.builder()
			.userInfo(contract.getUserInfo() != null ? contract.getUserInfo() : new ContractInfo())
			.proInfo(contract.getProInfo() != null ? contract.getProInfo() : new ContractInfo())
			.price(contract.getPrice())
			.ptAddress(contract.getPtAddress())
			.startDate(contract.getStartDate())
			.totalSession(contract.getTotalSession())
			.contractDate(contract.getContractDate())
			.status(contract.getMatching().getStatus())
			.build();
	}
}
