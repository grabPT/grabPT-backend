package com.grabpt.service;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.PhotoRequestDTO;
import java.util.List;

public interface PhotoService {
	void updatePhotos(ProProfile proProfile, List<PhotoRequestDTO> photoDTOs);
}
