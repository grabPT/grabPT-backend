package com.grabpt.service.CertificationService;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.CertificationRequestDTO;

public interface CertificationService {
	void updateCertifications(ProProfile proProfile, List<CertificationRequestDTO> certificationDTOs, List<MultipartFile> images);
}
