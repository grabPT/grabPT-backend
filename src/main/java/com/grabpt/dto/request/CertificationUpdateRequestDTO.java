package com.grabpt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Getter;
import java.util.List;

@Getter
public class CertificationUpdateRequestDTO {

	@Valid
	private List<CertificationRequestDTO> certifications;
}
