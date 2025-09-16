package com.grabpt.service.RequestionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.grabpt.repository.RequestionRepository.RequestionRepository;
import com.grabpt.service.AlarmService.AlarmService;
import com.grabpt.service.CategoryService.CategoryQueryService;
import com.grabpt.service.MatchingService.MatchingService;
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
	private final CategoryQueryService categoryQueryService;
	private final UserQueryService userQueryService;
	private final ProfileService profileService;
	private final AlarmService alarmService;
	private final MatchingService matchingService;

	@Override
	public List<Requestions> getReqeustions(String categoryCode, Pageable pageable) {
		return requestionRepository.findTop8RequestionsByCategory(categoryCode, pageable);
	}

	@Override
	public Requestions save(RequestionRequestDto dto, String email) {
		Users user = userQueryService.findByEmail(email)
			.orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		Category category = categoryQueryService.findById(dto.getCategoryId());

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

		List<ProProfile> proProfiles = profileService.findAllProByCategoryCodeAndRegion(category.getCode(),
			requestion.getLocation());
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
		Users findProUser = userQueryService.findByEmail(userInfo.getEmail()).orElseThrow(
			() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

		Address addr = findProUser.getAddress();
		String proAddressPrefix = buildAddressPrefix(addr); // "서울 강남구 역삼동"
		log.info("RequestServiceImpl pro address prefix = {}", proAddressPrefix);

		if (proAddressPrefix.isBlank()) {
			// 주소가 비어있다면 빈 결과 반환
			return Page.empty(pageable);
		}

		Page<Requestions> requestionPage;
		Category category = findProUser.getProProfile().getCategory();
		if ("price".equalsIgnoreCase(sortBy)) {
			requestionPage = requestionRepository
				.findByLocationStartingWithAndCategoryOrderByPriceDesc(proAddressPrefix, category, pageable);
		} else {
			requestionPage = requestionRepository
				.findByLocationStartingWithAndCategoryOrderByCreatedAtDesc(proAddressPrefix, category, pageable);
		}

		return requestionPage.map(req -> {
			Users u = req.getUser(); // 한번만 접근해 지역 변수에 담아 사용 (4) 미세 최적화
			return RequestionResponseDto.RequestionResponsePagingDto.builder()
				.username(u.getNickname())
				.userStreet(req.getLocation())
				.sessionCount(req.getSessionCount())
				.price(req.getPrice())
				.categoryName(req.getCategory().getName())
				.availableDays(req.getAvailableDays())
				.availableTimes(req.getAvailableTimes())
				.status(req.getStatus())
				.userProfileImageUrl(u.getProfileImageUrl())
				.requestId(req.getId())
				.location(req.getLocation())
				.content(req.getContent())
				.nickname(u.getNickname())
				.etcPurposeContent(req.getEtcPurposeContent())
				.build();
		});
	}

	@Override
	@Transactional
	public void update(Long requestionId, RequestionRequestDto dto, String email) {
		Requestions requestion = requestionRepository.findById(requestionId)
			.orElseThrow(() -> new RequestionHandler(ErrorStatus.REQUESTION_NOT_FOUND));

		assertOwner(requestion, email);

		Category category = categoryQueryService.findById(dto.getCategoryId());

		// 값 변경
		var cmd = com.grabpt.dto.request.RequestionUpdateDto.builder()
			.category(category)
			.price(dto.getPrice())
			.sessionCount(dto.getSessionCount())
			.purpose(dto.getPurpose())
			.etcPurposeContent(dto.getEtcPurposeContent())
			.content(dto.getContent())
			.ageGroup(dto.getAgeGroup())
			.userGender(Gender.fromKorean(dto.getUserGender()))
			.availableDays(dto.getAvailableDays())
			.availableTimes(dto.getAvailableTimes())
			.trainerGender(Gender.fromKorean(dto.getTrainerGender()))
			.startPreference(dto.getStartPreference())
			.location(dto.getLocation())
			.build();

		requestion.applyUpdate(cmd);
	}

	@Override
	@Transactional
	public void delete(Long requestionId, String email) {
		// 잠금 후 조회 (동시성 안전)
		Requestions requestion = requestionRepository.findByIdForUpdate(requestionId)
			.orElseThrow(() -> new RequestionHandler(ErrorStatus.REQUESTION_NOT_FOUND));

		// 소유자 검사
		if (!requestion.getUser().getEmail().equals(email)) {
			throw new RequestionHandler(ErrorStatus.REQUESTION_DELETE_NOT_OWNER);
		}

		// 상태 검사: MATCHING(= 미매칭 상태)일 때만 삭제 허용
		if (requestion.getStatus() != RequestStatus.MATCHING) {
			throw new RequestionHandler(ErrorStatus.REQUESTION_DELETE_NOT_ALLOWED);
		}

		// 안전장치: 매칭 레코드가 이미 존재하면 삭제 불가
		if (matchingService.existsByRequestionId(requestionId)) {
			throw new RequestionHandler(ErrorStatus.REQUESTION_DELETE_NOT_ALLOWED);
		}

		// 5) 삭제
		requestionRepository.delete(requestion);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<RequestionResponseDto.UserOwnRequestionDto> getRequestionsByUser(HttpServletRequest request,
		Pageable pageable) throws IllegalAccessException, NullPointerException {
		UserResponseDto.UserInfoDTO userInfo = userQueryService.getUserInfo(request);
		String email = userInfo.getEmail();

		Page<Requestions> page = requestionRepository.findAllByUserEmail(email, pageable);

		// 1) 현재 페이지의 요청서 IDs
		List<Long> reqIds = page.getContent().stream()
			.map(Requestions::getId)
			.toList();

		// 2) 요청서 ID들에 대한 매칭을 한 번에 로드
		var matchings = matchingService.findAllWithProByRequestionIds(reqIds);

		// 3) reqId -> proProfileId 맵 구성
		var reqIdToProId = matchings.stream()
			.collect(Collectors.toMap(
				m -> m.getRequestion().getId(),
				m -> m.getSuggestion().getProProfile().getId()
			));

		// 4) DTO 매핑 시 proProfileId 주입 (없으면 null)
		return page.map(req -> {
			Long proProfileId = reqIdToProId.get(req.getId());

			String proNickname = profileService.getProNicknameById(proProfileId);

			return RequestionResponseDto.UserOwnRequestionDto.from(req, proProfileId, proNickname);
		});
	}

	@Override
	public boolean canEditRequestion(Long requestionId, String email) {
		Requestions requestion = requestionRepository.findById(requestionId)
			.orElseThrow(() -> new RequestionHandler(ErrorStatus.REQUESTION_NOT_FOUND));

		return isOwner(requestion, email);
	}

	@Override
	public Requestions findById(Long requestionId) {
		return requestionRepository.findById(requestionId)
			.orElseThrow(() -> new RequestionHandler(ErrorStatus.REQUESTION_NOT_FOUND));
	}

	@Override
	public Optional<Object> findByIdForUpdate(Long requestionId) {
		return Optional.ofNullable(requestionRepository.findByIdForUpdate(requestionId)
			.orElse(null));
	}

	@Override
	public Requestions save(Requestions requestions) {
		return requestionRepository.save(requestions);
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

		// "서울 강남구 역삼동" 형태
		String joined = String.join(" ", parts).replaceAll("\\s+", " ").trim();
		return joined;
	}

	private void assertOwner(Requestions r, String email) {
		if (!isOwner(r, email)) {
			throw new RequestionHandler(ErrorStatus.INVALID_USER);
		}
	}

	private boolean isOwner(Requestions r, String email) {
		return r.getUser() != null
			&& r.getUser().getEmail() != null
			&& r.getUser().getEmail().equalsIgnoreCase(email);
	}

}
