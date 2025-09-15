package com.grabpt.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.domain.entity.Users;
import com.grabpt.dto.request.RefreshTokenRequestDto;
import com.grabpt.dto.request.SignupRequest;
import com.grabpt.dto.response.UserResponseDto;
import com.grabpt.service.AuthService.AuthService;
import com.grabpt.service.UserService.UserQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserQueryService userQueryService;
	private final AuthService authService;

	@Operation(summary = "User 회원가입 요청 (Multipart)",
		description = "JSON 데이터와 프로필 이미지를 동시에 전송하는 회원가입")
	@PostMapping(value = "/user-signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<String> userSignup(
		@RequestPart("data") SignupRequest.UserSignupRequestDto signupRequest,
		@RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
		HttpServletResponse response) {

		authService.registerUser_photo(signupRequest, profileImage, response);

		return ApiResponse.onSuccess("user 회원가입 성공");
	}

	@Operation(summary = "Pro 회원가입 요청 (Multipart)",
		description = "JSON 데이터와 프로필 이미지를 동시에 전송하는 회원가입")
	@PostMapping(value = "/pro-signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<String> proSignup(
		@RequestPart("data") SignupRequest.ProSignupRequestDto signupRequest,
		@RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
		HttpServletResponse response) {

		authService.registerPro_photo(signupRequest, profileImage, response);

		return ApiResponse.onSuccess("pro 회원가입 성공");
	}

	// JWT 토큰 재발행
	@Operation(summary = "JWT Refresh Token으로 인증 토큰 재발행",
		description = "유효한 Refresh Token 전달 시 인증 토큰 재발행, access, refresh 토큰은 쿠키로 전달")
	@PostMapping("/reissue")
	public ResponseEntity<Void> reissueToken(HttpServletRequest request, HttpServletResponse response) {

		log.info("reissue 진입");
		authService.reissueTokens(request, response); // 실패 시 AuthHandler 발생 → Advice에서 401 처리
		return ResponseEntity.noContent().build();
	}

	@Operation(
		summary = "로그아웃",
		description = "accessToken 및 refreshToken 쿠키 삭제, DB refreshToken 초기화"
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "리프레시 토큰을 담은 요청 body",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = RefreshTokenRequestDto.class)
		)
	)
	@PostMapping("/logout")
	public ApiResponse<String> logout(@RequestBody(required = false) RefreshTokenRequestDto request,
		HttpServletRequest req,
		HttpServletResponse res,
		Authentication authentication) {

		log.info("쿠키 삭제 진입");
		authService.logout(request, req, res, authentication);
		return ApiResponse.onSuccess("로그아웃 완료");
	}

	@GetMapping("/check-nickname")
	@Operation(
		summary = "nickname 중복 검증",
		description = "nickname 파라미터 전송 시 중복 여부 판단, 중복이면 true / 미중복이면 false"
	)
	@Parameters({
		@Parameter(name = "nickname", description = "중복 닉네임", required = true, example = "닉네임")
	})
	public ApiResponse<Boolean> checkNickname(@RequestParam String nickname) {
		boolean isDuplicate = userQueryService.existsByNickname(nickname);
		return ApiResponse.onSuccess(isDuplicate);
	}

	@GetMapping("/check-email")
	@Operation(
		summary = "이메일 중복 검증",
		description = "이메일 파라미터 전송 시 중복 여부와 가입된 OAuth 제공자 정보를 반환합니다."
			+ "이메일 중복이면 isDuplicate=true, oauthProvider는 해당하는 유저의 oauthProvider는(google, kakao, naver 중 하나 / "
			+ "이메일 중복이 아니면 isDuplicate=false, oauthProvider는 null로 반환합니다."
	)
	@Parameters({
		@Parameter(name = "email", description = "중복 이메일", required = true, example = "test@gmail.com")
	})
	public ApiResponse<UserResponseDto.DuplicateEmailDto> checkEmail(@RequestParam String email) {
		Optional<Users> userOptional = userQueryService.findByEmail(email);

		boolean isDuplicate = userOptional.isPresent();
		String oauthProvider = null;

		if (isDuplicate) {
			oauthProvider = userOptional.get().getOauthProvider();
		}

		log.info("Email duplication check for {}: {}", email, isDuplicate);

		UserResponseDto.DuplicateEmailDto dto = UserResponseDto.DuplicateEmailDto.builder()
			.duplicate(isDuplicate)
			.oauthProvider(oauthProvider)
			.build();

		return ApiResponse.onSuccess(dto);
	}

	@GetMapping("/api/temp-info")
	@Operation(
		summary = "소셜 로그인 시 임시 정보 조회",
		description = "소셜 로그인 시 기존 쿠키 방식이 아닌 세션 방식으로 반환"
	)
	public ApiResponse<Map<String, String>> getTempInfo(HttpSession session) {
		Map<String, String> data = new HashMap<>();
		data.put("email", (String)session.getAttribute("tempEmail"));
		data.put("username", (String)session.getAttribute("tempName"));
		data.put("oauthProvider", (String)session.getAttribute("tempOauthProvider"));
		data.put("oauthId", (String)session.getAttribute("tempOauthId"));

		log.info("임시 회원가입 데이터 반환: {}", data);

		return ApiResponse.onSuccess(data);
	}
}
