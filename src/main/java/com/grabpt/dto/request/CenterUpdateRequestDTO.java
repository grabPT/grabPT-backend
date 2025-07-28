package com.grabpt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CenterUpdateRequestDTO {

	@NotBlank(message = "센터명은 필수 입력 항목입니다.")
	private String center;

	@NotBlank(message = "주소는 필수 입력 항목입니다.")
	private String centerDescription;
}
