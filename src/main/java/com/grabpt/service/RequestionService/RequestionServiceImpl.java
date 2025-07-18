package com.grabpt.service.RequestionService;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.RequestionHandler;
import com.grabpt.domain.entity.Category;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.RequestStatus;
import com.grabpt.dto.request.RequestionRequestDto;
import com.grabpt.dto.response.RequestionResponseDto;
import com.grabpt.repository.CategoryRepository.CategoryRepository;
import com.grabpt.repository.RequestionRepository.RequestionRepository;
import com.grabpt.repository.UserRepository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RequestionServiceImpl implements RequestionService {

	private final RequestionRepository requestionRepository;
	private final CategoryRepository categoryRepository;
	private final UserRepository userRepository;

	@Override
	public List<Requestions> getReqeustions(String categoryCode, Pageable pageable) {
		return requestionRepository.findTop6RequestionsByCategory(categoryCode, pageable);
	}

	@Override
	public Requestions save(RequestionRequestDto dto, String email) {
		Users user = userRepository.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("사용자 없음"));

		Category category = categoryRepository.findById(dto.getCategoryId())
			.orElseThrow(() -> new RuntimeException("카테고리 없음"));

		Requestions requestion = Requestions.builder()
			.user(user)  // JWT 기반으로 추출된 사용자
			.category(category)
			.price(dto.getPrice())
			.sessionCount(dto.getSessionCount())
			.purpose(dto.getPurpose())
			.ageGroup(dto.getAgeGroup())
			.userGender(dto.getUserGender())
			.availableDays(dto.getAvailableDays())
			.availableTimes(dto.getAvailableTimes())
			.trainerGender(dto.getTrainerGender())
			.startPreference(dto.getStartPreference())
			.location(dto.getLocation())
			.status(RequestStatus.MATCHING)
			.build();

		requestion.setUser(user); // 연관관계 설정

		return requestionRepository.save(requestion);
	}

	@Override
	public RequestionResponseDto.RequestionDetailResponseDto getDetail(Long requestionId) {
		Requestions r = requestionRepository.findById(requestionId)
			.orElseThrow(() -> new RequestionHandler(ErrorStatus.REQUESTION_NOT_FOUND));
		return RequestionResponseDto.RequestionDetailResponseDto.from(r);
	}
}
