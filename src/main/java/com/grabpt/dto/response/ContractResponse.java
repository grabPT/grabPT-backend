package com.grabpt.dto.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.grabpt.domain.entity.ContractInfo;
import com.grabpt.domain.enums.MatchingStatus;
import com.grabpt.domain.enums.PaymentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

public class ContractResponse {

	@Getter
	@Builder
	public static class ContractResponseDto {
		@Builder.Default
		@Schema(description = "사용자 계약 정보", implementation = ContractInfo.class)
		ContractInfo userInfo = new ContractInfo();

		@Builder.Default
		@Schema(description = "전문가 계약 정보", implementation = ContractInfo.class)
		ContractInfo proInfo = new ContractInfo();

		@Schema(description = "세션 횟수", example = "12")
		Integer contractSessionCount;

		@Schema(description = "총 계약 금액(원)", example = "600000")
		Integer contractPrice;

		@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
		@Schema(description = "시작일", example = "2025-10-01")
		LocalDate startDate;

		@Schema(description = "PT 장소 주소", example = "서울시 강남구 ...")
		String ptLocation;

		@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
		@Schema(description = "계약일", example = "2025-09-26")
		LocalDate expireDate;


		@Schema(description = "매칭 상태", example = "MATCHED")
		MatchingStatus status;

		@Schema(description = "매칭 ID", example = "101")
		Long matchingId;
	}

	@Getter
	@Builder
	public static class CreateMatchingAndContractResponseDto {
		@Schema(description = "매칭 ID", example = "101")
		Long matchingId;

		@Schema(description = "계약 ID", example = "555")
		Long contractId;
	}

	@Getter
	@Builder
	public static class ContractListResponseDto {
		@Schema(description = "진행 중인 계약 수 (MATCHED)", example = "2")
		Integer totalActiveContracts;

		@Schema(description = "완료된 계약 수 (COMPLETED)", example = "5")
		Integer totalCompletedContracts;

		@Schema(description = "계약 목록 (페이지)")
		Page<ContractListItemDto> contracts;
	}

	@Getter
	@Builder
	public static class ContractListItemDto {
		@Schema(description = "계약 ID", example = "42")
		Long contractId;

		@Schema(description = "상대방 닉네임 (회원 조회 시 전문가 닉네임, 전문가 조회 시 회원 닉네임)", example = "운동초보냥")
		String userNickname;

		@Schema(description = "상대방 프로필 이미지 URL", example = "https://grabpt.com/images/default.png")
		String profileImageUrl;

		@Schema(description = "결제 상태 (READY: 결제 대기, OK: 결제 완료)", example = "READY")
		PaymentStatus paymentStatus;

		@Schema(description = "총 세션 횟수", example = "20")
		Integer sessionCount;

		@Schema(description = "1회 계약 금액(원)", example = "45000")
		Integer contractPrice;

		@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
		@Schema(description = "시작일", example = "2024-03-01")
		LocalDate startDate;

		@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
		@Schema(description = "만료일", example = "2024-05-31")
		LocalDate expireDate;
	}
}
