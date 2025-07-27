package com.grabpt.service.AuthService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.CategoryHandler;
import com.grabpt.apiPayload.exception.handler.UserHandler;
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

import jakarta.servlet.http.Cookie;
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

	public void registerUser(SignupRequest.UserSignupRequestDto req, HttpServletResponse response) {

		List<Category> categoryList = req.getCategories().stream()
			.map(id -> categoryRepository.findById(id)
				.orElseThrow(() -> new CategoryHandler(ErrorStatus.CATEGORY_NOT_FOUND)))
			.collect(Collectors.toList());

		// UserProfile 생성
		UserProfile userPrprofile = UserProfile.builder()
			.categories(categoryList)
			.build();

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
			.password(passwordEncoder.encode(req.getPassword()))
			.nickname(req.getNickname())
			.role(mapToRole(req.getRole()))
			.gender(mapToGender(req.getGender()))
			.authRole(AuthRole.ROLE_USER)
			.profileImageUrl(req.getProfileImageUrl())
			.agreeMarketing(req.getAgreeMarketing())
			.agreeMarketingAt(req.getAgreeMarketing() ? LocalDateTime.now() : null)
			.oauthId(URLDecoder.decode(req.getOauthId(), StandardCharsets.UTF_8)) // 디코딩 해서 db 저장
			.oauthProvider(URLDecoder.decode(req.getOauthProvider(), StandardCharsets.UTF_8)) // 디코딩 해서 db 저장
			.userProfile(userPrprofile)  // 연관관계 연결
			.build();

		userPrprofile.setUser(user);
		address.setUser(user);
		Users savedUser = userRepository.save(user);

		// 필수 약관 동의 내역 저장
		saveUserAgreements(savedUser, req.getAgreedTermsIds());

		createTokenAndSetCookie(savedUser, response);
	}

	public void registerPro(SignupRequest.ProSignupRequestDto req, HttpServletResponse response) {

		// 카테고리 조회
		Category proCategory = categoryRepository.findById(req.getCategoryId())
			.orElseThrow(() -> new CategoryHandler(ErrorStatus.CATEGORY_NOT_FOUND));

		//  ProProfile 생성 및 Users 연관 설정
		ProProfile proProfile = ProProfile.builder()
			.center(req.getCenter())
			.career(req.getCareer())
			.description(req.getDescription())
			.category(proCategory)
			.build();

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
			.password(passwordEncoder.encode(req.getPassword()))
			.nickname(req.getNickname())
			.role(mapToRole(req.getRole())) // Role.PRO
			.gender(mapToGender(req.getGender()))
			.authRole(AuthRole.ROLE_USER)
			.profileImageUrl(req.getProfileImageUrl())
			.agreeMarketing(req.getAgreeMarketing())
			.agreeMarketingAt(req.getAgreeMarketing() ? LocalDateTime.now() : null)
			.oauthId(URLDecoder.decode(req.getOauthId(), StandardCharsets.UTF_8))
			.oauthProvider(URLDecoder.decode(req.getOauthProvider(), StandardCharsets.UTF_8))
			.proProfile(proProfile)
			.build();

		proProfile.setUser(user);
		address.setUser(user);
		Users savedUser = userRepository.save(user);

		// 필수 약관 동의 내역 저장
		saveUserAgreements(savedUser, req.getAgreedTermsIds());

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

	private void createTokenAndSetCookie(Users user, HttpServletResponse response) {
		String accessToken = jwtTokenProvider.generateToken(user);
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

		log.info("JWT 토큰 생성: {}", accessToken);

		user.setRefreshToken(refreshToken);
		userRepository.save(user);

		// 쿠키 생성
		Cookie accessCookie = new Cookie("accessToken", accessToken);
		accessCookie.setHttpOnly(true);
		accessCookie.setSecure(true);
		accessCookie.setPath("/");
		accessCookie.setMaxAge(60 * 30); // 30분

		Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
		refreshCookie.setHttpOnly(true);
		refreshCookie.setSecure(true);
		refreshCookie.setPath("/");
		refreshCookie.setMaxAge(60 * 60 * 24 * 7); // 7일

		response.addCookie(accessCookie);
		response.addCookie(refreshCookie);
	}

}
