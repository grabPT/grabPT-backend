package com.grabpt.service.PhotoService;

import java.util.List;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.PhotoRequestDTO;

public interface PhotoService {
	void updatePhotos(ProProfile proProfile, List<PhotoRequestDTO> photoDTOs);
}
