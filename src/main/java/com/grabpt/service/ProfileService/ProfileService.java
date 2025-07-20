package com.grabpt.service.ProfileService;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.grabpt.dto.request.CenterUpdateRequestDTO;
import com.grabpt.dto.request.CertificationRequestDTO;
import com.grabpt.dto.request.CertificationUpdateRequestDTO;
import com.grabpt.dto.request.DescriptionUpdateRequestDTO;

import com.grabpt.dto.request.PtPriceUpdateRequestDTO;
import com.grabpt.dto.request.PtProgramUpdateRequestDTO;
import com.grabpt.dto.request.UserProfileUpdateRequestDTO;
import com.grabpt.dto.response.CertificationResponseDTO;
import com.grabpt.dto.response.MyRequestListDTO;
import com.grabpt.dto.response.MyReviewListDTO;
import com.grabpt.dto.response.ProProfileResponseDTO;
import com.grabpt.dto.response.ProfileResponseDTO;

public interface ProfileService {
	ProfileResponseDTO.MyProfileDTO findMyUserProfile(Long userId);

	ProfileResponseDTO.MyProProfileDTO findMyProUserProfile(Long userId);

	Page<MyRequestListDTO> findMyRequests(Long userId, Pageable pageable);

	Page<MyReviewListDTO> findMyReviews(Long userId, Pageable pageable);

	void updateMyUserProfile(Long userId, UserProfileUpdateRequestDTO request);

	Page<MyReviewListDTO> findProReviews(Long userId, Pageable pageable);

	ProProfileResponseDTO findProProfile(Long userId);

	Page<ProProfileResponseDTO> findProProfilesByCategory(String categoryCode, Pageable pageable);

	void updateProCenter(Long userId, CenterUpdateRequestDTO request);

	void updateProDescription(Long userId, DescriptionUpdateRequestDTO request);
	void updateProPhotos(Long userId, List<MultipartFile> photoFiles);
	void updateProPtPrice(Long userId, PtPriceUpdateRequestDTO request);
	void updateProProgram(Long userId, PtProgramUpdateRequestDTO request);
	void updateUserProfileImage(Long userId, MultipartFile profileImage);
	CertificationResponseDTO findMyCertifications(Long userId);
	void updateProCertifications(Long userId, CertificationUpdateRequestDTO request, List<MultipartFile> images);

	void deleteUser(Long userId); // 회원 탈퇴 메서드 선언
}
