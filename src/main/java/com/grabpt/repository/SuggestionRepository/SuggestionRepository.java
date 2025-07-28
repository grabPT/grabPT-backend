package com.grabpt.repository.SuggestionRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.grabpt.domain.entity.Suggestions;

public interface SuggestionRepository extends JpaRepository<Suggestions, Long> {
	Page<Suggestions> findByRequestionId(Long requestionId, Pageable pageable);
}
