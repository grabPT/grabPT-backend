package com.grabpt.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ProgramRequestDTO {
	@NotBlank
	private String title;
	@NotBlank
	private String description;
	private Integer pricePerSession;
	private Integer totalSessions;
}
