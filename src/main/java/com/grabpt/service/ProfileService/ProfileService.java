package com.grabpt.service.ProfileService;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.grabpt.dto.request.ProProfileUpdateRequestDTO;
import com.grabpt.dto.request.UserProfileUpdateRequestDTO;

import java.util.List;

public interface ProfileService {
	ProfileResponseDTO.MyProfileDTO findMyUserProfile(Long userId);

	ProfileResponseDTO.MyProProfileDTO findMyProUserProfile(Long userId);

	Page<MyRequestListDTO> findMyRequests(Long userId, Pageable pageable);

	Page<MyReviewListDTO> findMyReviews(Long userId, Pageable pageable);

	void updateMyUserProfile(Long userId, UserProfileUpdateRequestDTO request);

	void updateMyProUserProfile(Long userId, ProProfileUpdateRequestDTO request);

	Page<MyReviewListDTO> findProReviews(Long userId, Pageable pageable);

	ProProfileResponseDTO findProProfile(Long userId);

	Page<ProProfileResponseDTO> findProProfilesByCategory(String categoryCode, Pageable pageable);

	List<CategoryResponse.ProListDto> findAllProByCategoryCodeAndRegion(String categoryCode, String region);
}
