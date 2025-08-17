package com.grabpt.service.ProfileService;

import java.util.List;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.grabpt.dto.response.CategoryResponse;
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

	void updateMyUserProfile(Long userId, UserProfileUpdateRequestDTO request, MultipartFile profileImage);


	Page<MyReviewListDTO> findProReviews(Long userId, Pageable pageable);

	ProProfileResponseDTO findProProfileByUser(Long userId);

	Page<MyReviewListDTO> findReviewsByUserId(Long userId, Pageable pageable);

	Page<ProProfileResponseDTO> findProProfilesByCategory(String categoryCode, Pageable pageable);

	List<ProProfile> findAllProByCategoryCodeAndRegion(String categoryCode, String region);
	void updateProCenter(Long userId, CenterUpdateRequestDTO request);
	void updateProDescription(Long userId, DescriptionUpdateRequestDTO request);
	void updateProPhotos(Long userId, PhotoUpdateRequestDTO updateRequest, List<MultipartFile> newPhotoFiles);
	void updateProPtPrice(Long userId, PtPriceRequest.PtPriceUpdateRequestList request);
	void updateProProgram(Long userId, PtProgramUpdateRequestDTO request);
	void updateUserProfileImage(Long userId, MultipartFile profileImage);
	CertificationResponseDTO findMyCertifications(Long userId);
	void updateProCertifications(Long userId, CertificationUpdateRequestDTO request, List<MultipartFile> images);

	void deleteUser(Long userId, DeletedRequestDTO deletedRequest);// 회원 탈퇴 메서드 선언

	void restoreUser(Long userId); // 회원복구 메서드

	void updateProLocation(Long userId, ProLocationUpdateRequestDTO request);
}
