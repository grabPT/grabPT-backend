package com.grabpt.dto.response;

import java.util.List;

import com.grabpt.domain.enums.RequestStatus;

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
		private String nickname;
		private String center;
		private String profileImageUrl;

		private Integer suggestedPrice;
		private Integer originalPrice;
		private Integer discountAmount; // = original - suggested
		private Boolean isDiscounted;   // true if discount exists

		private String message;
		private String location;
		private List<String> photoUrls; // 트레이너 제안 사진들
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

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MySuggestionPagingDto {
		private String requestionNickname;
		private Integer price;
		private Integer sessionCount;
		private RequestStatus status;
	}

}
