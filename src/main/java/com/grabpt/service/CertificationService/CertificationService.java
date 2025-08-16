package com.grabpt.service.CertificationService;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.CertificationRequestDTO;
import com.grabpt.dto.request.CertificationUpdateRequestDTO;

public interface CertificationService {
	void updateCertifications(ProProfile proProfile, CertificationUpdateRequestDTO request,
		List<MultipartFile> newImages);
}
