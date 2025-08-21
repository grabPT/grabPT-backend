package com.grabpt.repository.SuggestionRepository;

import static jakarta.persistence.LockModeType.*;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grabpt.domain.entity.Suggestions;

public interface SuggestionRepository extends JpaRepository<Suggestions, Long> {
	Page<Suggestions> findByRequestionId(Long requestionId, Pageable pageable);

	Page<Suggestions> findByProProfile_User_Email(String email, Pageable pageable);
	
	@Lock(PESSIMISTIC_WRITE)
	@Query("select s from Suggestions s where s.id = :id")
	Optional<Suggestions> findByIdForUpdate(@Param("id") Long id);
}
