package com.grabpt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class CertificationRequestDTO {
	@NotBlank
	private String name;
	@NotBlank
	private String issuer;
	@NotNull
	private LocalDate issuedDate;
}
