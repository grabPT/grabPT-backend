package com.grabpt.service.AuthService;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Gender;
import com.grabpt.domain.enums.Role;
import com.grabpt.dto.request.RefreshTokenRequestDto;
import com.grabpt.dto.request.SignupRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

	/**
	 * User 회원가입 (프로필 사진 포함)
	 * @param req 회원가입 요청 데이터
	 * @param profileImage 프로필 이미지 파일
	 * @param response HTTP 응답
	 * @param request HTTP 요청 (환경 감지용)
	 */
	void registerUser_photo(SignupRequest.UserSignupRequestDto req,
		MultipartFile profileImage,
		HttpServletResponse response,
		HttpServletRequest request);

	/**
	 * Pro 회원가입 (프로필 사진 포함)
	 * @param req 회원가입 요청 데이터
	 * @param profileImage 프로필 이미지 파일
	 * @param response HTTP 응답
	 * @param request HTTP 요청 (환경 감지용)
	 */
	void registerPro_photo(SignupRequest.ProSignupRequestDto req,
		MultipartFile profileImage,
		HttpServletResponse response,
		HttpServletRequest request);

	void saveUserAgreements(Users user, List<Long> agreedTermsIds);

	/**
	 * 토큰 생성 및 쿠키 설정
	 * @param user 사용자 정보
	 * @param response HTTP 응답
	 * @param request HTTP 요청 (환경 감지용, null 가능)
	 */
	void createTokenAndSetCookie(Users user, HttpServletResponse response, HttpServletRequest request);

	Gender mapToGender(int genderCode);

	Role mapToRole(int userTypeCode);

	Map<String, String> reissueTokens(HttpServletRequest request, HttpServletResponse response);

	void logout(RefreshTokenRequestDto body, HttpServletRequest request, HttpServletResponse response,
		Authentication authentication);
}
