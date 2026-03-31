package com.grabpt.service.ProfileService;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.request.*;
import com.grabpt.dto.request.ProSearchRequest;
import com.grabpt.dto.response.*;
import com.grabpt.dto.response.ProSearchResponse;
import com.grabpt.service.ProProfileService.ProProfileService;
import com.grabpt.service.UserActivityService.UserActivityService;
import com.grabpt.service.UserProfileService.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileFacade implements ProfileService {

	private final UserProfileService userProfileService;
	private final ProProfileService proProfileService;
	private final UserActivityService userActivityService;

	@Override
	public ProfileResponseDTO.MyProfileDTO findMyUserProfile(Long userId) {
		return userProfileService.findMyUserProfile(userId);
	}

	@Override
	public ProfileResponseDTO.MyProProfileDTO findMyProUserProfile(Long userId) {
		return proProfileService.findMyProUserProfile(userId);
	}

	@Override
	public Page<MyRequestListDTO> findMyRequests(Long userId, Pageable pageable) {
		return userActivityService.findMyRequests(userId, pageable);
	}

	@Override
	public Page<MyReviewUserDTO> findMyReviews(Long userId, Pageable pageable) {
		return userActivityService.findMyReviews(userId, pageable);
	}

	@Override
	@Transactional
	public void updateMyUserProfile(Long userId, UserProfileUpdateRequestDTO request, MultipartFile profileImage) {
		userProfileService.updateMyUserProfile(userId, request, profileImage);
	}

	@Override
	public Page<MyReviewListDTO> findProReviews(Long userId, Pageable pageable) {
		return userActivityService.findProReviews(userId, pageable);
	}

	@Override
	public ProProfileResponseDTO findProProfileByUser(Long userId) {
		return proProfileService.findProProfileByUser(userId);
	}

	@Override
	public Page<ReviewListDto> findProProReviews(Long userId, Pageable pageable) {
		return userActivityService.findProProReviews(userId, pageable);
	}

	@Override
	public Page<ProProfileResponseDTO> findProProfilesByCategory(String categoryCode, Pageable pageable) {
		return proProfileService.findProProfilesByCategory(categoryCode, pageable);
	}

	@Override
	public List<ProProfile> findAllProByCategoryCodeAndRegion(String categoryCode, String region) {
		return proProfileService.findAllProByCategoryCodeAndRegion(categoryCode, region);
	}

	@Override
	@Transactional
	public void updateProCenter(Long userId, CenterUpdateRequestDTO request) {
		proProfileService.updateProCenter(userId, request);
	}

	@Override
	@Transactional
	public void updateProDescription(Long userId, DescriptionUpdateRequestDTO request) {
		proProfileService.updateProDescription(userId, request);
	}

	@Override
	@Transactional
	public void updateProPhotos(Long userId, PhotoUpdateRequestDTO updateRequest, List<MultipartFile> newPhotoFiles) {
		proProfileService.updateProPhotos(userId, updateRequest, newPhotoFiles);
	}

	@Override
	@Transactional
	public void updateProPtPrice(Long userId, PtPriceRequest.PtPriceUpdateRequestList request) {
		proProfileService.updateProPtPrice(userId, request);
	}

	@Override
	@Transactional
	public void updateProProgram(Long userId, PtProgramUpdateRequestDTO request) {
		proProfileService.updateProProgram(userId, request);
	}

	@Override
	@Transactional
	public void updateUserProfileImage(Long userId, MultipartFile profileImage) {
		userProfileService.updateUserProfileImage(userId, profileImage);
	}

	@Override
	public CertificationResponseDTO findMyCertifications(Long userId) {
		return proProfileService.findMyCertifications(userId);
	}

	@Override
	@Transactional
	public void updateProCertifications(Long userId, CertificationUpdateRequestDTO request, List<MultipartFile> images) {
		proProfileService.updateProCertifications(userId, request, images);
	}

	@Override
	@Transactional
	public void deleteUser(Long userId, DeletedRequestDTO deletedRequest) {
		userProfileService.deleteUser(userId, deletedRequest);
	}

	@Override
	@Transactional
	public void restoreUser(Long userId) {
		userProfileService.restoreUser(userId);
	}

	@Override
	@Transactional
	public void updateProLocation(Long userId, ProLocationUpdateRequestDTO request) {
		proProfileService.updateProLocation(userId, request);
	}

	@Override
	public String getProNicknameById(Long proProfileId) {
		return proProfileService.getProNicknameById(proProfileId);
	}

	@Override
	public ProProfile findByUser(Users user) {
		return proProfileService.findByUser(user);
	}

	@Override
	public Page<ProSearchResponse> searchProfiles(ProSearchRequest request, Pageable pageable) {
		return proProfileService.searchProfiles(request, pageable);
	}
}
