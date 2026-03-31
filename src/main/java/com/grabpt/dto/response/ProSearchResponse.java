package com.grabpt.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProSearchResponse {

    @Schema(description = "프로 프로필 ID", example = "1")
    private Long proProfileId;

    @Schema(description = "유저 ID", example = "10")
    private Long userId;

    @Schema(description = "닉네임", example = "강프로")
    private String nickname;

    @Schema(description = "프로필 이미지 URL")
    private String profileImageUrl;

    @Schema(description = "센터명", example = "강남 PT센터")
    private String center;

    @Schema(description = "카테고리 코드", example = "PT")
    private String categoryCode;

    @Schema(description = "카테고리명", example = "퍼스널 트레이닝")
    private String categoryName;

    @Schema(description = "경력(연차)", example = "5")
    private Integer career;

    @Schema(description = "1회 가격(원)", example = "50000")
    private Integer pricePerSession;

    @Schema(description = "평균 평점", example = "4.5")
    private Double averageRating;

    @Schema(description = "리뷰 수", example = "23")
    private Long reviewCount;

    @Schema(description = "시/도", example = "서울시")
    private String city;

    @Schema(description = "시/군/구", example = "강남구")
    private String district;

    @Schema(description = "소개 사진 URL 목록")
    private List<String> photoUrls;
}
