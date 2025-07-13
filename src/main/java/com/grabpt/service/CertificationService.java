package com.grabpt.service;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.CertificationRequestDTO;
import java.util.List;

public interface CertificationService {
	void updateCertifications(ProProfile proProfile, List<CertificationRequestDTO> certificationDTOs);
}
