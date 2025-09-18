package com.grabpt.service.ProProfileService;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.GeneralException;
import com.grabpt.apiPayload.exception.handler.ProHandler;
import com.grabpt.converter.ProfileConverter;
import com.grabpt.domain.entity.*;
import com.grabpt.dto.request.*;
import com.grabpt.dto.response.CertificationResponseDTO;
import com.grabpt.dto.response.ProProfileResponseDTO;
import com.grabpt.dto.response.ProfileResponseDTO;
import com.grabpt.repository.ProProfileRepository.ProProfileRepository;
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
public class ProProfileServiceImpl implements ProProfileService {

	private final UserRepository userRepository;
	private final ProProfileRepository proProfileRepository;
	private final PhotoService photoService;
	private final CertificationService certificationService;

	@Override
	public ProfileResponseDTO.MyProProfileDTO findMyProUserProfile(Long userId) {
		Users user = findUserById(userId);
		return ProfileConverter.toMyProProfileDTO(user);
	}

	@Override
	public ProProfileResponseDTO findProProfileByUser(Long userId) {
		Users user = findUserById(userId);
		ProProfile proProfile = user.getProProfile();
		if (proProfile == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}
		return ProfileConverter.toProProfileDetailDTO(proProfile);
	}

	@Override
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
		if (address.length >= 1) street = address[address.length - 1];
		if (address.length >= 2) district = address[address.length - 2];
		if (address.length >= 3) city = address[0];
		return proProfileRepository.findAllProByCategoryCodeAndRegion(categoryCode, city, district, street);
	}

	@Override
	@Transactional
	public void updateProCenter(Long userId, CenterUpdateRequestDTO request) {
		ProProfile proProfile = findProProfileByUserId(userId);
		proProfile.setCenter(request.getCenter());
		proProfile.setCenterDescription(request.getCenterDescription());
	}

	@Override
	@Transactional
	public void updateProDescription(Long userId, DescriptionUpdateRequestDTO request) {
		ProProfile proProfile = findProProfileByUserId(userId);
		proProfile.setDescription(request.getDescription());
	}

	@Override
	@Transactional
	public void updateProPhotos(Long userId, PhotoUpdateRequestDTO updateRequest, List<MultipartFile> newPhotoFiles) {
		ProProfile proProfile = findProProfileByUserId(userId);
		photoService.updateProPhotos(proProfile, updateRequest, newPhotoFiles);
	}

	@Override
	@Transactional
	public void updateProPtPrice(Long userId, PtPriceRequest.PtPriceUpdateRequestList request) {
		ProProfile proProfile = findProProfileByUserId(userId);
		proProfile.getPtPrices().clear();

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
		ProProfile proProfile = findProProfileByUserId(userId);
		proProfile.setProgramDescription(request.getProgramDescription());
	}

	@Override
	public CertificationResponseDTO findMyCertifications(Long userId) {
		ProProfile proProfile = findUserById(userId).getProProfile();
		return CertificationResponseDTO.from(
			proProfile != null ? proProfile.getCertifications() : Collections.emptyList());
	}

	@Override
	@Transactional
	public void updateProCertifications(Long userId, CertificationUpdateRequestDTO request, List<MultipartFile> images) {
		if(images == null || images.isEmpty()) throw new GeneralException(ErrorStatus.NOT_IMAGE);

		ProProfile proProfile = findProProfileByUserId(userId);
		certificationService.updateCertifications(proProfile, request, images);
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

	@Override
	public String getProNicknameById(Long proProfileId) {
		if (proProfileId == null) {
			return null;
		}
		return proProfileRepository.findById(proProfileId)
			.map(proProfile -> proProfile.getUser())
			.map(Users::getNickname)
			.orElse(null);
	}

	@Override
	public ProProfile findByUser(Users user) {
		return proProfileRepository.findByUser(user)
			.orElseThrow(() -> new ProHandler(ErrorStatus.PRO_NOT_FOUND));
	}

	private Users findUserById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
	}

	private ProProfile findProProfileByUserId(Long userId) {
		ProProfile proProfile = findUserById(userId).getProProfile();
		if (proProfile == null) {
			throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
		}
		return proProfile;
	}
}
