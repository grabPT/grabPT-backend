package com.grabpt.service.CategoryService;

import java.util.List;

import com.grabpt.domain.entity.Category;

public interface CategoryQueryService {
	List<Category> getCategories();

	Category findById(long id);
}
