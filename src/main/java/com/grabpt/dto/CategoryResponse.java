package com.grabpt.dto;

import com.grabpt.domain.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class CategoryResponse {

	//카테고리 목록을 위한 DTO
	@Builder
	@Getter
	@AllArgsConstructor
	public static class CategoryListDto{
		Long categoryId;
		String categoryName;
		//String iconUrl;
	}


	//전문가 목록 조회를 위한 DTO
	@Builder
	@Getter
	@AllArgsConstructor
	public static class TrainerPreviewListDto{
		List<CategoryResponse.TrainerPreviewDto> trainerList;
		Integer listSize;
		Integer totalPage;
		Long totalElements;
		Boolean isFirst;
		Boolean isLast;
	}

	@Builder
	@Getter
	@AllArgsConstructor
	public static class TrainerPreviewDto{
		Long id;
		String name;
		double rating;
		CenterDto centerDto;
		int pricePerSession;
		String profileImageUrl;
	}

	@Builder
	@Getter
	@AllArgsConstructor
	public static class CenterDto{
		String name;
		String address;
	}

	//요청서 목록을 위한 Dto
	@Builder
	@Getter
	@AllArgsConstructor
	public static class RequestListDto{
		Long id;
		String nickname; //request.user.nickname
		String region; //requestion.location
		int sessionCount; // requestion.sessionCount 없음
		int totalPrice;
		RequestStatus matchStatus; //reqeustion.status
		String profileImageUrl; // //UserProfile에 url없음
	}



}
