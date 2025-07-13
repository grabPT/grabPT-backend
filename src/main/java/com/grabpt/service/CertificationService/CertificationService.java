package com.grabpt.service.CertificationService;

import java.util.List;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.CertificationRequestDTO;

public interface CertificationService {
	void updateCertifications(ProProfile proProfile, List<CertificationRequestDTO> certificationDTOs);
}
