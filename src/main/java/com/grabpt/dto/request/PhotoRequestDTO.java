package com.grabpt.dto.request;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PhotoRequestDTO {
	@NotBlank
	private MultipartFile image; // String imageUrl -> MultipartFile image
	private String description;
}
