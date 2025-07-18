package com.grabpt.repository.MatchingRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grabpt.domain.entity.Matching;

public interface MatchingRepository extends JpaRepository<Matching, Long> {
}
