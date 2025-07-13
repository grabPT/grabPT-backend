package com.grabpt.service.CategoryService;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grabpt.domain.entity.Category;
import com.grabpt.repository.CategoryRepository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryQueryServiceImpl implements CategoryQueryService {

	private final CategoryRepository categoryRepository;

	@Override
	public List<Category> getCategories() {
		return categoryRepository.findAll();
	}
}
