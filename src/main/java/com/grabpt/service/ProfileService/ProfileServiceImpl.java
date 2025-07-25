package com.grabpt.service.ProfileService;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.GeneralException;
import com.grabpt.converter.CategoryConverter;
import com.grabpt.converter.ProfileConverter;
import com.grabpt.domain.entity.*;
import com.grabpt.dto.request.CenterUpdateRequestDTO;
import com.grabpt.dto.request.CertificationUpdateRequestDTO;
import com.grabpt.dto.request.DescriptionUpdateRequestDTO;
import com.grabpt.dto.request.ProLocationUpdateRequestDTO;
import com.grabpt.dto.request.PtPriceUpdateRequestDTO;
import com.grabpt.dto.request.PtProgramUpdateRequestDTO;
import com.grabpt.dto.request.UserProfileUpdateRequestDTO;
import com.grabpt.dto.response.*;
import com.grabpt.repository.ProProfileRepository.ProProfileRepository;
import com.grabpt.repository.RequestionRepository.RequestionRepository;
import com.grabpt.repository.ReviewRepository.reviewRepository;
import com.grabpt.repository.UserRepository.UserRepository;
import com.grabpt.service.CertificationService.CertificationService;
import com.grabpt.service.PhotoService.PhotoService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

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
	public Page<MyReviewListDTO> findProReviews(Long userId, Pageable pageable) {
		Users user = findUserById(userId);
		if (user.getProProfile() == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}
		Page<Review> reviews = reviewRepository.findAllByProProfile_Id(user.getProProfile().getId(), pageable);
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
	@Transactional(readOnly = true) // 데이터를 조회만 하므로 readOnly = true 추가 권장
	public Page<ProProfileResponseDTO> findProProfilesByCategory(String categoryCode, Pageable pageable) {
		Page<ProProfile> proProfiles = proProfileRepository.findByCategory_Code(categoryCode, pageable);

		return proProfiles.map(ProfileConverter::toProProfileDetailDTO);
	}

	@Override
	public List<CategoryResponse.ProListDto> findAllProByCategoryCodeAndRegion(String categoryCode, String region) {
		return CategoryConverter.toProListDto(proProfileRepository.
			findAllProByCategoryCodeAndRegion(categoryCode, region));
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
		// 1. 사용자 조회
		Users user = findUserById(userId);

		// 2. S3에 이미지 업로드 시도
		String newImageUrl = photoService.uploadProfileImage(profileImage);

		if (newImageUrl != null) {
			// 3-1. 사용자의 profileImageUrl을 업데이트하고,
			user.setProfileImageUrl(newImageUrl);

			// 3-2. ✅ 변경된 사용자 정보를 명시적으로 저장합니다.
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
	public void deleteUser(Long userId) {
		Users user = findUserById(userId);
		user.withdraw(); // Users 엔티티에 추가한 탈퇴 메서드 호출
	}

	@Override
	@Transactional
	public void updateProLocation(Long userId, ProLocationUpdateRequestDTO request) {
		Users user = findUserById(userId);
		ProProfile proProfile = user.getProProfile();
		if (proProfile == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}

		// 1. 센터 정보 업데이트
		proProfile.setCenter(request.getCenter());
		proProfile.setCenterDescription(request.getCenterDescription());

		// 2. 대표 주소 업데이트
		Address address = user.getAddress(); // 기존 주소 가져오기
		if (address == null) {
			address = new Address();
			address.setUser(user); // 새로 생성된 Address 객체에 Users 설정
			user.setAddress(address); // Users 객체에 새로 생성된 Address 설정
		}

		address.setCity(request.getCity());
		address.setDistrict(request.getDistrict());
		address.setStreet(request.getStreet());
		address.setZipcode(request.getZipcode());
	}

}
