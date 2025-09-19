package com.grabpt.service.ProProfileService;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.request.*;
import com.grabpt.dto.response.CertificationResponseDTO;
import com.grabpt.dto.response.ProProfileResponseDTO;
import com.grabpt.dto.response.ProfileResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProProfileService {
	ProfileResponseDTO.MyProProfileDTO findMyProUserProfile(Long userId);
	ProProfileResponseDTO findProProfileByUser(Long userId);
	Page<ProProfileResponseDTO> findProProfilesByCategory(String categoryCode, Pageable pageable);
	List<ProProfile> findAllProByCategoryCodeAndRegion(String categoryCode, String region);
	void updateProCenter(Long userId, CenterUpdateRequestDTO request);
	void updateProDescription(Long userId, DescriptionUpdateRequestDTO request);
	void updateProPhotos(Long userId, PhotoUpdateRequestDTO updateRequest, List<MultipartFile> newPhotoFiles);
	void updateProPtPrice(Long userId, PtPriceRequest.PtPriceUpdateRequestList request);
	void updateProProgram(Long userId, PtProgramUpdateRequestDTO request);
	CertificationResponseDTO findMyCertifications(Long userId);
	void updateProCertifications(Long userId, CertificationUpdateRequestDTO request, List<MultipartFile> images);
	void updateProLocation(Long userId, ProLocationUpdateRequestDTO request);
	String getProNicknameById(Long proProfileId);
	ProProfile findByUser(Users user);
}
