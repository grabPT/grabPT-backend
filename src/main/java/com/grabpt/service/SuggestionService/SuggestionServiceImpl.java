package com.grabpt.service.SuggestionService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.ProHandler;
import com.grabpt.apiPayload.exception.handler.RequestionHandler;
import com.grabpt.apiPayload.exception.handler.SuggestionHandler;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.aws.s3.AmazonS3Manager;
import com.grabpt.aws.s3.Uuid;
import com.grabpt.converter.SuggestionConverter;
import com.grabpt.domain.entity.Matching;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.entity.SuggestionPhoto;
import com.grabpt.domain.entity.Suggestions;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.MatchingStatus;
import com.grabpt.dto.request.SuggestionRequestDto;
import com.grabpt.dto.response.SuggestionResponseDto;
import com.grabpt.dto.response.UserResponseDto;
import com.grabpt.repository.ProProfileRepository.ProProfileRepository;
import com.grabpt.repository.RequestionRepository.RequestionRepository;
import com.grabpt.repository.SuggestionRepository.SuggestionRepository;
import com.grabpt.repository.UserRepository.UserRepository;
import com.grabpt.service.AlarmService.AlarmService;
import com.grabpt.service.MatchingService.MatchingService;
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
	private final AmazonS3Manager amazonS3Manager;
	private final AlarmService alarmService;
	private final MatchingService matchingService;

	@Override
	public Suggestions save(SuggestionRequestDto dto, String email, List<MultipartFile> photos) {
		Users user = userRepository.findByEmail(email)
			.orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		ProProfile proProfile = proProfileRepository.findByUser(user)
			.orElseThrow(() -> new ProHandler(ErrorStatus.PRO_NOT_FOUND));

		Requestions requestion = requestionRepository.findById(dto.getRequestionId())
			.orElseThrow(() -> new RequestionHandler(ErrorStatus.REQUESTION_NOT_FOUND));

		Suggestions suggestion = Suggestions.builder()
			.price(dto.getPrice())
			.sessionCount(dto.getSessionCount())
			.message(dto.getMessage())
			.location(dto.getLocation())
			.sentAt(dto.getSentAt() != null ? dto.getSentAt() : LocalDate.now())
			.isAgreed(dto.getIsAgreed() != null ? dto.getIsAgreed() : false)
			.photos(new ArrayList<>())
			.build();

		suggestion.setProProfile(proProfile);
		suggestion.setRequestion(requestion);

		// S3 업로드 및 SuggestionPhoto 저장
		if (photos != null && !photos.isEmpty()) {
			for (MultipartFile photo : photos) {
				String keyName = amazonS3Manager.generateSuggestionPhotoKeyName(
					Uuid.builder().uuid(UUID.randomUUID().toString()).build()
				);
				String imageUrl = amazonS3Manager.uploadFile(keyName, photo);

				SuggestionPhoto suggestionPhoto = SuggestionPhoto.builder()
					.imageUrl(imageUrl)
					.suggestion(suggestion)
					.build();

				suggestion.addPhoto(suggestionPhoto); // 양방향 연관관계 처리
			}
		}
		Suggestions save = suggestionRepository.save(suggestion);
		alarmService.sendAlarm(requestion.getUser().getId(), "SUGGESTION", "제안서 도착",
			user.getNickname() + " 님이 제안서를 보냈습니다", "/matching/proposals/" + suggestion.getId());
		return save;
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

		// 사진 URL 추출
		List<String> photoUrls = suggestion.getPhotos().stream()
			.map(photo -> photo.getImageUrl())
			.collect(Collectors.toList());

		// Matching 조회 (없으면 null)
		Matching matching = matchingService.findMatchingBySuggestionId(suggestionId);
		Long matchId = (matching != null) ? matching.getId() : null;

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
			.photoUrls(photoUrls) // 사진 포함
			.expertId(pro.getUser().getId())  // 트레이너 ID
			.userId(requestion.getUser().getId())  // 요청자 ID
			.matchingId(matchId)  // 매칭 ID (없으면 null)
			.requestionId(requestion.getId())
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

		return suggestionsPage.map(s -> {
			Matching matching = matchingService.findMatchingBySuggestionId(s.getId());
			MatchingStatus status = (matching != null) ? matching.getStatus() : MatchingStatus.WAITING;

			return SuggestionResponseDto.MySuggestionPagingDto.builder()
				.requestionNickname(s.getRequestion().getUser().getNickname())
				.price(s.getRequestion().getPrice())
				.sessionCount(s.getRequestion().getSessionCount())
				.status(status)
				.requestionId(s.getRequestion().getId())
				.suggestionId(s.getId())
				.profileImageUrl(s.getProProfile().getUser().getProfileImageUrl())
				.build();
		});
	}

	@Override
	@Transactional
	public void updateSuggestion(Long suggestionId, SuggestionRequestDto dto, String email) {
		// 작성자 검증
		Suggestions suggestion = suggestionRepository.findById(suggestionId)
			.orElseThrow(() -> new SuggestionHandler(ErrorStatus.SUGGESTION_NOT_FOUND));

		if (!suggestion.getProProfile().getUser().getEmail().equals(email)) {
			throw new SuggestionHandler(ErrorStatus.INVALID_PRO); // 작성자 아님
		}

		// 값 변경
		suggestion.setPrice(dto.getPrice());
		suggestion.setSessionCount(dto.getSessionCount());
		suggestion.setMessage(dto.getMessage());
		suggestion.setLocation(dto.getLocation());
		suggestion.setSentAt(dto.getSentAt() != null ? dto.getSentAt() : LocalDate.now());
		suggestion.setIsAgreed(dto.getIsAgreed() != null ? dto.getIsAgreed() : false);
	}

	@Override
	public void deleteSuggestion(Long suggestionId, String email) {
		Suggestions suggestion = suggestionRepository.findById(suggestionId)
			.orElseThrow(() -> new SuggestionHandler(ErrorStatus.SUGGESTION_NOT_FOUND));

		if (!suggestion.getProProfile().getUser().getEmail().equals(email)) {
			throw new SuggestionHandler(ErrorStatus.INVALID_PRO);
		}

		suggestionRepository.delete(suggestion);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean canEditSuggestion(Long suggestionId, String email) {
		Suggestions suggestion = suggestionRepository.findById(suggestionId)
			.orElseThrow(() -> new SuggestionHandler(ErrorStatus.SUGGESTION_NOT_FOUND));

		return suggestion.getProProfile().getUser().getEmail().equals(email);
	}
}
