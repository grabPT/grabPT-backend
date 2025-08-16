package com.grabpt.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import lombok.Getter;


@Getter
public class CertificationUpdateRequestDTO {

	@Valid
	private List<ExistingCertificationDTO> existingCertifications;

	@Valid
	private List<NewCertificationDTO> newCertifications;
}

