package com.grabpt.service.ReviewService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.grabpt.dto.request.ReviewRequestDTO;
import com.grabpt.dto.response.MyReviewListDTO;

public interface ReviewService {
	void createReview(Long userId,ReviewRequestDTO reviewRequestDTO);

	void deleteReview(Long userId,Long reviewId);
}
