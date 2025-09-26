package com.grabpt.service.AuthService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.AuthHandler;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.aws.s3.AmazonS3Manager;
import com.grabpt.aws.s3.Uuid;
import com.grabpt.config.auth.PrincipalDetails;
import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.config.jwt.properties.CookieSupport;
import com.grabpt.config.oauth.DynamicCookieSupport;
import com.grabpt.domain.entity.Address;
import com.grabpt.domain.entity.Category;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Terms;
import com.grabpt.domain.entity.UserProfile;
import com.grabpt.domain.entity.UserTermsAgreement;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.AuthRole;
import com.grabpt.domain.enums.Gender;
import com.grabpt.domain.enums.Role;
import com.grabpt.dto.request.RefreshTokenRequestDto;
import com.grabpt.dto.request.SignupRequest;
import com.grabpt.repository.TermsRepository.TermsRepository;
import com.grabpt.repository.UserRepository.UserRepository;
import com.grabpt.repository.UserTermsAgreementRepository;
import com.grabpt.service.CategoryService.CategoryQueryService;
import com.grabpt.service.UserService.UserQueryService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final CategoryQueryService categoryQueryService;
	private final TermsRepository termsRepository;
	private final UserTermsAgreementRepository userTermsAgreementRepository;
	private final AmazonS3Manager amazonS3Manager;
	private final UserDetailsService userDetailsService;
	private final UserQueryService userQueryService;

	private static final String SHARED_DOMAIN = "grabpt.com";

	@Override
	public void registerUser_photo(SignupRequest.UserSignupRequestDto req,
		MultipartFile profileImage,
		HttpServletResponse response) {

		// 프로필 사진 S3 업로드
		String imageUrl = null;
		if (profileImage != null && !profileImage.isEmpty()) {
			Uuid uuid = Uuid.builder().uuid(UUID.randomUUID().toString()).build();
			String keyName = amazonS3Manager.generateProfilePhotoKeyName(uuid);
			imageUrl = amazonS3Manager.uploadFile(keyName, profileImage);
		}

		// 카테고리 조회
		Category userCategory = categoryQueryService.findById(req.getCategoryId());

		// UserProfile 생성
		UserProfile userProfile = UserProfile.builder()
			.category(userCategory)
			.build();

		// Address 생성
		SignupRequest.UserSignupRequestDto.AddressRequest addressDto = req.getAddress();
		Address address = Address.builder()
			.city(addressDto.getCity())
			.district(addressDto.getDistrict())
			.street(addressDto.getStreet())
			.zipcode(addressDto.getZipcode())
			.streetCode(addressDto.getStreetCode())
			.specAddress(addressDto.getSpecAddress())
			.build();

		// Users 생성 및 연관관계 설정
		Users user = Users.builder()
			.username(req.getUserName())
			.email(req.getEmail())
			.phone_number(req.getPhoneNumber())
			.address(address)
			.nickname(req.getUserNickname())
			.role(mapToRole(req.getRole()))
			.authRole(AuthRole.ROLE_USER)
			.profileImageUrl(imageUrl)  // S3 URL 저장
			.agreeMarketing(req.getIsAgreeMarketing())
			.agreeMarketingAt(req.getIsAgreeMarketing() ? LocalDateTime.now() : null)
			.oauthId(URLDecoder.decode(req.getOauthId(), StandardCharsets.UTF_8))
			.oauthProvider(URLDecoder.decode(req.getOauthProvider(), StandardCharsets.UTF_8))
			.userProfile(userProfile)
			.build();

		userProfile.setUser(user);
		address.setUser(user);

		Users savedUser = userRepository.save(user);

		// 필수 약관 동의 저장
		saveUserAgreements(savedUser, req.getAgreedTermsIds());

		// 토큰 생성 및 쿠키 세팅
		createTokenAndSetCookie(savedUser, response);
	}

	@Override
	public void registerPro_photo(SignupRequest.ProSignupRequestDto req,
		MultipartFile profileImage,
		HttpServletResponse response) {

		// 프로필 사진 S3 업로드
		String imageUrl = null;
		if (profileImage != null && !profileImage.isEmpty()) {
			Uuid uuid = Uuid.builder().uuid(UUID.randomUUID().toString()).build();
			String keyName = amazonS3Manager.generateProfilePhotoKeyName(uuid);
			imageUrl = amazonS3Manager.uploadFile(keyName, profileImage);
		}

		// 카테고리 조회
		Category proCategory = categoryQueryService.findById(req.getCategoryId());

		// ProProfile 생성
		ProProfile proProfile = ProProfile.builder()
			.center(req.getCenterName())
			.career(req.getCareer())
			.category(proCategory)
			.age(req.getAge())
			.build();

		// Address 생성
		SignupRequest.ProSignupRequestDto.AddressRequest addressDto = req.getAddress();
		Address address = Address.builder()
			.city(addressDto.getCity())
			.district(addressDto.getDistrict())
			.street(addressDto.getStreet())
			.zipcode(addressDto.getZipcode())
			.streetCode(addressDto.getStreetCode())
			.specAddress(addressDto.getSpecAddress())
			.build();

		// Users 생성 및 연관관계 설정
		Users user = Users.builder()
			.username(req.getUserName())
			.email(req.getEmail())
			.phone_number(req.getPhoneNumber())
			.address(address)
			.nickname(req.getUserNickname())
			.role(mapToRole(req.getRole()))
			.gender(mapToGender(req.getGender()))
			.authRole(AuthRole.ROLE_USER)
			.profileImageUrl(imageUrl)  // S3 URL 저장
			.agreeMarketing(req.getIsAgreeMarketing())
			.agreeMarketingAt(req.getIsAgreeMarketing() ? LocalDateTime.now() : null)
			.oauthId(URLDecoder.decode(req.getOauthId(), StandardCharsets.UTF_8))
			.oauthProvider(URLDecoder.decode(req.getOauthProvider(), StandardCharsets.UTF_8))
			.proProfile(proProfile)
			.build();

		proProfile.setUser(user);
		address.setUser(user);

		Users savedUser = userRepository.save(user);

		// 필수 약관 동의 저장
		saveUserAgreements(savedUser, req.getAgreedTermsIds());

		// 토큰 생성 및 쿠키 세팅
		createTokenAndSetCookie(savedUser, response);
	}

	@Override
	public void saveUserAgreements(Users user, List<Long> agreedTermsIds) {
		if (agreedTermsIds == null || agreedTermsIds.isEmpty())
			return;

		List<Terms> termsList = termsRepository.findAllById(agreedTermsIds);
		for (Terms terms : termsList) {
			UserTermsAgreement agreement = UserTermsAgreement.builder()
				.user(user)
				.terms(terms)
				.agreed(true)
				.agreedAt(LocalDateTime.now())
				.build();
			userTermsAgreementRepository.save(agreement);
		}
	}

	@Override
	public void createTokenAndSetCookie(Users user, HttpServletResponse response) {
		String accessToken = jwtTokenProvider.generateToken(user);
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
		String role = user.getRole().name();

		log.info("JWT 토큰 생성: {}", accessToken);

		user.setRefreshToken(refreshToken);
		userRepository.save(user);

		// 쿠키 생성
		// 1) accessToken (HttpOnly)
		addCookie(response, "accessToken", accessToken, Duration.ofMinutes(30), true);

		// 2) refreshToken (HttpOnly)
		addCookie(response, "refreshToken", refreshToken, Duration.ofDays(7), true);

		// 3) role (Base64, 프론트에서 읽어야 하므로 HttpOnly=false)
		String cookieRole = user.getRole().toString();
		addCookie(response, "role", b64(cookieRole), Duration.ofMinutes(30), false);

		// userId 쿠키 추가
		addCookie(response, "userId", b64(user.getId().toString()), Duration.ofMinutes(30), false);
	}

	@Override
	public Gender mapToGender(int genderCode) {
		switch (genderCode) {
			case 1:
				return Gender.MALE;
			case 2:
				return Gender.FEMALE;
			default:
				throw new UserHandler(ErrorStatus.INVALID_GENDER);
		}
	}

	@Override
	public Role mapToRole(int userTypeCode) {
		switch (userTypeCode) {
			case 1:
				return Role.USER;
			case 2:
				return Role.PRO;
			default:
				throw new UserHandler(ErrorStatus.INVALID_ROLE);
		}
	}

	@Override
	public void reissueTokens(HttpServletRequest request, HttpServletResponse response) {
		// 1) 쿠키에서 refresh 읽기 (신/구 이름 모두 허용)
		String refreshToken = findCookie(request, "REFRESH_TOKEN", "refreshToken");
		if (refreshToken == null || refreshToken.isBlank()) {
			log.warn("[REISSUE] missing refresh cookie");
			throw new AuthHandler(ErrorStatus.AUTH_MISSING_REFRESH_COOKIE);
		}

		// 2) 유효성 검사
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			try {
				jwtTokenProvider.getUserEmail(refreshToken);
			} catch (io.jsonwebtoken.ExpiredJwtException e) {
				log.warn("[REISSUE] expired refresh token");
				throw new AuthHandler(ErrorStatus.AUTH_EXPIRED);
			}
			log.warn("[REISSUE] invalid/expired refresh token");
			throw new AuthHandler(ErrorStatus.AUTH_INVALID_OR_EXPIRED);
		}

		String email = jwtTokenProvider.getUserEmail(refreshToken);
		Users user = userQueryService.findByEmail(email).orElse(null);
		if (user == null) {
			log.warn("[REISSUE] user not found: {}", email);
			throw new AuthHandler(ErrorStatus.AUTH_USER_NOT_FOUND);
		}

		String stored = user.getRefreshToken();
		if (stored == null) {
			log.warn("[REISSUE] stored refresh is null for {}", email);
			throw new AuthHandler(ErrorStatus.AUTH_STORED_REFRESH_NULL);
		}
		if (!refreshToken.equals(stored)) {
			log.warn("[REISSUE] refresh mismatch for {}", email);
			throw new AuthHandler(ErrorStatus.AUTH_REFRESH_MISMATCH);
		}

		// 3) 재발급(회전)
		var userDetails = userDetailsService.loadUserByUsername(email);
		var authentication = new UsernamePasswordAuthenticationToken(
			userDetails, null, userDetails.getAuthorities());

		String newAccessToken = jwtTokenProvider.generateToken(authentication);
		String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

		user.setRefreshToken(newRefreshToken);
		userQueryService.save(user);

		// 4) 쿠키 재설정 (동적 속성)
		var access = DynamicCookieSupport.newCookie("ACCESS_TOKEN", newAccessToken, request)
			.maxAge(java.time.Duration.ofHours(4)).build();
		var refresh = DynamicCookieSupport.newCookie("REFRESH_TOKEN", newRefreshToken, request)
			.maxAge(java.time.Duration.ofDays(30)).build();

		response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, access.toString());
		response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, refresh.toString());
	}

	private static String findCookie(HttpServletRequest request, String... names) {
		var cs = request.getCookies();
		if (cs == null)
			return null;
		for (String n : names) {
			for (var c : cs)
				if (n.equals(c.getName()))
					return c.getValue();
		}
		return null;
	}

	@Override
	public void logout(RefreshTokenRequestDto body, HttpServletRequest req, HttpServletResponse res,
		Authentication authentication) {
		log.info("[LOGOUT] start");

		// 1) 쿠키 삭제(레거시 포함) — 기존 유틸 재사용
		for (var c : CookieSupport.logoutDeletionSet()) {
			res.addHeader(HttpHeaders.SET_COOKIE, c.toString());
			log.info("[LOGOUT] Set-Cookie -> {}", c);
		}

		// 2) refresh 토큰 확보: 바디 우선 > 쿠키(신/구 이름 허용)
		String refresh = null;
		if (body != null && body.getRefreshToken() != null && !body.getRefreshToken().isBlank()) {
			refresh = body.getRefreshToken();
		} else {
			refresh = findCookie(req, "REFRESH_TOKEN", "refreshToken");
		}

		boolean revoked = false;

		// 3) DB refreshToken 무효화
		// 3-1) 바디/쿠키에서 가져온 refresh가 있고, 유효하면 이메일로 무효화
		if (refresh != null && !refresh.isBlank()) {
			if (jwtTokenProvider.validateToken(refresh)) {
				String email = jwtTokenProvider.getUserEmail(refresh);
				userQueryService.findByEmail(email).ifPresent(u -> {
					u.setRefreshToken(null);
					userQueryService.save(u);
					log.info("[LOGOUT] refreshToken removed by email");
				});
				revoked = true;
			} else {
				// (선택) 요청 바디로 들어온 refresh가 '명백히' 유효하지 않다면 401로 처리하고 싶을 때:
				// - 로그아웃을 항상 200으로 유지하고 싶다면 아래 3줄을 제거하시면 됩니다.
				if (body != null && body.getRefreshToken() != null) {
					throw new AuthHandler(ErrorStatus.AUTH_INVALID_OR_EXPIRED);
				}
				// 바디가 아니고 쿠키에서만 온 경우엔 조용히 계속 진행(idempotent)
			}
		}

		// 3-2) (보조) 인증 정보가 있다면 그 유저의 refreshToken 도 제거
		if (!revoked && authentication != null && authentication.getPrincipal() instanceof PrincipalDetails pd) {
			Users u = pd.getUser();
			u.setRefreshToken(null);
			userQueryService.save(u);
			log.info("[LOGOUT] refreshToken removed by authentication principal");
		}

		// 4) 세션 & 시큐리티 컨텍스트 정리 (항상)
		var session = req.getSession(false);
		if (session != null)
			session.invalidate();
		SecurityContextHolder.clearContext();

		log.info("[LOGOUT] completed");
	}

	private static void addCookie(HttpServletResponse res, String name, String value,
		Duration maxAge, boolean httpOnly) {
		ResponseCookie c = ResponseCookie.from(name, value == null ? "" : value)
			.domain(SHARED_DOMAIN)   // 앞에 점(.) 금지
			.path("/")
			.maxAge(maxAge)
			.secure(true)            // HTTPS 전제
			.httpOnly(httpOnly)      // 프론트에서 읽을 값(role)은 false
			.sameSite("None")        // 서브도메인 간 쿠키 공유를 위해 필수
			.build();
		res.addHeader(HttpHeaders.SET_COOKIE, c.toString());
	}

	private static String b64(String s) {
		if (s == null)
			return "";
		return Base64.getEncoder()
			.encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

}
