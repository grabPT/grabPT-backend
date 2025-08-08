package com.grabpt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class ReviewRequestDTO {

	@Schema(description = "리뷰를 받는 전문가 프로필 ID", example = "1")
	private Long proProfileId;

	@Schema(description = "별점 (0.0 ~ 5.0)", example = "4.5")
	private Double rating;

	@Schema(description = "리뷰 내용", example = "정말 좋은 PT였습니다!")
	private String content;
}
