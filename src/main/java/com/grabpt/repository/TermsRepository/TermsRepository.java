package com.grabpt.repository.TermsRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grabpt.domain.entity.Terms;

public interface TermsRepository extends JpaRepository<Terms, Long> {
}
