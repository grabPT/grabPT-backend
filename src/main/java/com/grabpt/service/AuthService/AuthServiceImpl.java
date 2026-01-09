package com.grabpt.service.AuthService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
import com.grabpt.config.jwt.properties.CookieManagerV2;
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

	@Override
	public void registerUser_photo(SignupRequest.UserSignupRequestDto req,
		MultipartFile profileImage,
		HttpServletResponse response,
		HttpServletRequest request) {

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
			.profileImageUrl(imageUrl)
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

		// 토큰 생성 및 쿠키 세팅 (request 전달)
		createTokenAndSetCookie(savedUser, response, request);
	}

	@Override
	public void registerPro_photo(SignupRequest.ProSignupRequestDto req,
		MultipartFile profileImage,
		HttpServletResponse response,
		HttpServletRequest request) {

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
			.profileImageUrl(imageUrl)
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

		// 토큰 생성 및 쿠키 세팅 (request 전달)
		createTokenAndSetCookie(savedUser, response, request);
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

	/**
	 * 토큰 생성 및 쿠키 설정
	 * - CookieManagerV2를 사용하여 환경별 자동 처리
	 */
	@Override
	public void createTokenAndSetCookie(Users user, HttpServletResponse response, HttpServletRequest request) {
		String accessToken = jwtTokenProvider.generateToken(user);
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

		log.info("[AUTH] JWT 토큰 생성 완료: userId={}", user.getId());

		// DB에 Refresh Token 저장
		user.setRefreshToken(refreshToken);
		userRepository.save(user);

		// 쿠키 설정 (환경별 자동 처리)
		CookieManagerV2.setAccessToken(response, request, accessToken);
		CookieManagerV2.setRefreshToken(response, request, refreshToken);
		CookieManagerV2.setRole(response, request, user.getRole().name());
		CookieManagerV2.setUserId(response, request, user.getId().toString());

		log.info("[AUTH] 쿠키 설정 완료: userId={}, role={}", user.getId(), user.getRole());
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

	/**
	 * Authorization 헤더에서 토큰 추출
	 * - "Bearer {token}" 형식에서 토큰만 추출
	 * - Bearer 없이 토큰만 있는 경우도 처리
	 */
	private String extractTokenFromHeader(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || authHeader.isBlank()) {
			return null;
		}

		// "Bearer " 접두사 제거
		if (authHeader.startsWith("Bearer ")) {
			return authHeader.substring(7).trim();
		}

		// Bearer 없이 토큰만 있는 경우
		return authHeader.trim();
	}

	/**
	 * 토큰 재발급
	 * - Authorization 헤더 우선, 쿠키 fallback
	 * - 환경별 자동 처리 (CookieManagerV2 사용)
	 */
	@Override
	public void reissueTokens(HttpServletRequest request, HttpServletResponse response) {
		log.info("[REISSUE] 토큰 재발급 요청");

		// 1) Refresh Token 확보 (우선순위: Authorization 헤더 > 쿠키)
		String refreshToken = extractTokenFromHeader(request);
		if (refreshToken == null || refreshToken.isBlank()) {
			log.debug("[REISSUE] Refresh token not found in header, trying cookie");
			refreshToken = CookieManagerV2.getRefreshToken(request);
		} else {
			log.debug("[REISSUE] Refresh token found in Authorization header");
		}

		if (refreshToken == null || refreshToken.isBlank()) {
			log.warn("[REISSUE] Refresh token not found in both header and cookie");
			throw new AuthHandler(ErrorStatus.AUTH_MISSING_REFRESH_COOKIE);
		}

		// 2) 유효성 검사
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			try {
				jwtTokenProvider.getUserEmail(refreshToken);
			} catch (io.jsonwebtoken.ExpiredJwtException e) {
				log.warn("[REISSUE] Refresh token expired");
				throw new AuthHandler(ErrorStatus.AUTH_EXPIRED);
			}
			log.warn("[REISSUE] Invalid refresh token");
			throw new AuthHandler(ErrorStatus.AUTH_INVALID_OR_EXPIRED);
		}

		String email = jwtTokenProvider.getUserEmail(refreshToken);
		Users user = userQueryService.findByEmail(email).orElse(null);
		if (user == null) {
			log.warn("[REISSUE] User not found: {}", email);
			throw new AuthHandler(ErrorStatus.AUTH_USER_NOT_FOUND);
		}

		// 3) DB 저장된 Refresh Token과 비교
		String storedRefresh = user.getRefreshToken();
		if (storedRefresh == null) {
			log.warn("[REISSUE] Stored refresh token is null for user: {}", email);
			throw new AuthHandler(ErrorStatus.AUTH_STORED_REFRESH_NULL);
		}

		// 디버깅: 토큰 비교
		log.info("[REISSUE] Received token length: {}, Stored token length: {}",
			refreshToken.length(), storedRefresh.length());
		log.info("[REISSUE] Received token (first 20 chars): {}",
			refreshToken.length() > 20 ? refreshToken.substring(0, 20) : refreshToken);
		log.info("[REISSUE] Stored token (first 20 chars): {}",
			storedRefresh.length() > 20 ? storedRefresh.substring(0, 20) : storedRefresh);
		log.info("[REISSUE] Tokens equal: {}", refreshToken.equals(storedRefresh));

		// Trim 후 비교 시도
		String trimmedReceived = refreshToken.trim();
		String trimmedStored = storedRefresh.trim();

		if (!trimmedReceived.equals(trimmedStored)) {
			log.warn("[REISSUE] Refresh token mismatch for user: {}", email);
			log.warn("[REISSUE] Received (trimmed) != Stored (trimmed)");
			throw new AuthHandler(ErrorStatus.AUTH_REFRESH_MISMATCH);
		}

		// 4) 새 토큰 생성
		var userDetails = userDetailsService.loadUserByUsername(email);
		var authentication = new UsernamePasswordAuthenticationToken(
			userDetails, null, userDetails.getAuthorities());

		String newAccessToken = jwtTokenProvider.generateToken(authentication);
		String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

		// 5) DB에 새 Refresh Token 저장
		user.setRefreshToken(newRefreshToken);
		userQueryService.save(user);

		// 6) 쿠키 재설정 (환경별 자동 처리)
		CookieManagerV2.setAccessToken(response, request, newAccessToken);
		CookieManagerV2.setRefreshToken(response, request, newRefreshToken);

		log.info("[REISSUE] 토큰 재발급 완료: userId={}", user.getId());
	}

	/**
	 * 로그아웃
	 * - 모든 인증 쿠키 삭제
	 * - DB Refresh Token 무효화
	 */
	@Override
	public void logout(RefreshTokenRequestDto body, HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) {
		log.info("[LOGOUT] 로그아웃 시작");

		// 1) 모든 인증 쿠키 삭제 (환경별 자동 처리)
		CookieManagerV2.clearAuthCookies(response, request);
		CookieManagerV2.clearOAuthTempCookies(response, request);

		// 2) Refresh Token 확보 (우선순위: body > Authorization 헤더 > 쿠키)
		String refreshToken = null;
		if (body != null && body.getRefreshToken() != null && !body.getRefreshToken().isBlank()) {
			refreshToken = body.getRefreshToken();
			log.debug("[LOGOUT] Refresh token from request body");
		} else {
			// Authorization 헤더 확인
			refreshToken = extractTokenFromHeader(request);
			if (refreshToken != null && !refreshToken.isBlank()) {
				log.debug("[LOGOUT] Refresh token from Authorization header");
			} else {
				// 쿠키 확인
				refreshToken = CookieManagerV2.getRefreshToken(request);
				if (refreshToken != null && !refreshToken.isBlank()) {
					log.debug("[LOGOUT] Refresh token from cookie");
				}
			}
		}

		boolean tokenRevoked = false;

		// 3) DB Refresh Token 무효화
		if (refreshToken != null && !refreshToken.isBlank()) {
			if (jwtTokenProvider.validateToken(refreshToken)) {
				String email = jwtTokenProvider.getUserEmail(refreshToken);
				userQueryService.findByEmail(email).ifPresent(user -> {
					user.setRefreshToken(null);
					userQueryService.save(user);
					log.info("[LOGOUT] Refresh token revoked for user: {}", email);
				});
				tokenRevoked = true;
			} else {
				// body로 전달된 잘못된 토큰이면 예외 (선택)
				if (body != null && body.getRefreshToken() != null) {
					log.warn("[LOGOUT] Invalid refresh token in request body");
					throw new AuthHandler(ErrorStatus.AUTH_INVALID_OR_EXPIRED);
				}
				// 쿠키의 잘못된 토큰은 무시 (idempotent)
			}
		}

		// 4) 인증 정보로도 토큰 무효화 시도 (보조)
		if (!tokenRevoked && authentication != null
			&& authentication.getPrincipal() instanceof PrincipalDetails pd) {
			Users user = pd.getUser();
			user.setRefreshToken(null);
			userQueryService.save(user);
			log.info("[LOGOUT] Refresh token revoked via authentication principal: {}", user.getEmail());
		}

		// 5) 세션 및 시큐리티 컨텍스트 정리
		var session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		SecurityContextHolder.clearContext();

		log.info("[LOGOUT] 로그아웃 완료");
	}
}
