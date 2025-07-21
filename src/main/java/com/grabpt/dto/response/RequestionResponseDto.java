package com.grabpt.dto.response;

import java.util.List;

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
		private String purpose;
		private String ageGroup;
		private Gender userGender;
		private Integer price;
		private Integer sessionCount;
		private String location;
		private String startPreference;
		private List<String> availableDays;
		private List<String> availableTimes;
		private Gender trainerGender;

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
	}
}
