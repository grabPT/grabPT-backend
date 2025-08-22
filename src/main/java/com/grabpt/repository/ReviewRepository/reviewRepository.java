package com.grabpt.repository.ReviewRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.grabpt.domain.entity.Review;

public interface reviewRepository extends JpaRepository<Review, Long> {
	Page<Review> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

	Review findByUser_IdAndIdOrderByCreatedAtDesc(Long userId, Long reviewId);
	// JPA 규칙
	Page<Review> findAllByProProfile_IdOrderByCreatedAtDesc(Long proProfileId, Pageable pageable); //// 전문가용

}
