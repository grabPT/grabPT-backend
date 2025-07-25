package com.grabpt.service.SuggestionService;

import java.time.LocalDate;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.ProHandler;
import com.grabpt.apiPayload.exception.handler.RequestionHandler;
import com.grabpt.apiPayload.exception.handler.SuggestionHandler;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.converter.SuggestionConverter;
import com.grabpt.domain.entity.ProPhoto;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.entity.Suggestions;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.request.SuggestionRequestDto;
import com.grabpt.dto.response.SuggestionResponseDto;
import com.grabpt.dto.response.UserResponseDto;
import com.grabpt.repository.ProProfileRepository.ProProfileRepository;
import com.grabpt.repository.RequestionRepository.RequestionRepository;
import com.grabpt.repository.SuggestionRepository.SuggestionRepository;
import com.grabpt.repository.UserRepository.UserRepository;
import com.grabpt.service.UserService.UserQueryService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuggestionServiceImpl implements SuggestionService {

	private final SuggestionRepository suggestionRepository;
	private final UserRepository userRepository;
	private final ProProfileRepository proProfileRepository;
	private final RequestionRepository requestionRepository;
	private final UserQueryService userQueryService;

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
			.sentAt(dto.getSentAt() != null ? dto.getSentAt() : LocalDate.now())
			.isAgreed(dto.getIsAgreed() != null ? dto.getIsAgreed() : false)
			.build();

		suggestion.setProProfile(proProfile);   // 연관관계 설정
		suggestion.setRequestion(requestion);   // 연관관계 설정

		return suggestionRepository.save(suggestion);
	}

	@Override
	@Transactional(readOnly = true)
	public SuggestionResponseDto.SuggestionDetailResponseDto getDetail(Long suggestionId) {
		Suggestions suggestion = suggestionRepository.findById(suggestionId)
			.orElseThrow(() -> new SuggestionHandler(ErrorStatus.SUGGESTION_NOT_FOUND));

		ProProfile pro = suggestion.getProProfile();
		Users user = pro.getUser();
		Requestions requestion = suggestion.getRequestion();

		// 가격 비교 로직
		int originalPrice = requestion.getPrice();
		int suggestedPrice = suggestion.getPrice();
		int discount = originalPrice - suggestedPrice;

		return SuggestionResponseDto.SuggestionDetailResponseDto.builder()
			.nickname(user.getNickname())
			.center(pro.getCenter())
			.profileImageUrl(user.getProfileImageUrl())
			.suggestedPrice(suggestedPrice)
			.originalPrice(originalPrice)
			.discountAmount(discount > 0 ? discount : 0)
			.isDiscounted(discount > 0)
			.message(suggestion.getMessage())
			.location(suggestion.getLocation())
			.photoUrls(pro.getPhotos().stream()
				.map(ProPhoto::getImageUrl)
				.collect(Collectors.toList()))
			.build();
	}

	@Override
	public Page<SuggestionResponseDto.SuggestionResponsePagingDto> getSuggestionsByRequestionId(Long requestionId,
		int page) {
		Pageable pageable = PageRequest.of(page, 6); // 6개씩 페이징
		Page<Suggestions> suggestionsPage = suggestionRepository.findByRequestionId(requestionId, pageable);

		return SuggestionConverter.toSuggestionResponsePageDto(suggestionsPage);
	}

	@Transactional(readOnly = true)
	public Page<SuggestionResponseDto.MySuggestionPagingDto> getMySuggestions(HttpServletRequest request,
		int page) throws
		IllegalAccessException {
		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();

		PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), 8); // 1부터 시작, 8개씩 페이징
		Page<Suggestions> suggestionsPage = suggestionRepository.findByProProfile_User_Email(email, pageable);

		return suggestionsPage.map(s -> SuggestionResponseDto.MySuggestionPagingDto.builder()
			.requestionNickname(s.getRequestion().getUser().getNickname())
			.price(s.getRequestion().getPrice())
			.sessionCount(s.getRequestion().getSessionCount())
			.status(s.getRequestion().getStatus())
			.build());
	}
}
