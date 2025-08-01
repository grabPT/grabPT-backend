package com.grabpt.dto.response;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainerDashboardDto {
	private Long totalEarnings;      // 적립 금액
	private Long totalOrders;        // 총 결제 건수
	private Long activeClients;      // 활성 회원 수
	private Page<MemberPaymentDto> memberPayments; // 페이징 처리된 회원 결제 내역
}
