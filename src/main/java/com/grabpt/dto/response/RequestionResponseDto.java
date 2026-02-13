package com.grabpt.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.grabpt.domain.entity.Address;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Gender;
import com.grabpt.domain.enums.MatchingStatus;
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
		private Long categoryId;
		private List<String> purposes;
		private String ageGroup;
		private String userGender;
		private Integer requestedPrice;
		private Integer sessionCount;
		private String location;
		private LocalDate startDate;
		private List<String> availableDays;
		private List<String> availableTimes;
		private String proGender;
		private String content; // 상세 설명
		private String etcPurposeContent; // 기타 목적

		// 유저 정보
		private String userNickname;
		private String profileImageUrl;

		// 매칭 여부
		private Boolean isMatched;

		public static RequestionDetailResponseDto from(Requestions r) {
			Users u = r.getUser();

			return RequestionDetailResponseDto.builder()
				.requestionId(r.getId())
				.categoryId(r.getCategory().getId())
				.purposes(r.getPurpose())
				.ageGroup(r.getAgeGroup())
				.userGender(convertGenderToKorean(r.getUserGender()))
				.requestedPrice(r.getPrice())
				.sessionCount(r.getSessionCount())
				.location(r.getLocation())
				.startDate(r.getStartPreference())
				.availableDays(r.getAvailableDays())
				.availableTimes(r.getAvailableTimes())
				.proGender(convertGenderToKorean(r.getTrainerGender()))
				.content(r.getContent())
				.etcPurposeContent(r.getEtcPurposeContent())
				.userNickname(u.getNickname())
				.profileImageUrl(u.getProfileImageUrl())
				.isMatched(r.getMatching() != null)
				.build();
		}
	}

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RequestionResponsePagingDto {
		private String userName;        // 일반 유저
		private String location;      // 주소 - 동
		private Integer sessionCount;   // 세션 횟수
		private Integer requestedPrice;          // 1회당 가격
		private RequestStatus matchingStatus;   // 상태 (대기중 등)
		private String profileImageUrl; // 유저 프로필 이미지 링크
		private Long requestionId; // 리다이렉트를 위한 requestId
		private String content; // 상세 설명
		private String etcPurposeContent; // 기타 목적

		// 추가 정보
		private List<String> availableDays;
		private List<String> availableTimes;
		private String categoryName;

		// 추가 정보
		private String userNickname;

		public static RequestionResponsePagingDto from(Requestions r) {
			Users u = r.getUser();
			return RequestionResponsePagingDto.builder()
				.userName(u.getNickname())
				.location(u.getAddress().getStreet())
				.sessionCount(r.getSessionCount())
				.requestedPrice(r.getPrice())
				.matchingStatus(r.getStatus())
				.profileImageUrl(u.getProfileImageUrl())
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
		private Long requestId;
		private String profileImageURL;
		private List<String> availableDays;
		private List<String> availableTimes;
		private String categoryName;
		private Integer sessionCount;
		private String content;
		private AddressDto address;
		private Boolean isWriteReview;

		public static UserOwnRequestionDto from(Requestions requestion, Long proProfileId, String proNickname,
			boolean hasReview) {
			Users user = requestion.getUser();
			Address address = user.getAddress();

			return UserOwnRequestionDto.builder()
				.requestId(requestion.getId())
				.profileImageURL(user.getProfileImageUrl())
				.address(AddressDto.from(address))
				.categoryName(requestion.getCategory().getName())
				.availableDays(requestion.getAvailableDays())
				.availableTimes(requestion.getAvailableTimes())
				.content(requestion.getContent())
				.isWriteReview(canWriteReview(requestion) && !hasReview)
				.build();
		}
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class RequestionSaveResponseDto {
		private Long requestRequestionId;
	}

	@Getter
	@Builder
	public static class AddressDto {
		private String city;
		private String district;
		private String street;
		private String zipcode;
		private String streetCode;
		private String specAddress;

		public static AddressDto from(Address address) {
			if (address == null)
				return null;
			return AddressDto.builder()
				.city(address.getCity())
				.district(address.getDistrict())
				.street(address.getStreet())
				.zipcode(address.getZipcode())
				.streetCode(address.getStreetCode())
				.specAddress(address.getSpecAddress())
				.build();
		}
	}

	private static String convertGenderToKorean(Gender gender) {
		if (gender == null)
			return null;
		return switch (gender) {
			case MALE -> "남자";
			case FEMALE -> "여자";
		};
	}

	private static boolean canWriteReview(Requestions r) {
		if (r.getMatching() == null || r.getMatching().getStatus() == null) {
			return false;
		}
		return r.getMatching().getStatus() == MatchingStatus.COMPLETED;
	}
}
