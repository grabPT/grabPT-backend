package com.grabpt.service.UserActivityService;

import com.grabpt.dto.response.MyRequestListDTO;
import com.grabpt.dto.response.MyReviewListDTO;
import com.grabpt.dto.response.MyReviewUserDTO;
import com.grabpt.dto.response.ReviewListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserActivityService {
	Page<MyRequestListDTO> findMyRequests(Long userId, Pageable pageable);
	Page<MyReviewUserDTO> findMyReviews(Long userId, Pageable pageable);
	Page<MyReviewListDTO> findProReviews(Long userId, Pageable pageable);
	Page<ReviewListDto> findProProReviews(Long userId, Pageable pageable);
	Page<MyReviewListDTO> findReviewsByUserId(Long userId, Pageable pageable);
}
