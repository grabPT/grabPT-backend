package com.grabpt.service.UserProfileService;

import com.grabpt.dto.request.DeletedRequestDTO;
import com.grabpt.dto.request.UserProfileUpdateRequestDTO;
import com.grabpt.dto.response.ProfileResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {
	ProfileResponseDTO.MyProfileDTO findMyUserProfile(Long userId);
	void updateMyUserProfile(Long userId, UserProfileUpdateRequestDTO request, MultipartFile profileImage);
	void updateUserProfileImage(Long userId, MultipartFile profileImage);
	void deleteUser(Long userId, DeletedRequestDTO deletedRequest);
	void restoreUser(Long userId);
}
