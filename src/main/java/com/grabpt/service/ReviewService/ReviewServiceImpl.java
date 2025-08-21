package com.grabpt.service.ReviewService;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Review;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.request.ReviewRequestDTO;
import com.grabpt.dto.response.MyReviewListDTO;
import com.grabpt.repository.ProProfileRepository.ProProfileRepository;
import com.grabpt.repository.ReviewRepository.reviewRepository;
import com.grabpt.repository.UserRepository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
	private final UserRepository userRepository;
	private final reviewRepository reviewRepository;
	private final ProProfileRepository proProfileRepository;

	@Override
	public void createReview(Long userId,ReviewRequestDTO reviewRequestDTO) {
		Users user = userRepository.findById(userId)
			.orElseThrow(()-> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		ProProfile proProfile = proProfileRepository.findById(reviewRequestDTO.getProProfileId())
			.orElseThrow(()-> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		Review review = Review.builder()
			.user(user)
			.proProfile(proProfile)
			.content(reviewRequestDTO.getContent())
			.rating(reviewRequestDTO.getRating())
			.build();

		reviewRepository.save(review);
	}

	@Override
	public void deleteReview(Long userId, Long reviewId) {
		Review review = reviewRepository.findByUser_IdAndIdOOrderByCreatedAtDesc(userId,reviewId);

		reviewRepository.delete(review);
	}

}
