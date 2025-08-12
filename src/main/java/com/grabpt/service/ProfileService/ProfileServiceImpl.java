package com.grabpt.service.ProfileService;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.GeneralException;
import com.grabpt.converter.CategoryConverter;
import com.grabpt.converter.ProfileConverter;
import com.grabpt.domain.entity.Address;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.entity.Review;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Role;
import com.grabpt.dto.request.CenterUpdateRequestDTO;
import com.grabpt.dto.request.CertificationUpdateRequestDTO;
import com.grabpt.dto.request.DeletedRequestDTO;
import com.grabpt.dto.request.DescriptionUpdateRequestDTO;
import com.grabpt.dto.request.ProLocationUpdateRequestDTO;
import com.grabpt.dto.request.PtPriceUpdateRequestDTO;
import com.grabpt.dto.request.PtProgramUpdateRequestDTO;
import com.grabpt.dto.request.UserProfileUpdateRequestDTO;
import com.grabpt.dto.response.CategoryResponse;
import com.grabpt.dto.response.CertificationResponseDTO;
import com.grabpt.dto.response.MyRequestListDTO;
import com.grabpt.dto.response.MyReviewListDTO;
import com.grabpt.dto.response.ProProfileResponseDTO;
import com.grabpt.dto.response.ProfileResponseDTO;
import com.grabpt.repository.ProProfileRepository.ProProfileRepository;
import com.grabpt.repository.RequestionRepository.RequestionRepository;
import com.grabpt.repository.ReviewRepository.reviewRepository;
import com.grabpt.repository.UserRepository.UserRepository;
import com.grabpt.service.CertificationService.CertificationService;
import com.grabpt.service.PhotoService.PhotoService;
import lombok.RequiredArgsConstructor;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {

	private final UserRepository userRepository;
	private final RequestionRepository requestionRepository;
	private final reviewRepository reviewRepository;

	private final PhotoService photoService;
	private final ProProfileRepository proProfileRepository;
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
		Page<Requestions> requests = requestionRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);;
		return requests.map(MyRequestListDTO::new);
	}

	@Override
	public Page<MyReviewListDTO> findMyReviews(Long userId, Pageable pageable) {
		Page<Review> reviews = reviewRepository.findAllByUserId(userId, pageable);
		return reviews.map(MyReviewListDTO::from);
	}

	@Override
	@Transactional
	public void updateMyUserProfile(Long userId, UserProfileUpdateRequestDTO request, MultipartFile profileImage) {
		Users user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

		// 1. 닉네임 수정
		user.setNickname(request.getNickname());

		// 2. 주소 정보가 있으면 주소 수정
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

		// 3. 이미지 파일이 있으면 프로필 이미지 수정 (주소 수정 로직과 분리)
		if (profileImage != null && !profileImage.isEmpty()) {
			String imageUrl = photoService.uploadProfileImage(profileImage);
			user.setProfileImageUrl(imageUrl);
		}
	}


	@Override
	public Page<MyReviewListDTO> findProReviews(Long userId, Pageable pageable) {
		Users user = findUserById(userId);
		if (user.getProProfile() == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}
		Page<Review> reviews = reviewRepository.findAllByProProfile_Id(user.getProProfile().getId(), pageable);
		return reviews.map(MyReviewListDTO::from);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<MyReviewListDTO> findReviewsByCategoryAndUserId(String categoryCode, Long userId, Pageable pageable) {

		Users user = userRepository.findById(userId) // userRepository를 주입받아야 함
			.orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

		if (user.getProProfile() == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}

		Long proProfileId = user.getProProfile().getId();

		Page<Review> reviews = reviewRepository.findAllByProProfile_IdAndProProfile_Category_Code(proProfileId, categoryCode, pageable);


		return reviews.map(MyReviewListDTO::from);
	}

	@Override
	@Transactional(readOnly = true)
	public ProProfileResponseDTO findProProfileByCategoryAndUser(String categoryCode, Long userId) {
		ProProfile proProfile = proProfileRepository.findByUserId(userId);
		if (proProfile == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}

		return ProfileConverter.toProProfileDetailDTO(proProfile);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ProProfileResponseDTO> findProProfilesByCategory(String categoryCode, Pageable pageable) {
		Page<ProProfile> proProfiles = proProfileRepository.findByCategory_Code(categoryCode, pageable);

		return proProfiles.map(ProfileConverter::toProProfileDetailDTO);
	}

	@Override
	public List<ProProfile> findAllProByCategoryCodeAndRegion(String categoryCode, String region) {
		return proProfileRepository.findAllProByCategoryCodeAndRegion(categoryCode, region);
	}

	private Users findUserById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
	}

	@Override
	@Transactional
	public void updateProCenter(Long userId, CenterUpdateRequestDTO request) {
		Users user = findUserById(userId);
		ProProfile proProfile = user.getProProfile();
		if (proProfile == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}

		String center = request.getCenter();
		String centerDescription = request.getCenterDescription();
		proProfile.setCenter(center);
		proProfile.setCenterDescription(centerDescription);
	}

	@Override
	@Transactional
	public void updateProDescription(Long userId, DescriptionUpdateRequestDTO request) {
		ProProfile proProfile = findUserById(userId).getProProfile();
		proProfile.setDescription(request.getDescription());
	}

	@Override
	@Transactional
	public void updateProPhotos(Long userId, List<MultipartFile> photoFiles) {
		ProProfile proProfile = findUserById(userId).getProProfile();
		if (proProfile == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}
		photoService.updateProPhotos(proProfile, photoFiles);
	}


	@Override
	@Transactional
	public void updateProPtPrice(Long userId, PtPriceUpdateRequestDTO request) {
		ProProfile proProfile = findUserById(userId).getProProfile();
		proProfile.setPricePerSession(request.getPricePerSession());
		proProfile.setTotalSessions(request.getTotalSessions());
	}

	@Override
	@Transactional
	public void updateProProgram(Long userId, PtProgramUpdateRequestDTO request) {
		ProProfile proProfile = findUserById(userId).getProProfile();
		proProfile.setProgramDescription(request.getProgramDescription());
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
	public CertificationResponseDTO findMyCertifications(Long userId) {
		ProProfile proProfile = findUserById(userId).getProProfile();
		return CertificationResponseDTO.from(proProfile != null ? proProfile.getCertifications() : Collections.emptyList());
	}

	@Override
	@Transactional
	public void updateProCertifications(Long userId, CertificationUpdateRequestDTO request, List<MultipartFile> images) {
		ProProfile proProfile = findUserById(userId).getProProfile();
		if (proProfile == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}
		certificationService.updateCertifications(proProfile, request.getCertifications(), images);
	}

	@Override
	@Transactional
	public void deleteUser(Long userId, DeletedRequestDTO deletedRequest) {
		Users user = userRepository.findById(userId).orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

		user.setPreviousRole(user.getRole());
		user.setRole(Role.DELETED);
		user.setDeletedAt(LocalDateTime.now());

		user.setDeletionReason(deletedRequest.getDeletionReason());
		userRepository.save(user);
	}

	@Override
	@Transactional
	public void restoreUser(Long userId) {
		Users user = userRepository.findById(userId).orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

		LocalDateTime recoveryDate = LocalDateTime.now().minusDays(30);

		if(user.getRole() == Role.DELETED && user.getDeletedAt().isAfter(recoveryDate)) {
			user.setRole(user.getPreviousRole());
			user.setDeletedAt(null);
			user.setDeletionReason(null);
		}
		else {
			throw new IllegalStateException("복구 가능 시간이 지났습니다.");
		}
	}

	@Override
	@Transactional
	public void updateProLocation(Long userId, ProLocationUpdateRequestDTO request) {
		Users user = findUserById(userId);
		ProProfile proProfile = user.getProProfile();
		if (proProfile == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}

		proProfile.setCenter(request.getCenter());
		proProfile.setCenterDescription(request.getCenterDescription());

		Address address = user.getAddress();
		if (address == null) {
			address = new Address();
			address.setUser(user);
			user.setAddress(address);
		}

		address.setCity(request.getCity());
		address.setDistrict(request.getDistrict());
		address.setStreet(request.getStreet());
		address.setZipcode(request.getZipcode());
	}

}
