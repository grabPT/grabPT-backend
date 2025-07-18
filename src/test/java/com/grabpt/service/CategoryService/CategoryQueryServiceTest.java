package com.grabpt.service.CategoryService;

import com.grabpt.domain.entity.Category;
import com.grabpt.repository.CategoryRepository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class CategoryQueryServiceTest {

	@Autowired
	CategoryRepository categoryRepository;

	@BeforeEach
	void save(){
		categoryRepository.save(new Category(1L,"box", "복싱"));
		categoryRepository.save(new Category(2L,"swi", "수영"));
		categoryRepository.save(new Category(3L,"pin", "탁구"));
		categoryRepository.save(new Category(4L,"run", "런닝"));
		categoryRepository.save(new Category(5L,"hea", "헬스"));
		categoryRepository.save(new Category(6L,"dan", "댄스"));
		categoryRepository.save(new Category(7L,"fil", "필라테스"));

	}

	@Test
	void getCategories(){
		List<Category> all = categoryRepository.findAll();
		for (Category category : all) {
			System.out.println(category.getCode());
		}
	}

}
