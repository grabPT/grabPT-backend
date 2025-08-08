package com.grabpt.service.ReviewService;

import com.grabpt.dto.request.ReviewRequestDTO;

public interface ReviewService {
	void createReview(Long userId,ReviewRequestDTO reviewRequestDTO);
}
