package com.grabpt.service.CategoryService;

import com.grabpt.domain.entity.Category;
import com.grabpt.domain.entity.Requestions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryQueryService {
	List<Category> getCategories();
}
