package com.grabpt.service.ProfileService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.GeneralException;
import com.grabpt.converter.ProfileConverter;
import com.grabpt.domain.entity.Address;
import com.grabpt.domain.entity.Matching;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.PtPrice;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.entity.Review;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Role;
import com.grabpt.dto.request.CenterUpdateRequestDTO;
import com.grabpt.dto.request.CertificationUpdateRequestDTO;
import com.grabpt.dto.request.DeletedRequestDTO;
import com.grabpt.dto.request.DescriptionUpdateRequestDTO;
import com.grabpt.dto.request.PhotoUpdateRequestDTO;
import com.grabpt.dto.request.ProLocationUpdateRequestDTO;
import com.grabpt.dto.request.PtPriceRequest;
import com.grabpt.dto.request.PtProgramUpdateRequestDTO;
import com.grabpt.dto.request.UserProfileUpdateRequestDTO;
import com.grabpt.dto.response.CertificationResponseDTO;
import com.grabpt.dto.response.MyRequestListDTO;
import com.grabpt.dto.response.MyReviewListDTO;
import com.grabpt.dto.response.ProProfileResponseDTO;
import com.grabpt.dto.response.ProfileResponseDTO;
import com.grabpt.repository.MatchingRepository.MatchingRepository;
import com.grabpt.repository.ProProfileRepository.ProProfileRepository;
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
	private final MatchingRepository matchingRepository;

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
	@Transactional(readOnly = true)
	public Page<MyRequestListDTO> findMyRequests(Long userId, Pageable pageable) {
		// 1) 내 요청서 페이지 조회
		Page<Requestions> page = requestionRepository
			.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);

		// 2) 요청서 ID 목록 추출
		List<Long> requestionIds = page.stream()
			.map(Requestions::getId)
			.toList();

		// 요청서가 없으면 바로 매핑해서 리턴
		if (requestionIds.isEmpty()) {
			return page.map(MyRequestListDTO::new);
		}

		// 3) Matching을 요청서 ID 기준으로 한 번에 로드 (pro의 user까지 fetch join)
		List<Matching> matchings = matchingRepository.findAllWithProByRequestionIds(requestionIds);

		// 4) requestionId → Matching 매핑
		Map<Long, Matching> matchingMap = matchings.stream()
			.collect(Collectors.toMap(m -> m.getRequestion().getId(), m -> m));

		// 5) DTO 매핑 + pro 정보 세팅
		return page.map(req -> {
			MyRequestListDTO dto = new MyRequestListDTO(req);
			Matching m = matchingMap.get(req.getId());
			if (m != null) {
				// pro 닉네임 / proId 세팅
				dto.setProNickname(m.getSuggestion().getProProfile().getUser().getNickname());
				dto.setProId(m.getSuggestion().getProProfile().getId());
			}
			return dto;
		});
	}

	@Override
	public Page<MyReviewListDTO> findMyReviews(Long userId, Pageable pageable) {
		Page<Review> reviews = reviewRepository.findAllByUserIdOrderByCreatedAt(userId, pageable);
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
		Page<Review> reviews = reviewRepository.findAllByProProfile_IdOrderByCreatedAt(user.getProProfile().getId(),
			pageable);
		return reviews.map(MyReviewListDTO::from);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<MyReviewListDTO> findReviewsByUserId(Long userId, Pageable pageable) {

		Users user = userRepository.findById(userId) // userRepository를 주입받아야 함
			.orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

		if (user.getProProfile() == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}

		Long proId = user.getProProfile().getId();

		Page<Review> reviews = reviewRepository.findAllByProProfile_IdOrderByCreatedAt(proId, pageable);

		return reviews.map(MyReviewListDTO::from);
	}

	@Override
	@Transactional(readOnly = true)
	public ProProfileResponseDTO findProProfileByUser(Long userId) {
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
		String[] address = region.split(" ");
		String city = null;
		String district = null;
		String street = null;
		if (address.length >= 1)
			street = address[address.length - 1];
		if (address.length >= 2)
			district = address[address.length - 2];
		if (address.length >= 3)
			city = address[0];
		return proProfileRepository.findAllProByCategoryCodeAndRegion(categoryCode, city, district, street);
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
	public void updateProPhotos(Long userId, PhotoUpdateRequestDTO updateRequest, List<MultipartFile> newPhotoFiles) {
		ProProfile proProfile = findUserById(userId).getProProfile();
		if (proProfile == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}
		photoService.updateProPhotos(proProfile, updateRequest, newPhotoFiles);
	}

	@Override
	@Transactional
	public void updateProPtPrice(Long userId, PtPriceRequest.PtPriceUpdateRequestList request) {
		ProProfile proProfile = findUserById(userId).getProProfile();
		proProfile.getPtPrices().clear(); // 기존 ptPrices 삭제

		List<PtPriceRequest.PtPriceUpdateRequestDto> requestDtos = request.getPtPriceUpdateRequestDtoList();
		for (int i = 0; i < requestDtos.size(); i++) {
			PtPriceRequest.PtPriceUpdateRequestDto requestDto = requestDtos.get(i);
			if (i == 0) {
				proProfile.setTotalSessions(requestDto.getTotalSessions());
				proProfile.setPricePerSession(requestDto.getPricePerSession());
			} else {
				PtPrice ptPrice = new PtPrice();
				ptPrice.setSessionCount(requestDto.getTotalSessions());
				ptPrice.setPrice(requestDto.getPricePerSession());
				proProfile.getPtPrices().add(ptPrice);
			}
		}
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
		return CertificationResponseDTO.from(
			proProfile != null ? proProfile.getCertifications() : Collections.emptyList());
	}

	@Override
	@Transactional
	public void updateProCertifications(Long userId, CertificationUpdateRequestDTO request,
		List<MultipartFile> images) {
		ProProfile proProfile = findUserById(userId).getProProfile();
		if (proProfile == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}
		certificationService.updateCertifications(proProfile, request, images);
	}

	@Override
	@Transactional
	public void deleteUser(Long userId, DeletedRequestDTO deletedRequest) {
		Users user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

		user.setPreviousRole(user.getRole());
		user.setRole(Role.DELETED);
		user.setDeletedAt(LocalDateTime.now());
		user.setDeletionReason(deletedRequest.getDeletionReason());
		user.setUsername("탈퇴한 회원");
		user.setNickname("탈퇴한 회원");
		user.setEmail("deleted@" + user.getId());
		user.setOauthId(null);
		user.setOauthProvider(null);
		user.setPhone_number("");
		user.setProfileImageUrl(null);
		user.setRefreshToken(null);
		user.setUserProfile(null);
		user.setProProfile(null);

		userRepository.save(user);
	}

	@Override
	@Transactional
	public void restoreUser(Long userId) {
		Users user = userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

		LocalDateTime recoveryDate = LocalDateTime.now().minusDays(30);

		if (user.getRole() == Role.DELETED && user.getDeletedAt().isAfter(recoveryDate)) {
			user.setRole(user.getPreviousRole());
			user.setDeletedAt(null);
			user.setDeletionReason(null);
		} else {
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
