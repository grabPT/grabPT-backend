package com.grabpt.service.AuthService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
import com.grabpt.domain.entity.UserProfile;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.AuthRole;
import com.grabpt.domain.enums.Gender;
import com.grabpt.domain.enums.Role;
import com.grabpt.dto.request.SignupRequest;
import com.grabpt.repository.CategoryRepository.CategoryRepository;
import com.grabpt.repository.UserRepository.UserRepository;

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

	public void registerUser(SignupRequest.UserSignupRequestDto req, HttpServletResponse response) {

		// 카테고리 ID 리스트 → 엔티티 리스트
		List<Category> categoryList = req.getCategories().stream()
			.map(id -> categoryRepository.findById(id)
				.orElseThrow(() -> new CategoryHandler(ErrorStatus.CATEGORY_NOT_FOUND)))
			.collect(Collectors.toList());

		// address 리스트 매핑
		List<Address> addressList = req.getAddress().stream()
			.map(addr -> Address.builder()
				.city(addr.getCity())
				.district(addr.getDistrict())
				.street(addr.getStreet())
				.zipcode(addr.getZipcode())
				.build())
			.collect(Collectors.toList());

		// UserProfile 생성
		UserProfile userPrprofile = UserProfile.builder()
			.categories(categoryList)
			.build();

		// Users 생성 및 연관관계 설정
		Users user = Users.builder()
			.username(req.getUsername())
			.email(req.getEmail())
			.phone_number(req.getPhoneNum())
			.addresses(addressList)
			.password(passwordEncoder.encode(req.getPassword()))
			.nickname(req.getNickname())
			.role(mapToRole(req.getRole()))
			.gender(mapToGender(req.getGender()))
			.authRole(AuthRole.ROLE_USER)
			.profileImageUrl(req.getProfileImageUrl())
			.oauthId(URLDecoder.decode(req.getOauthId(), StandardCharsets.UTF_8)) // 디코딩 해서 db 저장
			.oauthProvider(URLDecoder.decode(req.getOauthProvider(), StandardCharsets.UTF_8)) // 디코딩 해서 db 저장
			.userProfile(userPrprofile)  // 연관관계 연결
			.build();

		userPrprofile.setUser(user);

		userRepository.save(user);

		createTokenAndSetCookie(user, response);
	}

	public void registerPro(SignupRequest.ProSignupRequestDto req, HttpServletResponse response) {

		// 카테고리 ID 리스트 → 엔티티 리스트
		Category proCategory = categoryRepository.findById(req.getCategoryId())
			.orElseThrow(() -> new CategoryHandler(ErrorStatus.CATEGORY_NOT_FOUND));

		// address 리스트 매핑
		List<Address> addressList = req.getAddress().stream()
			.map(addr -> Address.builder()
				.city(addr.getCity())
				.district(addr.getDistrict())
				.street(addr.getStreet())
				.zipcode(addr.getZipcode())
				.build())
			.collect(Collectors.toList());

		//  ProProfile 생성 및 Users 연관 설정
		ProProfile proProfile = ProProfile.builder()
			.center(req.getCenter())
			.career(req.getCareer())
			.description(req.getDescription())
			.category(proCategory)
			.build();

		// Users 생성 및 연관관계 설정
		Users user = Users.builder()
			.username(req.getUsername())
			.email(req.getEmail())
			.phone_number(req.getPhoneNum())
			.addresses(addressList)
			.password(passwordEncoder.encode(req.getPassword()))
			.nickname(req.getNickname())
			.role(mapToRole(req.getRole())) // Role.PRO
			.gender(mapToGender(req.getGender()))
			.authRole(AuthRole.ROLE_USER)
			.profileImageUrl(req.getProfileImageUrl())
			.oauthId(URLDecoder.decode(req.getOauthId(), StandardCharsets.UTF_8))
			.oauthProvider(URLDecoder.decode(req.getOauthProvider(), StandardCharsets.UTF_8))
			.proProfile(proProfile)
			.build();

		proProfile.setUser(user);

		userRepository.save(user);

		createTokenAndSetCookie(user, response);
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
