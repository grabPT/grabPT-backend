package com.grabpt.dto.response;

import java.time.LocalDateTime;

import com.grabpt.domain.entity.Suggestions;
import com.grabpt.domain.entity.Users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
public class SuggestionResponseDto {

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SuggestionDetailResponseDto {

		private Long suggestionId;
		private Integer price;
		private String message;
		private String location;
		private LocalDateTime sentAt;
		private Boolean isAgreed;

		// 프로 유저 정보
		private String nickname;
		private String profileImageUrl;

		public static SuggestionDetailResponseDto from(Suggestions s) {
			Users proUser = s.getProProfile().getUser();

			return SuggestionDetailResponseDto.builder()
				.suggestionId(s.getId())
				.price(s.getPrice())
				.message(s.getMessage())
				.location(s.getLocation())
				.sentAt(s.getSentAt())
				.isAgreed(s.getIsAgreed())
				.nickname(proUser.getNickname())
				.profileImageUrl(proUser.getProfileImageUrl())
				.build();
		}
	}

	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class SuggestionResponsePagingDto {
		private String nickname;
		private String center;
		private String address;
		private Integer price;
		private Double averageRate; // 평점 추가
	}

}
