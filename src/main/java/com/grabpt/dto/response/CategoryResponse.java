package com.grabpt.dto.response;

import java.util.List;

import com.grabpt.domain.enums.RequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class CategoryResponse {

	//카테고리 목록을 위한 DTO
	@Builder
	@Getter
	@AllArgsConstructor
	public static class CategoryListDto {
		Long categoryId;
		String categoryName;
	}

	//전문가 목록 조회를 위한 DTO
	@Builder
	@Getter
	@AllArgsConstructor
	public static class ProListDto {
		Long userId;  //ProProfile.getUser().getId()
		String userName; //ProProfile.User.name
		Double rating;  //ProProfile.reviews.getAverageReview
		String centerName;
		@Builder.Default
		int suggestedPrice = 0; //Proprofile.Suggestions.price
		@Builder.Default
		int sessionCount = 0;
		String profileImageUrl;
	}

	//요청서 목록을 위한 Dto
	@Builder
	@Getter
	@AllArgsConstructor
	public static class RequestListDto {
		Long requestionId;
		String userNickname; //request.user.nickname
		String location; //requestion.location
		int sessionCount; // requestion.sessionCount 없음
		int requestedPrice;
		RequestStatus matchingStatus; //reqeustion.status
		String profileImageUrl; // //UserProfile에 url없음
	}

}
