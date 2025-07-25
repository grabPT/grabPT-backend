package com.grabpt.dto.request;


import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class DescriptionUpdateRequestDTO {

	@Size(max = 2000, message = "소개는 최대 2000자까지 입력 가능합니다.")
	private String description;
}
