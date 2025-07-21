package com.grabpt.service.ProfileService;

import com.grabpt.domain.entity.Category;
import com.grabpt.domain.entity.Center;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Users;
import com.grabpt.repository.CategoryRepository.CategoryRepository;
import com.grabpt.repository.ProProfileRepository.ProProfileRepository;
import com.grabpt.repository.UserRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProfileServiceImplTest {

	@Autowired
	UserRepository userRepository;
	@Autowired
	CategoryRepository categoryRepository;
	@Autowired
	ProProfileRepository proProfileRepository;

	@BeforeEach
	void before(){
		Category category = categoryRepository.findById(1L).orElse(null);
		Users users = userRepository.findById(17L).orElse(null);
		ProProfile.builder()
			.id(17L)
			.user(users)
			.category(category)
			.center(new Center("센터","서울시 동작구 상도동 123"))
			.pricePerSession(50000)
			.totalSessions(10)
			.build();
	}
	@Test
	void getProList(){
		List<ProProfile> allProByCategoryCodeAndRegion = proProfileRepository.findAllProByCategoryCodeAndRegion("box", "상도동");
		for (ProProfile proProfile : allProByCategoryCodeAndRegion) {
			System.out.println(proProfile.getUser().getNickname());
		}
	}

}
