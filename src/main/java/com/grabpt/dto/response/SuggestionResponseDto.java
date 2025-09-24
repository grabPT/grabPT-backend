package com.grabpt.dto.response;

import java.util.List;

import com.grabpt.domain.enums.MatchingStatus;

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
		private String suggestUserNickName;
		private String suggestCenter;
		private String profileImageUrl;

		private Integer suggestSuggestedPrice;
		private Integer suggestOriginalPrice;
		private Integer suggestDiscountAmount; // = original - suggested
		private Boolean suggestIsDiscounted;   // true if discount exists

		private String suggestMessage;
		private String suggestLocation;
		private List<String> photos; // 트레이너 제안 사진들

		private Long suggestProId;
		private Long suggestUserId;
		private Long suggestMatchingId;
		private Long suggestRequestionId;
	}

	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class SuggestionResponsePagingDto {
		private String suggestUserNickName;
		private String suggestCenter;
		private String suggestAddress;
		private Integer suggestPrice;
		private Double suggestAverageRate; // 평점 추가
		private Integer suggestSessionCount;
		private String photos;
		private Long suggestSuggestionId;

	}

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MySuggestionPagingDto {
		private String suggestUserNickName; // 해당 suggestion의 requestion 유저의 nickname
		private Integer suggestPrice;
		private Integer suggestSessionCount;
		private MatchingStatus suggestStatus;
		private String photos;
		private Long suggestRequestionId;
		private Long suggestSuggestionId;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class SuggestionSaveResponseDto {
		private Long suggestSuggestionId;
	}
}
