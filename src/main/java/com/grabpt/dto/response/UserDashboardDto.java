package com.grabpt.dto.response;

import java.time.LocalDateTime;

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
public class UserDashboardDto {

	private Long contractId;         // 계약 id
	private String trainerName;       // 회원(트레이너) 이름
	private Integer ptCount;         // PT 횟수 (Requestions.sessionCount)
	private Long paymentAmount;      // 결제 금액
	private LocalDateTime paymentDate; // 결제일
}
