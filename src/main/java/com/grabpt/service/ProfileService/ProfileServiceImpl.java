package com.grabpt.service.ProfileService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.GeneralException;
import com.grabpt.converter.ProfileConverter;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.entity.Review;
import com.grabpt.domain.entity.UserProfile;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.request.ProProfileUpdateRequestDTO;
import com.grabpt.dto.request.UserProfileUpdateRequestDTO;
import com.grabpt.dto.response.MyRequestListDTO;
import com.grabpt.dto.response.MyReviewListDTO;
import com.grabpt.dto.response.ProProfileResponseDTO;
import com.grabpt.dto.response.ProfileResponseDTO;
import com.grabpt.repository.RequestionRepository.RequestionRepository;
import com.grabpt.repository.ReviewRepository.reviewRepository;
import com.grabpt.repository.UserRepository.UserRepository;
import com.grabpt.service.CertificationService.CertificationService;
import com.grabpt.service.PhotoService.PhotoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {

	private final UserRepository userRepository;
	private final RequestionRepository requestionRepository;
	private final reviewRepository reviewRepository;

	private final PhotoService photoService;
	private final CertificationService certificationService;

	@Override
	public ProfileResponseDTO.MyProfileDTO findMyUserProfile(Long userId) {
		Users user = findUserById(userId);
		return ProfileConverter.toMyProfileDTO(user);
	}

	@Override
	public ProfileResponseDTO.MyProProfileDTO findMyProUserProfile(Long userId) {
		Users user = findUserById(userId);
		return ProfileConverter.toMyProProfileDTO(user);
	}

	@Override
	public Page<MyRequestListDTO> findMyRequests(Long userId, Pageable pageable) {
		Page<Requestions> requests = requestionRepository.findAllByUserId(userId, pageable);
		return requests.map(MyRequestListDTO::new);
	}

	@Override
	public Page<MyReviewListDTO> findMyReviews(Long userId, Pageable pageable) {
		Page<Review> reviews = reviewRepository.findAllByUserId(userId, pageable);
		return reviews.map(MyReviewListDTO::from);
	}

	@Override
	@Transactional
	public void updateMyUserProfile(Long userId, UserProfileUpdateRequestDTO request) {
		Users user = findUserById(userId);
		UserProfile userProfile = user.getUserProfile();
		if (userProfile == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}
		user.setNickname(request.getNickname());
		userProfile.setResidence(request.getResidence());
		userProfile.setPreferredAreas(request.getPreferredAreas());
	}

	@Override
	@Transactional
	public void updateMyProUserProfile(Long userId, ProProfileUpdateRequestDTO request) {
		Users user = findUserById(userId);
		ProProfile proProfile = user.getProProfile();
		if (proProfile == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}

		user.setNickname(request.getNickname());
		proProfile.setCenter(request.getCenter());
		proProfile.setCareer(request.getCareer());
		proProfile.setDescription(request.getDescription());
		proProfile.setProgramDescription(request.getProgramDescription());
		proProfile.setPricePerSession(request.getPricePerSession());
		proProfile.setTotalSessions(request.getTotalSessions());

		photoService.updatePhotos(proProfile, request.getPhotos());
		certificationService.updateCertifications(proProfile, request.getCertifications());
	}

	@Override
	public Page<MyReviewListDTO> findProReviews(Long userId, Pageable pageable) {
		Users user = findUserById(userId);
		if (user.getProProfile() == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}
		Page<Review> reviews = reviewRepository.findAllByProProfile_Id(user.getId(), pageable);
		return reviews.map(MyReviewListDTO::from);
	}

	@Override
	public ProProfileResponseDTO findProProfile(Long userId) {
		Users user = findUserById(userId);
		if (user.getProProfile() == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}
		return ProfileConverter.toProProfileDetailDTO(user);
	}

	@Override
	public Page<ProProfileResponseDTO> findProProfilesByCategory(String categoryCode, Pageable pageable) {
		Page<Users> users = userRepository.findAllByProProfile_Category_Code(categoryCode, pageable);
		return users.map(ProfileConverter::toProProfileDetailDTO);
	}

	private Users findUserById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
	}
}
