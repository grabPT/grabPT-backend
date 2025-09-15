package com.grabpt.service.CategoryService;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.CategoryHandler;
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

	@Override
	public Category findById(long id) {
		return categoryRepository.findById(id).orElseThrow(() -> new CategoryHandler(ErrorStatus.CATEGORY_NOT_FOUND));
	}
}
