package com.grabpt.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class DeletedRequestDTO {

	@Size(max = 500, message = "탈퇴 사유는 최대 500자 까지 입니다.")
	private String deletionReason;
}
