package com.grabpt.converter;

import com.grabpt.domain.entity.Contract;
import com.grabpt.domain.entity.ContractInfo;
import com.grabpt.dto.response.ContractResponse;

public class ContractConverter {
	public static ContractResponse.ContractResponseDto toContractResponseDto(Contract contract) {
		return ContractResponse.ContractResponseDto.builder()
			.userInfo(contract.getUserInfo() != null ? contract.getUserInfo() : new ContractInfo())
			.proInfo(contract.getProInfo() != null ? contract.getProInfo() : new ContractInfo())
			.contractPrice(contract.getPrice())
			.ptLocation(contract.getPtAddress())
			.startDate(contract.getStartDate())
			.contractSessionCount(contract.getTotalSession())
			.expireDate(contract.getContractDate())
			.status(contract.getMatching().getStatus())
			.matchingId(contract.getMatching().getId())
			.build();
	}
}
