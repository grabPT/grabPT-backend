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
		private String userNickname;
		private String centerName;
		private String profileImageUrl;

		private Integer suggestedPrice;
		private Integer requestedPrice;
		private Integer discountedPrice; // = original - suggested
		private Boolean isDiscounted;   // true if discount exists

		private String message;
		private String location;
		private List<String> photos; // 트레이너 제안 사진들

		private Long proId;
		private Long userId;
		private Long matchingId;
		private Long requestionId;
		private Long suggestionId;

		// 세션 횟수 관련 상세 정보
		private Integer suggestionSessionCount; // 전문가가 제안한 총 횟수
		private Integer requestionSessionCount; // 요청서의 총 횟수
		private Integer discountSessionCount;   // 횟수 차이 (요청 - 제안, 차이가 있을 경우)
	}

	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class SuggestionResponsePagingDto {
		private String userNickname;
		private String centerName;
		private String location;
		private Integer suggestedPrice;
		private Double averageRating; // 평점 추가
		private Integer sessionCount;
		private String profileImageUrl;
		private Long suggestionId;

	}

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MySuggestionPagingDto {
		private String userNickname; // 해당 suggestion의 requestion 유저의 nickname
		private Integer suggestedPrice;
		private Integer sessionCount;
		private MatchingStatus matchingStatus;
		private String profileImageUrl;
		private Long requestionId;
		private Long suggestionId;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class SuggestionSaveResponseDto {
		private Long suggestionId;
	}
}
