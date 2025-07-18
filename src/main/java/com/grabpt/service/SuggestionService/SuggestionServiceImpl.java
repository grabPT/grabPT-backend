package com.grabpt.service.SuggestionService;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.ProHandler;
import com.grabpt.apiPayload.exception.handler.RequestionHandler;
import com.grabpt.apiPayload.exception.handler.SuggestionHandler;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.entity.Suggestions;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.request.SuggestionRequestDto;
import com.grabpt.dto.response.SuggestionResponseDto;
import com.grabpt.repository.ProProfileRepository.ProProfileRepository;
import com.grabpt.repository.RequestionRepository.RequestionRepository;
import com.grabpt.repository.SuggestionRepository.SuggestionRepository;
import com.grabpt.repository.UserRepository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuggestionServiceImpl implements SuggestionService {

	private final SuggestionRepository suggestionRepository;
	private final UserRepository userRepository;
	private final ProProfileRepository proProfileRepository;
	private final RequestionRepository requestionRepository;

	@Override
	public Suggestions save(SuggestionRequestDto dto, String email) {
		Users user = userRepository.findByEmail(email)
			.orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		ProProfile proProfile = proProfileRepository.findByUser(user)
			.orElseThrow(() -> new ProHandler(ErrorStatus.PRO_NOT_FOUND));

		Requestions requestion = requestionRepository.findById(dto.getRequestionId())
			.orElseThrow(() -> new RequestionHandler(ErrorStatus.REQUESTION_NOT_FOUND));

		Suggestions suggestion = Suggestions.builder()
			.price(dto.getPrice())
			.message(dto.getMessage())
			.location(dto.getLocation())
			.sentAt(dto.getSentAt() != null ? dto.getSentAt() : LocalDateTime.now())
			.isAgreed(dto.getIsAgreed() != null ? dto.getIsAgreed() : false)
			.build();

		suggestion.setProProfile(proProfile);   // 연관관계 설정
		suggestion.setRequestion(requestion);   // 연관관계 설정

		return suggestionRepository.save(suggestion);
	}

	@Override
	public SuggestionResponseDto.SuggestionDetailResponseDto getDetail(Long suggestionId) {
		Suggestions suggestion = suggestionRepository.findById(suggestionId)
			.orElseThrow(() -> new SuggestionHandler(ErrorStatus.SUGGESTION_NOT_FOUND));
		return SuggestionResponseDto.SuggestionDetailResponseDto.from(suggestion);
	}
}
