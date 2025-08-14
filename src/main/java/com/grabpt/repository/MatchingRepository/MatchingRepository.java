package com.grabpt.repository.MatchingRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grabpt.domain.entity.Matching;
import com.grabpt.domain.enums.MatchingStatus;

public interface MatchingRepository extends JpaRepository<Matching, Long> {
	/** 트레이너 활성 회원 수 */
	@Query("SELECT COUNT(DISTINCT m.requestion.user.id) FROM Matching m " +
		"JOIN m.suggestion s " +
		"WHERE s.proProfile.id = :proProfileId " +
		"AND m.status = :status")
	Long getActiveClients(@Param("proProfileId") Long proProfileId,
		@Param("status") MatchingStatus status);

	Matching findMatchingBySuggestionId(Long suggestionId);
}
