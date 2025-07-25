package com.grabpt.dto.request;

import jakarta.validation.Valid;
import lombok.Getter;
import java.util.List;

@Getter
public class CertificationUpdateRequestDTO {

	@Valid
	private List<CertificationRequestDTO> certifications;
}
