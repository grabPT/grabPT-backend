package com.grabpt.service.MatchingService;

import java.util.List;
import java.util.Optional;

import com.grabpt.domain.entity.Matching;
import com.grabpt.domain.enums.MatchingStatus;
import com.grabpt.dto.response.ContractResponse;

public interface MatchingService {
	ContractResponse.CreateMatchingAndContractResponseDto createMatching(Long requestionId, Long suggestionId);

	Matching updateStatus(Long matchingId, MatchingStatus newStatus);

	Matching findMatchingBySuggestionId(Long suggestionId);

	List<Matching> findAllWithProByRequestionIds(List<Long> requestionIds);

	boolean existsByRequestionId(Long requestionId);

	Long getActiveClients(Long proProfileId, MatchingStatus matchingStatus);

	Long getActiveContractsByUser(Long userId, MatchingStatus matchingStatus);

	Optional<Object> findById(Long matchingId);

}
