package com.grabpt.service.SuggestionService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.grabpt.apiPayload.code.status.ErrorStatus;
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
import com.grabpt.domain.enums.SuggestStatus;
import com.grabpt.dto.request.SuggestionRequestDto;
import com.grabpt.dto.response.SuggestionResponseDto;
import com.grabpt.dto.response.UserResponseDto;
import com.grabpt.repository.SuggestionRepository.SuggestionRepository;
import com.grabpt.service.AlarmService.AlarmService;
import com.grabpt.service.MatchingService.MatchingService;
import com.grabpt.service.ProfileService.ProfileService;
import com.grabpt.service.RequestionService.RequestionService;
import com.grabpt.service.UserService.UserQueryService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuggestionServiceImpl implements SuggestionService {

	private final SuggestionRepository suggestionRepository;
	private final ProfileService profileService;
	private final RequestionService requestionService;
	private final UserQueryService userQueryService;
	private final AmazonS3Manager amazonS3Manager;
	private final AlarmService alarmService;
	private final MatchingService matchingService;

	@Override
	public Suggestions save(SuggestionRequestDto dto, String email, List<MultipartFile> photos) {
		Users user = userQueryService.findByEmail(email)
			.orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		ProProfile proProfile = profileService.findByUser(user); // 에러 처리는 서비스단에 구현

		Requestions requestion = requestionService.findById(dto.getRequestionId()); // 에러 처리는 서비스단에 구현

		Suggestions suggestion = Suggestions.builder()
			.price(dto.getPrice())
			.sessionCount(dto.getSessionCount())
			.message(dto.getMessage())
			.location(dto.getLocation())
			.sentAt(dto.getSentAt() != null ? dto.getSentAt() : LocalDate.now())
			.isAgreed(dto.getIsMatched() != null ? dto.getIsMatched() : false)
			.photos(new ArrayList<>())
			.status(SuggestStatus.MATCHING)
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

		// 전문가가 제안한 총 횟수
		long sessionCount = suggestionRepository.countByProProfileId(pro.getId());

		return SuggestionResponseDto.SuggestionDetailResponseDto.builder()
			.userNickName(user.getNickname())
			.centerName(pro.getCenter())
			.profileImageUrl(user.getProfileImageUrl())
			.suggestedPrice(suggestedPrice)
			.requestedPrice(originalPrice)
			.discountAmount(discount > 0 ? discount : 0)
			.isDiscounted(discount > 0)
			.message(suggestion.getMessage())
			.location(suggestion.getLocation())
			.photos(photoUrls) // 사진 포함
			.proId(pro.getUser().getId())  // 트레이너 ID
			.userId(requestion.getUser().getId())  // 요청자 ID
			.matchingId(matchId)  // 매칭 ID (없으면 null)
			.requestionId(requestion.getId())
			.suggestionId(suggestionId)
			.sessionCount(sessionCount)
			.build();
	}

	@Override
	@Transactional(readOnly = true)
	public Page<SuggestionResponseDto.SuggestionResponsePagingDto> getSuggestionsByRequestionId(Long requestionId,
		int page) {
		Pageable pageable = PageRequest.of(page, 6); // 6개씩 페이징
		Page<Suggestions> suggestionsPage = suggestionRepository.findByRequestionId(requestionId, pageable);

		return suggestionsPage.map(s -> {
			var pro = s.getProProfile();
			var user = pro.getUser();
			var address = user.getAddress();

			// 전문가가 제안한 총 횟수
			long sessionCount = suggestionRepository.countByProProfileId(pro.getId());

			return SuggestionResponseDto.SuggestionResponsePagingDto.builder()
				.userNickname(user.getNickname())
				.centerName(pro.getCenter())
				.location(address != null ? address.getFullAddress() : "")
				.suggestedPrice(s.getPrice())
				.averageRating(pro.getAverageRating())
				.sessionCount((int) sessionCount)
				.profileImageUrl(user.getProfileImageUrl())
				.suggestionId(s.getId())
				.build();
		});
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
				.userNickname(s.getRequestion().getUser().getNickname())
				.suggestedPrice(s.getRequestion().getPrice())
				.sessionCount(s.getRequestion().getSessionCount())
				.matchingStatus(status)
				.requestionId(s.getRequestion().getId())
				.suggestionId(s.getId())
				.profileImageUrl(s.getRequestion().getUser().getProfileImageUrl())
				.build();
		});
	}

	@Override
	@Transactional
	public void updateSuggestion(Long suggestionId, SuggestionRequestDto dto, String email) {
		Suggestions suggestion = suggestionRepository.findById(suggestionId)
			.orElseThrow(() -> new SuggestionHandler(ErrorStatus.SUGGESTION_NOT_FOUND));

		assertOwner(suggestion, email);

		// 값 변경
		suggestion.setPrice(dto.getPrice());
		suggestion.setSessionCount(dto.getSessionCount());
		suggestion.setMessage(dto.getMessage());
		suggestion.setLocation(dto.getLocation());
		suggestion.setSentAt(dto.getSentAt() != null ? dto.getSentAt() : LocalDate.now());
		suggestion.setIsAgreed(dto.getIsMatched() != null ? dto.getIsMatched() : false);
	}

	@Override
	@Transactional
	public void deleteSuggestion(Long suggestionId, String email) {
		// 1) 잠금 후 조회 (동시성 안전)
		Suggestions suggestion = suggestionRepository.findByIdForUpdate(suggestionId)
			.orElseThrow(() -> new SuggestionHandler(ErrorStatus.SUGGESTION_NOT_FOUND));

		// 2) 작성자 확인
		assertOwner(suggestion, email);

		// 3) 상태 검사: MATCHED면 삭제 불가
		if (suggestion.getStatus() == SuggestStatus.MATCHED) {
			throw new SuggestionHandler(ErrorStatus.SUGGESTION_DELETE_NOT_ALLOWED_STATUS);
		}

		suggestionRepository.delete(suggestion);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean canEditSuggestion(Long suggestionId, String email) {
		Suggestions suggestion = suggestionRepository.findById(suggestionId)
			.orElseThrow(() -> new SuggestionHandler(ErrorStatus.SUGGESTION_NOT_FOUND));

		return isOwner(suggestion, email);
	}

	@Override
	public Optional<Object> findById(Long suggestionId) {
		return Optional.of(suggestionRepository.findById(suggestionId));
	}

	private void assertOwner(Suggestions s, String email) {
		if (!isOwner(s, email)) {
			throw new SuggestionHandler(ErrorStatus.INVALID_PRO);
		}
	}

	private boolean isOwner(Suggestions s, String email) {
		return s.getProProfile() != null
			&& s.getProProfile().getUser() != null
			&& s.getProProfile().getUser().getEmail() != null
			&& s.getProProfile().getUser().getEmail().equalsIgnoreCase(email);
	}
}
