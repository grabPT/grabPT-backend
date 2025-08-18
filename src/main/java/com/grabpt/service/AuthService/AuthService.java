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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.CategoryHandler;
import com.grabpt.apiPayload.exception.handler.UserHandler;
import com.grabpt.aws.s3.AmazonS3Manager;
import com.grabpt.aws.s3.Uuid;
import com.grabpt.config.jwt.JwtTokenProvider;
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
import com.grabpt.dto.request.SignupRequest;
import com.grabpt.repository.CategoryRepository.CategoryRepository;
import com.grabpt.repository.TermsRepository.TermsRepository;
import com.grabpt.repository.UserRepository.UserRepository;
import com.grabpt.repository.UserTermsAgreementRepository;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final CategoryRepository categoryRepository;
	private final TermsRepository termsRepository;
	private final UserTermsAgreementRepository userTermsAgreementRepository;
	private final AmazonS3Manager amazonS3Manager;

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
		Category userCategory = categoryRepository.findById(req.getCategoryId())
			.orElseThrow(() -> new CategoryHandler(ErrorStatus.CATEGORY_NOT_FOUND));

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
			.username(req.getUsername())
			.email(req.getEmail())
			.phone_number(req.getPhoneNum())
			.address(address)
			.nickname(req.getNickname())
			.role(mapToRole(req.getRole()))
			.authRole(AuthRole.ROLE_USER)
			.profileImageUrl(imageUrl)  // S3 URL 저장
			.agreeMarketing(req.getAgreeMarketing())
			.agreeMarketingAt(req.getAgreeMarketing() ? LocalDateTime.now() : null)
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
		Category proCategory = categoryRepository.findById(req.getCategoryId())
			.orElseThrow(() -> new CategoryHandler(ErrorStatus.CATEGORY_NOT_FOUND));

		// ProProfile 생성
		ProProfile proProfile = ProProfile.builder()
			.center(req.getCenter())
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
			.username(req.getUsername())
			.email(req.getEmail())
			.phone_number(req.getPhoneNum())
			.address(address)
			.nickname(req.getNickname())
			.role(mapToRole(req.getRole()))
			.gender(mapToGender(req.getGender()))
			.authRole(AuthRole.ROLE_USER)
			.profileImageUrl(imageUrl)  // S3 URL 저장
			.agreeMarketing(req.getAgreeMarketing())
			.agreeMarketingAt(req.getAgreeMarketing() ? LocalDateTime.now() : null)
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

	private void saveUserAgreements(Users user, List<Long> agreedTermsIds) {
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

	private Gender mapToGender(int genderCode) {
		switch (genderCode) {
			case 1:
				return Gender.MALE;
			case 2:
				return Gender.FEMALE;
			default:
				throw new UserHandler(ErrorStatus.INVALID_GENDER);
		}
	}

	private static String b64(String s) {
		if (s == null)
			return "";
		return Base64.getEncoder()
			.encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

	private Role mapToRole(int userTypeCode) {
		switch (userTypeCode) {
			case 1:
				return Role.USER;
			case 2:
				return Role.PRO;
			default:
				throw new UserHandler(ErrorStatus.INVALID_ROLE);
		}
	}

	private static final String SHARED_DOMAIN = "grabpt.com";

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

	private static String toCookieRole(Role role) {
		return (role == Role.PRO) ? "EXPERT" : role.name();
	}

	private void createTokenAndSetCookie(Users user, HttpServletResponse response) {
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
		String cookieRole = toCookieRole(user.getRole()); // PRO → EXPERT 매핑
		addCookie(response, "role", b64(cookieRole), Duration.ofMinutes(30), false);

		// userId 쿠키 추가
		addCookie(response, "userId", b64(user.getId().toString()), Duration.ofMinutes(30), false);
	}

}
