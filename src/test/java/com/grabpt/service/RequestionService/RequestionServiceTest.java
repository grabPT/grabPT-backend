package com.grabpt.service.RequestionService;

import com.grabpt.domain.entity.Category;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Gender;
import com.grabpt.domain.enums.Role;
import com.grabpt.repository.CategoryRepository.CategoryRepository;
import com.grabpt.repository.RequestionRepository.RequestionRepository;
import com.grabpt.repository.UserRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RequestionServiceTest {

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	RequestionRepository requestionRepository;

	@Autowired
	UserRepository userRepository;


	@Test
	void testFindTop6ByCategoryCode() {
		Pageable pageable = Pageable.ofSize(6);
		List<Requestions> result = requestionRepository.findTop6RequestionsByCategory("box", pageable);

		assertEquals(6, result.size());
		result.forEach(r -> {
			System.out.println("요청 목적: " + r.getPurpose() + ", 생성일: " + r.getCreatedAt());
		});
	}
}
