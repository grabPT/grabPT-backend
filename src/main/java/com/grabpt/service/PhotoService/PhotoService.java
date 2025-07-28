package com.grabpt.service.PhotoService;

import java.util.List;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.PhotoRequestDTO;
import com.grabpt.dto.response.ChatResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PhotoService {
	String uploadProfileImage(MultipartFile profileImage);
	void updateProPhotos(ProProfile proProfile, List<MultipartFile> photoFiles);
}
