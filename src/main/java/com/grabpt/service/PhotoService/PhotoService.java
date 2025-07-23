package com.grabpt.service.PhotoService;

import java.util.List;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.PhotoRequestDTO;
import com.grabpt.dto.response.ChatResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PhotoService {
	void updatePhotos(ProProfile proProfile, List<PhotoRequestDTO> photoDTOs);
}
