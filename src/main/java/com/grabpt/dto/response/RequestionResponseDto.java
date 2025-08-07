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
		private Long requestionId;
		private List<String> purpose;
		private String ageGroup;
		private Gender userGender;
		private Integer price;
		private Integer sessionCount;
		private String location;
		private LocalDate startPreference;
		private List<String> availableDays;
		private List<String> availableTimes;
		private Gender trainerGender;
		private String content; // 상세 설명
		private String etcPurposeContent; // 기타 목적

		// 유저 정보
		private String nickname;
		private String profileImageUrl;

		public static RequestionDetailResponseDto from(Requestions r) {
			Users u = r.getUser();

			return RequestionDetailResponseDto.builder()
				.requestionId(r.getId())
				.purpose(r.getPurpose())
				.ageGroup(r.getAgeGroup())
				.userGender(r.getUserGender())
				.price(r.getPrice())
				.sessionCount(r.getSessionCount())
				.location(r.getLocation())
				.startPreference(r.getStartPreference())
				.availableDays(r.getAvailableDays())
				.availableTimes(r.getAvailableTimes())
				.trainerGender(r.getTrainerGender())
				.content(r.getContent())
				.etcPurposeContent(r.getEtcPurposeContent())
				.nickname(u.getNickname())
				.profileImageUrl(u.getProfileImageUrl())
				.build();
		}
	}

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RequestionResponsePagingDto {
		private String username;        // 일반 유저 닉네임
		private String userStreet;      // 주소 - 동
		private Integer sessionCount;   // 세션 횟수
		private Integer price;          // 1회당 가격
		private RequestStatus status;   // 상태 (대기중 등)
		private String userProfileImageUrl; // 유저 프로필 이미지 링크
		private Long requestionId; // 리다이렉트를 위한 requestionId
		private String content; // 상세 설명
		private String etcPurposeContent; // 기타 목적

		public static RequestionResponsePagingDto from(Requestions r) {
			Users u = r.getUser();
			return RequestionResponsePagingDto.builder()
				.username(u.getNickname())
				.userStreet(u.getAddress().getStreet())
				.sessionCount(r.getSessionCount())
				.price(r.getPrice())
				.status(r.getStatus())
				.userProfileImageUrl(u.getProfileImageUrl())
				.requestionId(r.getId())
				.content(r.getContent())
				.etcPurposeContent(r.getEtcPurposeContent())
				.build();
		}
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class UserOwnRequestionDto {
		private String nickname;
		private String profileImageUrl;

		private String city;
		private String district;
		private String street;
		private String zipcode;
		private String streetCode;
		private String specAddress;
		private String etcPurposeContent; // 기타 목적

		private String categoryName;
		private List<String> availableDays;
		private List<String> availableTimes;
		private String content;

		public static UserOwnRequestionDto from(Requestions requestion) {
			Users user = requestion.getUser();
			Address address = user.getAddress();

			return UserOwnRequestionDto.builder()
				.nickname(user.getNickname())
				.profileImageUrl(user.getProfileImageUrl())
				.city(address.getCity())
				.district(address.getDistrict())
				.street(address.getStreet())
				.zipcode(address.getZipcode())
				.streetCode(address.getStreetCode())
				.specAddress(address.getSpecAddress())
				.categoryName(requestion.getCategory().getName())
				.availableDays(requestion.getAvailableDays())
				.availableTimes(requestion.getAvailableTimes())
				.content(requestion.getContent())
				.etcPurposeContent(requestion.getEtcPurposeContent())
				.build();
		}
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class RequestionSaveResponseDto {
		private Long requestionId;
	}
}
