package com.grabpt.dto.request;

import com.grabpt.domain.enums.MatchingStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchingStatusUpdateRequest {
	@NotNull
	private MatchingStatus status;
}
