package com.grabpt.converter;

import com.grabpt.domain.entity.Contract;
import com.grabpt.domain.entity.ContractInfo;
import com.grabpt.domain.entity.Order;
import com.grabpt.domain.entity.Payment;
import com.grabpt.domain.enums.PaymentStatus;
import com.grabpt.dto.response.ContractResponse;

public class ContractConverter {
	public static ContractResponse.ContractResponseDto toContractResponseDto(Contract contract) {
		PaymentStatus paymentStatus = contract.getMatching().getOrders().stream()
			.filter(o -> o.getPayment() != null)
			.map(o -> o.getPayment().getStatus())
			.filter(s -> s == PaymentStatus.OK)
			.findFirst()
			.orElse(PaymentStatus.READY);

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
			.paymentStatus(paymentStatus)
			.build();
	}
}
