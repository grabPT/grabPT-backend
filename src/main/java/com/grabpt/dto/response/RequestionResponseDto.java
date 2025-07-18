package com.grabpt.dto.response;

import java.util.List;

import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Gender;

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

}
