package com.grabpt.service.Impl;

import com.grabpt.domain.entity.ProCertification;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.CertificationRequestDTO;
import com.grabpt.service.CertificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificationServiceImpl implements CertificationService {
	@Override
	@Transactional
	public void updateCertifications(ProProfile proProfile, List<CertificationRequestDTO> certificationDTOs) {
		proProfile.getCertifications().clear();
		if (certificationDTOs != null) {
			certificationDTOs.forEach(dto -> {
				ProCertification certification = ProCertification.builder()
					.name(dto.getName())
					.issuer(dto.getIssuer())
					.issuedDate(dto.getIssuedDate().atStartOfDay())
					.proProfile(proProfile)
					.build();
				proProfile.getCertifications().add(certification);
			});
		}
	}
}
