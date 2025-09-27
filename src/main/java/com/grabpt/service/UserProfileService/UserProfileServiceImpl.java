package com.grabpt.service.UserProfileService;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.GeneralException;
import com.grabpt.converter.ProfileConverter;
import com.grabpt.domain.entity.Address;
import com.grabpt.domain.entity.PreviousUser;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Role;
import com.grabpt.dto.request.DeletedRequestDTO;
import com.grabpt.dto.request.UserProfileUpdateRequestDTO;
import com.grabpt.dto.response.ProfileResponseDTO;
import com.grabpt.repository.UserRepository.UserRepository;
import com.grabpt.service.PhotoService.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileServiceImpl implements UserProfileService {

	private final UserRepository userRepository;
	private final PhotoService photoService;

	@Override
	public ProfileResponseDTO.MyProfileDTO findMyUserProfile(Long userId) {
		Users user = findUserById(userId);
		return ProfileConverter.toMyProfileDTO(user);
	}

	@Override
	@Transactional
	public void updateMyUserProfile(Long userId, UserProfileUpdateRequestDTO request, MultipartFile profileImage) {
		Users user = findUserById(userId);

		user.setNickname(request.getNickname());

		UserProfileUpdateRequestDTO.AddressDTO addressDto = request.getAddress();
		if (addressDto != null) {
			Address existingAddress = user.getAddress();
			if (existingAddress != null) {
				existingAddress.updateDetails(
					addressDto.getCity(),
					addressDto.getDistrict(),
					addressDto.getStreet(),
					addressDto.getSpecAddress(),
					addressDto.getZipcode()
				);
			} else {
				Address newAddress = Address.builder()
					.city(addressDto.getCity())
					.district(addressDto.getDistrict())
					.street(addressDto.getStreet())
					.zipcode(addressDto.getZipcode())
					.specAddress(addressDto.getSpecAddress())
					.build();
				user.setAddress(newAddress);
			}
		}

		if (profileImage != null && !profileImage.isEmpty()) {
			String imageUrl = photoService.uploadProfileImage(profileImage);
			user.setProfileImageUrl(imageUrl);
		}
	}

	@Override
	@Transactional
	public void updateUserProfileImage(Long userId, MultipartFile profileImage) {
		Users user = findUserById(userId);
		String newImageUrl = photoService.uploadProfileImage(profileImage);
		if (newImageUrl != null) {
			user.setProfileImageUrl(newImageUrl);
			userRepository.save(user);
		}
	}

	@Override
	@Transactional
	public void deleteUser(Long userId, DeletedRequestDTO deletedRequest) {
		Users user = findUserById(userId);

		PreviousUser previousUser = user.getPreviousUser();
		if (previousUser == null) {
			previousUser = new PreviousUser();
			previousUser.setUser(user);
			user.setPreviousUser(previousUser);
		}

		previousUser.setRole(user.getRole());
		user.setRole(Role.DELETED);
		user.setDeletedAt(LocalDateTime.now());
		user.setDeletionReason(deletedRequest.getDeletionReason());
		previousUser.setUsername(user.getUsername());
		user.setUsername("탈퇴한 회원");
		previousUser.setNickname(user.getNickname());
		user.setNickname("탈퇴한 회원");
		previousUser.setProfileImageUrl(user.getProfileImageUrl());
		user.setProfileImageUrl(null);
		previousUser.setPhone_number(user.getPhone_number());
		user.setPhone_number("010-0000-0000");
		previousUser.setEmail(user.getEmail());
		user.setEmail("deleted@" + user.getId());
		user.setAccessToken(null);
		user.setRefreshToken(null);
	}

	@Override
	@Transactional
	public void restoreUser(Long userId) {
		Users user = findUserById(userId);
		PreviousUser previousUser = user.getPreviousUser();
		LocalDateTime recoveryDate = LocalDateTime.now().minusDays(30);

		if (user.getRole() == Role.DELETED && user.getDeletedAt().isAfter(recoveryDate)) {
			user.setRole(previousUser.getRole());
			user.setNickname(previousUser.getNickname());
			user.setUsername(previousUser.getUsername());
			user.setProfileImageUrl(previousUser.getProfileImageUrl());
			user.setPhone_number(previousUser.getPhone_number());
			user.setEmail(previousUser.getEmail());
			user.setDeletedAt(null);
			user.setDeletionReason(null);
		} else {
			throw new IllegalStateException("복구 가능 시간이 지났습니다.");
		}
	}

	private Users findUserById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
	}
}
