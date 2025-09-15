package com.grabpt.service.AuthService;

import java.util.List;

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

	void registerUser_photo(SignupRequest.UserSignupRequestDto req,
		MultipartFile profileImage,
		HttpServletResponse response);

	void registerPro_photo(SignupRequest.ProSignupRequestDto req,
		MultipartFile profileImage,
		HttpServletResponse response);

	void saveUserAgreements(Users user, List<Long> agreedTermsIds);

	Gender mapToGender(int genderCode);

	Role mapToRole(int userTypeCode);

	void createTokenAndSetCookie(Users user, HttpServletResponse response);

	void reissueTokens(HttpServletRequest request, HttpServletResponse response);

	void logout(RefreshTokenRequestDto body, HttpServletRequest req, HttpServletResponse res,
		Authentication authentication);
}
