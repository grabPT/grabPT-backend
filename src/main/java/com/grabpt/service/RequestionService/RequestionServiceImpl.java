package com.grabpt.service.RequestionService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.RequestionHandler;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.domain.entity.Address;
import com.grabpt.domain.entity.Category;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Gender;
import com.grabpt.domain.enums.RequestStatus;
import com.grabpt.dto.request.RequestionRequestDto;
import com.grabpt.dto.response.RequestionResponseDto;
import com.grabpt.dto.response.UserResponseDto;
import com.grabpt.repository.CategoryRepository.CategoryRepository;
import com.grabpt.repository.RequestionRepository.RequestionRepository;
import com.grabpt.repository.UserRepository.UserRepository;
import com.grabpt.service.AlarmService.AlarmService;
import com.grabpt.service.ProfileService.ProfileService;
import com.grabpt.service.UserService.UserQueryService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestionServiceImpl implements RequestionService {

	private final RequestionRepository requestionRepository;
	private final CategoryRepository categoryRepository;
	private final UserRepository userRepository;
	private final UserQueryService userQueryService;
	private final ProfileService profileService;
	private final AlarmService alarmService;

	@Override
	public List<Requestions> getReqeustions(String categoryCode, Pageable pageable) {
		return requestionRepository.findTop6RequestionsByCategory(categoryCode, pageable);
	}

	@Override
	public Requestions save(RequestionRequestDto dto, String email) {
		Users user = userRepository.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("사용자 없음"));

		Category category = categoryRepository.findById(dto.getCategoryId())
			.orElseThrow(() -> new RuntimeException("카테고리 없음"));

		Requestions requestion = Requestions.builder()
			.user(user)  // JWT 기반으로 추출된 사용자
			.category(category)
			.price(dto.getPrice())
			.sessionCount(dto.getSessionCount())
			.purpose(dto.getPurpose())
			.ageGroup(dto.getAgeGroup())
			.userGender(Gender.fromKorean(dto.getUserGender()))
			.availableDays(dto.getAvailableDays())
			.availableTimes(dto.getAvailableTimes())
			.trainerGender(Gender.fromKorean(dto.getTrainerGender()))
			.startPreference(dto.getStartPreference())
			.etcPurposeContent(dto.getEtcPurposeContent())
			.content(dto.getContent())
			.location(dto.getLocation())
			.status(RequestStatus.MATCHING)
			.build();
		requestion.setUser(user); // 연관관계 설정
		Requestions save = requestionRepository.save(requestion);

		String[] address = requestion.getLocation().split(" ");
		List<ProProfile> proProfiles = profileService.findAllProByCategoryCodeAndRegion(category.getCode(), address[2]);
		for (ProProfile proProfile : proProfiles) {
			alarmService.sendAlarm(proProfile.getUser().getId(), "REQUESTION", "요청서 도착",
				requestion.getUser().getNickname() + "님의 요청서가 도착했습니다.", "/matching/requests/" + requestion.getId());
		}
		return save;
	}

	@Override
	public RequestionResponseDto.RequestionDetailResponseDto getDetail(Long requestionId) {
		Requestions r = requestionRepository.findById(requestionId)
			.orElseThrow(() -> new RequestionHandler(ErrorStatus.REQUESTION_NOT_FOUND));
		return RequestionResponseDto.RequestionDetailResponseDto.from(r);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<RequestionResponseDto.RequestionResponsePagingDto> getNearbyRequestions(HttpServletRequest request,
		String sortBy,
		Pageable pageable) throws IllegalAccessException {
		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		Users findProUser = userRepository.findByEmail(userInfo.getEmail()).orElseThrow(
			() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		Address addr = findProUser.getAddress();
		String proAddressPrefix = buildAddressPrefix(addr); // "서울시 강남구 역삼동"
		log.info("RequestServiceImpl pro address prefix = {}", proAddressPrefix);

		if (proAddressPrefix.isBlank()) {
			// 주소가 비어있다면 빈 결과 반환 혹은 예외 처리 중 택1
			return Page.empty(pageable);
		}

		// String proStreet = findProUser.getAddress().getStreet();
		// log.info("RequestServiceImpl 내부 프로 address = " + proStreet);

		Page<Requestions> requestionPage;

		if ("price".equalsIgnoreCase(sortBy)) {
			requestionPage = requestionRepository
				.findByLocationStartingWithOrderByPriceDesc(proAddressPrefix, pageable);
		} else {
			requestionPage = requestionRepository
				.findByLocationStartingWithOrderByCreatedAtDesc(proAddressPrefix, pageable);
		}

		return requestionPage.map(req -> {
			Users user = req.getUser();
			String username = req.getUser().getNickname();
			String userStreet = req.getLocation(); // Requestions 주소 기준으로 수정하였습니다

			return RequestionResponseDto.RequestionResponsePagingDto.builder()
				.username(username)
				.userStreet(userStreet)
				.sessionCount(req.getSessionCount())
				.price(req.getPrice())
				.status(req.getStatus())
				.userProfileImageUrl(user.getProfileImageUrl())
				.requestionId(req.getId())
				.content(req.getContent())
				.etcPurposeContent(req.getEtcPurposeContent())
				.build();
		});
	}

	@Override
	@Transactional
	public void update(Long requestionId, RequestionRequestDto dto, String email) {
		Requestions requestion = requestionRepository.findById(requestionId)
			.orElseThrow(() -> new RequestionHandler(ErrorStatus.REQUESTION_NOT_FOUND));

		if (!requestion.getUser().getEmail().equals(email)) {
			throw new RequestionHandler(ErrorStatus.INVALID_USER); // 작성자 아님
		}

		Category category = categoryRepository.findById(dto.getCategoryId())
			.orElseThrow(() -> new RequestionHandler(ErrorStatus.CATEGORY_NOT_FOUND));

		// 값 변경
		requestion.setCategory(category);
		requestion.setPrice(dto.getPrice());
		requestion.setSessionCount(dto.getSessionCount());
		requestion.setPurpose(dto.getPurpose());
		requestion.setEtcPurposeContent(dto.getEtcPurposeContent());
		requestion.setContent(dto.getContent());
		requestion.setAgeGroup(dto.getAgeGroup());
		requestion.setUserGender(Gender.fromKorean(dto.getUserGender()));
		requestion.setAvailableDays(dto.getAvailableDays());
		requestion.setAvailableTimes(dto.getAvailableTimes());
		requestion.setTrainerGender(Gender.fromKorean(dto.getTrainerGender()));
		requestion.setStartPreference(dto.getStartPreference());
		requestion.setLocation(dto.getLocation());
	}

	@Override
	public void delete(Long requestionId, String email) {
		Requestions requestion = requestionRepository.findById(requestionId)
			.orElseThrow(() -> new RequestionHandler(ErrorStatus.REQUESTION_NOT_FOUND));

		if (!requestion.getUser().getEmail().equals(email)) {
			throw new RequestionHandler(ErrorStatus.INVALID_USER);
		}

		requestionRepository.delete(requestion);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<RequestionResponseDto.UserOwnRequestionDto> getRequestionsByUser(HttpServletRequest request,
		Pageable pageable) throws IllegalAccessException {
		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();

		Page<Requestions> requestions = requestionRepository.findAllByUserEmail(email, pageable);
		return requestions.map(RequestionResponseDto.UserOwnRequestionDto::from);
	}

	@Override
	public boolean canEditRequestion(Long requestionId, String email) {
		Requestions requestion = requestionRepository.findById(requestionId)
			.orElseThrow(() -> new RequestionHandler(ErrorStatus.REQUESTION_NOT_FOUND));

		return requestion.getUser().getEmail().equals(email);
	}

	private String buildAddressPrefix(Address addr) {
		if (addr == null)
			return "";

		List<String> parts = new ArrayList<>();
		if (addr.getCity() != null && !addr.getCity().isBlank())
			parts.add(addr.getCity().trim());
		if (addr.getDistrict() != null && !addr.getDistrict().isBlank())
			parts.add(addr.getDistrict().trim());
		if (addr.getStreet() != null && !addr.getStreet().isBlank())
			parts.add(addr.getStreet().trim());

		// "서울시 강남구 역삼동" 형태
		String joined = String.join(" ", parts).replaceAll("\\s+", " ").trim();
		return joined;
	}
}
