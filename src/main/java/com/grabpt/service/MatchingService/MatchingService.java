package com.grabpt.service.MatchingService;

import java.util.List;

import com.grabpt.domain.entity.Matching;
import com.grabpt.domain.enums.MatchingStatus;
import com.grabpt.dto.response.ContractResponse;

public interface MatchingService {
	ContractResponse.CreateMatchingAndContractResponseDto createMatching(Long requestionId, Long suggestionId);

	Matching updateStatus(Long matchingId, MatchingStatus newStatus);

	Matching findMatchingBySuggestionId(Long suggestionId);

	List<Matching> findAllWithProByRequestionIds(List<Long> requestionIds);

	boolean existsByRequestionId(Long requestionId);
}
