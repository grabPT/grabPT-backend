package com.grabpt.converter;

import com.grabpt.domain.entity.Category;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CategoryConverter {

	public static List<CategoryResponse.CategoryListDto> toCategoryListDto(List<Category> categories){
		return categories.stream()
			.map(category -> CategoryResponse.CategoryListDto.builder()
				.categoryId(category.getId())
				.categoryName(category.getName())
				.build())
			.collect(Collectors.toList());
	}

	public static List<CategoryResponse.RequestListDto> toRequestListDto(List<Requestions> requestions){
		return requestions.stream()
			.map(requestion -> CategoryResponse.RequestListDto.builder()
				.nickname(requestion.getUser().getNickname())
				.region(requestion.getLocation())
				.matchStatus(requestion.getStatus())
				.profileImageUrl(null)
				.totalPrice(requestion.getPrice())
				.sessionCount(requestion.getSessionCount())
				.build())
			.collect(Collectors.toList());
	}


}
