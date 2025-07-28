package com.grabpt.dto.request;

import org.springframework.web.multipart.MultipartFile;

import com.grabpt.domain.enums.CertificationType;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificationRequestDTO {
	private CertificationType certificationType;
	private String description;
}
