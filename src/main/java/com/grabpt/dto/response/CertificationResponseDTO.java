package com.grabpt.dto.response;

import com.grabpt.domain.entity.ProCertification;
import com.grabpt.domain.enums.CertificationType;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CertificationResponseDTO {

	private List<CertificationItem> certifications;

	@Getter
	@Builder
	public static class CertificationItem {
		private CertificationType certificationType;
		private String description;
	}

	public static CertificationResponseDTO from(List<ProCertification> certifications) {
		if (certifications == null) {
			return CertificationResponseDTO.builder().certifications(Collections.emptyList()).build();
		}

		List<CertificationItem> items = certifications.stream()
			.map(cert -> CertificationItem.builder()
				.certificationType(cert.getCertificationType())
				.description(cert.getDescription())
				.build())
			.collect(Collectors.toList());
		return CertificationResponseDTO.builder()
			.certifications(items)
			.build();
	}
}
