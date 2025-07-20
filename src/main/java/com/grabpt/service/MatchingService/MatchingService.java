package com.grabpt.service.MatchingService;

import com.grabpt.domain.entity.Matching;
import com.grabpt.domain.enums.MatchingStatus;

public interface MatchingService {
	Matching createMatching(Long requestionId, Long suggestionId);

	Matching updateStatus(Long matchingId, MatchingStatus newStatus);
}
