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
public class UserDashboardResponseDto {

	// 합계/지표
	private Long totalSpent;      // 총 결제 금액 합
	private Long totalOrders;     // 총 결제 건수
	private Long activeContracts; // 활성 계약(매칭) 수

	// 페이징 목록 (회원 결제내역)
	private Page<UserDashboardDto> payments;
}
