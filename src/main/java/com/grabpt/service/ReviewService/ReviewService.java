package com.grabpt.service.ReviewService;

import com.grabpt.domain.entity.Review;
import com.grabpt.domain.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.grabpt.dto.request.ReviewRequestDTO;
import com.grabpt.dto.response.MyReviewListDTO;

public interface ReviewService {
	void createReview(Long userId,ReviewRequestDTO reviewRequestDTO);

	void deleteReview(Long userId,Long reviewId);

	Page<Review> reviews(Long userId, Pageable pageable);

	Page<Review> proReviews(Users user, Pageable pageable);
}
