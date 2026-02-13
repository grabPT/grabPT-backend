package com.grabpt.service.UserActivityService;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.GeneralException;
import com.grabpt.domain.entity.Matching;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.entity.Review;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.MatchingStatus;
import com.grabpt.dto.response.MyRequestListDTO;
import com.grabpt.dto.response.MyReviewListDTO;
import com.grabpt.dto.response.MyReviewUserDTO;
import com.grabpt.dto.response.ReviewListDto;
import com.grabpt.repository.UserRepository.UserRepository;
import com.grabpt.repository.ReviewRepository.reviewRepository;
import com.grabpt.service.MatchingService.MatchingService;
import com.grabpt.service.RequestionService.RequestionService;
import com.grabpt.service.ReviewService.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserActivityServiceImpl implements UserActivityService {

	private final UserRepository userRepository;
	private final ReviewService reviewService;
	private final RequestionService requestionService;
	private final MatchingService matchingService;
	private final reviewRepository reviewRepository;

	@Override
	public Page<MyRequestListDTO> findMyRequests(Long userId, Pageable pageable) {
		Page<Requestions> page = requestionService.page(userId, pageable);
		List<Long> requestionIds = page.stream().map(Requestions::getId).toList();

		if (requestionIds.isEmpty()) {
			return page.map(MyRequestListDTO::new);
		}

		List<Matching> matchings = matchingService.matchings(requestionIds);
		Map<Long, Matching> matchingMap = matchings.stream()
			.collect(Collectors.toMap(m -> m.getRequestion().getId(), m -> m));

		return page.map(req -> {
			MyRequestListDTO dto = new MyRequestListDTO(req);
			Matching m = matchingMap.get(req.getId());
			if (m != null) {
				Long proProfileId = m.getSuggestion().getProProfile().getId();
				dto.setProNickname(m.getSuggestion().getProProfile().getUser().getNickname());
				dto.setProProfileId(proProfileId);
				boolean completed = m.getStatus() == MatchingStatus.COMPLETED;
				boolean alreadyReviewed = reviewRepository.existsByUser_IdAndProProfile_Id(userId, proProfileId);
				dto.setCanWriteReview(completed && !alreadyReviewed);
			}
			return dto;
		});
	}

	@Override
	public Page<MyReviewUserDTO> findMyReviews(Long userId, Pageable pageable) {
		Page<Review> reviews = reviewService.reviews(userId, pageable);
		return reviews.map(MyReviewUserDTO::from);
	}

	@Override
	public Page<MyReviewListDTO> findProReviews(Long userId, Pageable pageable) {
		Users user = findUserById(userId);
		if (user.getProProfile() == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}
		Page<Review> reviews = reviewService.proReviews(user, pageable);
		return reviews.map(MyReviewListDTO::from);
	}

	@Override
	public Page<ReviewListDto> findProProReviews(Long userId, Pageable pageable) {
		Users user = findUserById(userId);
		if (user.getProProfile() == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}
		Page<Review> reviews = reviewService.proReviews(user, pageable);
		return reviews.map(ReviewListDto::from);
	}

	@Override
	public Page<MyReviewListDTO> findReviewsByUserId(Long userId, Pageable pageable) {
		Users user = findUserById(userId);
		if (user.getProProfile() == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}
		Page<Review> reviews = reviewService.proReviews(user, pageable);
		return reviews.map(MyReviewListDTO::from);
	}

	private Users findUserById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
	}
}
