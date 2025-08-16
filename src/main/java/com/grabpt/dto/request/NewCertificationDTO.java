package com.grabpt.dto.request;

import com.grabpt.domain.enums.CertificationType;

import lombok.Getter;

@Getter
public class NewCertificationDTO {
	private String description;
	private CertificationType certificationType;
}
