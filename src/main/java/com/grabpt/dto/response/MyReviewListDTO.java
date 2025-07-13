package com.grabpt.dto.response;

import com.grabpt.domain.entity.Review;
import com.grabpt.domain.entity.Users;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "전문가가 받은 리뷰 정보 DTO")
public class MyReviewListDTO {

	@Schema(description = "리뷰 ID", example = "1001")
	private Long reviewId;

	@Schema(description = "리뷰 작성자 닉네임", example = "홍길동")
	private String nickName;

	@Schema(description = "리뷰 작성자의 거주지", example = "서울특별시 강남구")
	private String residence;

	@Schema(description = "리뷰 평점 (0.0 ~ 5.0)", example = "4.5")
	private Double rating;

	@Schema(description = "리뷰 내용", example = "친절하고 운동도 재미있었어요!")
	private String content;

	public static MyReviewListDTO from(Review review) {
		Users user = review.getUser();

		return MyReviewListDTO.builder()
			.reviewId(review.getId())
			.nickName(user.getNickname())
			.residence(user.getUserProfile().getResidence())
			.rating(review.getRating())
			.content(review.getContent())
			.build();
	}
}
