package com.grabpt.converter;

import java.util.List;
import java.util.stream.Collectors;

import com.grabpt.domain.entity.Category;
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
}
