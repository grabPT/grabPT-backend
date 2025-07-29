package com.grabpt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grabpt.domain.entity.UserTermsAgreement;

public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreement, Long> {
	// 특정 유저가 동의한 약관 조회
	List<UserTermsAgreement> findByUserId(Long userId);
}
