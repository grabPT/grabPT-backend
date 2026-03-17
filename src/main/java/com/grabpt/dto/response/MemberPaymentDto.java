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
public class MemberPaymentDto {
	private Long contractId;
	private String memberName; // 회원 이름
	private Integer ptCount; // PT 횟수 (Requestions.sessionCount) -> Contract.totalSession 최종 결제 기준의 PT 횟수로 변환
	private Long paymentAmount; // 결제 금액
	private Long earnedAmount; // 적립 금액
	private LocalDateTime paymentDate; // 결제일
	private String profileImgUrl; // 회원 프로필 이미지
}
