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
		//String iconUrl;
	}

	//전문가 목록 조회를 위한 DTO
	@Builder
	@Getter
	@AllArgsConstructor
	public static class TrainerPreviewListDto {
		List<CategoryResponse.TrainerListDto> trainerList;
		Integer listSize;
		Integer totalPage;
		Long totalElements;
		Boolean isFirst;
		Boolean isLast;
	}

	@Builder
	@Getter
	@AllArgsConstructor
	public static class TrainerListDto {
		Long id;  //ProProfile.id
		String name; //ProProfile.User.name
		double rating;  //ProProfile.reviews.getAverageReview
		CenterDto centerDto; //
		int pricePerSession; //Proprofile.Suggestions.price
		String profileImageUrl;
	}

	@Builder
	@Getter
	@AllArgsConstructor
	public static class CenterDto {
		String name;
		String address; //center가 없음
	}

	//요청서 목록을 위한 Dto
	@Builder
	@Getter
	@AllArgsConstructor
	public static class RequestListDto {
		Long id;
		String nickname; //request.user.nickname
		String region; //requestion.location
		int sessionCount; // requestion.sessionCount 없음
		int totalPrice;
		RequestStatus matchStatus; //reqeustion.status
		String profileImageUrl; // //UserProfile에 url없음
	}

}
