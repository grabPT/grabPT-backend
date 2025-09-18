package com.grabpt.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.grabpt.domain.entity.Address;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Gender;
import com.grabpt.domain.enums.RequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
public class RequestionResponseDto {

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RequestionDetailResponseDto {
		private Long requestRequestionId;
		private Long requestCategoryId;
		private List<String> requestPurpose;
		private String requestAgeGroup;
		private String requestUserGender;
		private Integer requestPrice;
		private Integer requestSessionCount;
		private String requestLocation;
		private LocalDate requestStartPreference;
		private List<String> requestAvailableDays;
		private List<String> requestAvailableTimes;
		private String requestTrainerGender;
		private String requestContent; // 상세 설명
		private String requestEtcPurposeContent; // 기타 목적

		// 유저 정보
		private String requestUserNickName;
		private String photos;

		public static RequestionDetailResponseDto from(Requestions r) {
			Users u = r.getUser();

			return RequestionDetailResponseDto.builder()
				.requestRequestionId(r.getId())
				.requestCategoryId(r.getCategory().getId())
				.requestPurpose(r.getPurpose())
				.requestAgeGroup(r.getAgeGroup())
				.requestUserGender(convertGenderToKorean(r.getUserGender()))
				.requestPrice(r.getPrice())
				.requestSessionCount(r.getSessionCount())
				.requestLocation(r.getLocation())
				.requestStartPreference(r.getStartPreference())
				.requestAvailableDays(r.getAvailableDays())
				.requestAvailableTimes(r.getAvailableTimes())
				.requestTrainerGender(convertGenderToKorean(r.getTrainerGender()))
				.requestContent(r.getContent())
				.requestEtcPurposeContent(r.getEtcPurposeContent())
				.requestUserNickName(u.getNickname())
				.photos(u.getProfileImageUrl())
				.build();
		}
	}

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RequestionResponsePagingDto {
		private String requestUserName;        // 일반 유저
		private String requestUserStreet;      // 주소 - 동
		private Integer requestSessionCount;   // 세션 횟수
		private Integer requestPrice;          // 1회당 가격
		private RequestStatus requestStatus;   // 상태 (대기중 등)
		private String photos; // 유저 프로필 이미지 링크
		private Long requestRequestId; // 리다이렉트를 위한 requestId
		private String requestContent; // 상세 설명
		private String requestEtcPurposeContent; // 기타 목적

		// 추가 정보
		private List<String> requestAvailableDays;
		private List<String> requestAvailableTimes;
		private String requestCategoryName;

		// 추가 정보
		private String requestUserNickName;
		private String requestLocation;

		public static RequestionResponsePagingDto from(Requestions r) {
			Users u = r.getUser();
			return RequestionResponsePagingDto.builder()
				.requestUserName(u.getNickname())
				.requestUserStreet(u.getAddress().getStreet())
				.requestSessionCount(r.getSessionCount())
				.requestPrice(r.getPrice())
				.requestStatus(r.getStatus())
				.photos(u.getProfileImageUrl())
				.requestRequestId(r.getId())
				.requestContent(r.getContent())
				.requestEtcPurposeContent(r.getEtcPurposeContent())
				.build();
		}
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class UserOwnRequestionDto {
		private Long requestRequestionId;
		private String requestUserNickName;
		private String photos;

		private String requestCity;
		private String requestDistrict;
		private String requestStreet;
		private String requestZipcode;
		private String requestStreetCode;
		private String requestSpecAddress;
		private String requestEtcPurposeContent; // 기타 목적

		private String requestCategoryName;
		private List<String> requestAvailableDays;
		private List<String> requestAvailableTimes;
		private String requestContent;

		// 추가
		private RequestStatus requestStatus;
		private Long requestProProfileId;
		private String requestProNickName;

		public static UserOwnRequestionDto from(Requestions requestion, Long proProfileId, String proNickname) {
			Users user = requestion.getUser();
			Address address = user.getAddress();

			return UserOwnRequestionDto.builder()
				.requestRequestionId(requestion.getId())
				.requestUserNickName(user.getNickname())
				.photos(user.getProfileImageUrl())
				.requestCity(address.getCity())
				.requestDistrict(address.getDistrict())
				.requestStreet(address.getStreet())
				.requestZipcode(address.getZipcode())
				.requestStreetCode(address.getStreetCode())
				.requestSpecAddress(address.getSpecAddress())
				.requestCategoryName(requestion.getCategory().getName())
				.requestAvailableDays(requestion.getAvailableDays())
				.requestAvailableTimes(requestion.getAvailableTimes())
				.requestContent(requestion.getContent())
				.requestEtcPurposeContent(requestion.getEtcPurposeContent())
				.requestStatus(requestion.getStatus())
				.requestProProfileId(proProfileId)
				.requestProNickName(proNickname)
				.build();
		}
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class RequestionSaveResponseDto {
		private Long requestRequestionId;
	}

	private static String convertGenderToKorean(Gender gender) {
		if (gender == null)
			return null;
		return switch (gender) {
			case MALE -> "남자";
			case FEMALE -> "여자";
		};
	}
}
