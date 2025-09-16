package com.grabpt.converter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.grabpt.domain.entity.Category;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.dto.response.CategoryResponse;

public class CategoryConverter {

	public static List<CategoryResponse.CategoryListDto> toCategoryListDto(List<Category> categories) {
		return categories.stream()
			.map(category -> CategoryResponse.CategoryListDto.builder()
				.categoryId(category.getId())
				.categoryName(category.getName())
				.build())
			.collect(Collectors.toList());
	}

	public static List<CategoryResponse.RequestListDto> toRequestListDto(List<Requestions> requestions) {
		return requestions.stream()
			.map(requestion -> CategoryResponse.RequestListDto.builder()
				.userNickName(requestion.getUser().getNickname())
				.requestLocation(requestion.getLocation())
				.matchStatus(requestion.getStatus())
				.profileImageUrl(requestion.getUser().getProfileImageUrl())
				.requestPrice(requestion.getPrice())
				.requestCount(requestion.getSessionCount())
				.build())
			.collect(Collectors.toList());
	}

	public static List<CategoryResponse.ProListDto> toProListDto(List<ProProfile> proList) {
		return proList.stream()
			.map(pro -> CategoryResponse.ProListDto.builder()
				.userName(pro.getUser().getNickname())
				.profileImageUrl(pro.getUser().getProfileImageUrl())
				.rating(pro.getAverageRating())
				.proCenterName(pro.getCenter())
				.suggestPrice(
					pro.getPricePerSession() != null ? pro.getPricePerSession() : 0 // pricePerSession null 방어
				)
				.suggestCount(
					pro.getTotalSessions() != null ? pro.getTotalSessions() : 0 // totalSessions도 null 가능하면 방어
				)
				.userId(pro.getUser().getId())
				.build())
			.sorted(Comparator.comparing(CategoryResponse.ProListDto::getRating).reversed())
			.collect(Collectors.toList());
	}

}
