package com.grabpt.dto.request;

import com.grabpt.domain.enums.MatchingStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchingStatusUpdateRequest {
	@NotNull
	@Schema(description = "변경할 매칭 상태", example = "MATCHING")
	private MatchingStatus status;
}
