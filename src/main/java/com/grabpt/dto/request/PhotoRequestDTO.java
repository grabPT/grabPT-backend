package com.grabpt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PhotoRequestDTO {
	@NotBlank
	private String imageUrl;
	private String description;
}
