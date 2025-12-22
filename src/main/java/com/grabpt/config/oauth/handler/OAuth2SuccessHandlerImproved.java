package com.grabpt.config.oauth.handler;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.config.jwt.properties.CookieManagerV2;
import com.grabpt.config.oauth.RedirectTargetResolver;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Role;
import com.grabpt.repository.UserRepository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandlerImproved implements AuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication)
		throws IOException, ServletException {

		// 1. 리다이렉트 대상 결정
		String frontendBase = resolveFrontendBase(request);
		log.info("[OAuth Success] Frontend base: {}", frontendBase);

		// 2. OAuth 정보 파싱
		OAuthUserInfo oauthInfo = extractOAuthInfo(authentication);
		log.info("[OAuth Success] Provider: {}, Email: {}", oauthInfo.provider, oauthInfo.email);

		// 3. 기존 회원 확인
		Users existingUser = userRepository
			.findByOauthProviderAndOauthId(oauthInfo.provider, oauthInfo.oauthId)
			.orElse(null);

		if (existingUser != null) {
			// 기존 회원 - 토큰 발급 및 로그인 처리
			handleExistingUser(request, response, existingUser, frontendBase);
		} else {
			// 신규 회원 - 회원가입 페이지로 리다이렉트
			handleNewUser(request, response, oauthInfo, frontendBase);
		}

		// 4. 세션 정리
		cleanupSession(request);
	}

	/**
	 * 기존 회원 로그인 처리
	 */
	private void handleExistingUser(HttpServletRequest request, HttpServletResponse response,
		Users user, String frontendBase) throws IOException {

		// JWT 생성
		String accessToken = jwtTokenProvider.generateToken(user);
		String refreshToken = jwtTokenProvider.createRefreshToken(
			user.getEmail() != null ? user.getEmail() : user.getOauthId()
		);

		// DB에 Refresh Token 저장
		user.setRefreshToken(refreshToken);
		userRepository.save(user);

		// ⭐ 프론트엔드 환경에 따라 분기
		boolean isDevelopment = isDevelopmentFrontend(frontendBase);

		if (isDevelopment) {
			// 개발: URL 파라미터로 전달
			log.info("[OAuth Success] Dev environment - tokens in URL params");

			String redirectUrl = UriComponentsBuilder.fromUriString(frontendBase)
				.path("/authcallback")
				.queryParam("access_token", accessToken)
				.queryParam("refresh_token", refreshToken)
				.queryParam("role", getRoleString(user.getRole()))
				.queryParam("user_id", user.getId().toString())
				.build()
				.encode()  // ✅ URL 인코딩 추가!
				.toUriString();

			response.sendRedirect(redirectUrl);

		} else {
			// 운영: 쿠키로만 전달
			log.info("[OAuth Success] Prod environment - tokens in cookies");

			CookieManagerV2.setAccessToken(response, request, accessToken);
			CookieManagerV2.setRefreshToken(response, request, refreshToken);
			CookieManagerV2.setRole(response, request, getRoleString(user.getRole()));
			CookieManagerV2.setUserId(response, request, user.getId().toString());

			String redirectUrl = UriComponentsBuilder.fromUriString(frontendBase)
				.path("/authcallback")
				.build()
				.toUriString();

			response.sendRedirect(redirectUrl);
		}

		log.info("[OAuth Success] Existing user logged in: userId={}, role={}",
			user.getId(), user.getRole());
	}

	/**
	 * 신규 회원 처리 - 회원가입 페이지로 리다이렉트
	 */
	private void handleNewUser(HttpServletRequest request, HttpServletResponse response,
		OAuthUserInfo oauthInfo, String frontendBase) throws IOException {

		// ⭐ 프론트엔드 환경에 따라 분기
		boolean isDevelopment = isDevelopmentFrontend(frontendBase);

		if (isDevelopment) {
			// 개발: URL 파라미터로 전달 (URL 인코딩 자동 적용)
			log.info("[OAuth Success] Dev environment - OAuth info in URL params");

			String redirectUrl = UriComponentsBuilder.fromUriString(frontendBase)
				.path("/signup")
				.queryParam("oauthEmail", oauthInfo.email)
				.queryParam("oauthName", oauthInfo.name)
				.queryParam("oauthId", oauthInfo.oauthId)
				.queryParam("oauthProvider", oauthInfo.provider)
				.build()
				.encode()  // ✅ URL 인코딩 추가!
				.toUriString();

			response.sendRedirect(redirectUrl);

		} else {
			// 운영: 쿠키로 전달 (HttpOnly=false로 프론트에서 읽을 수 있게)
			log.info("[OAuth Success] Prod environment - OAuth info in cookies");

			CookieManagerV2.setOAuthTempInfo(response, request,
				oauthInfo.email,
				oauthInfo.name,
				oauthInfo.oauthId,
				oauthInfo.provider
			);

			String redirectUrl = UriComponentsBuilder.fromUriString(frontendBase)
				.path("/signup")
				.build()
				.toUriString();

			response.sendRedirect(redirectUrl);
		}

		log.info("[OAuth Success] New user redirected to signup: provider={}", oauthInfo.provider);
	}

	/**
	 * ⭐ 프론트엔드 환경 판단 (frontendBase 기준)
	 * 백엔드 환경이 아닌 프론트엔드 환경으로 판단!
	 */
	private boolean isDevelopmentFrontend(String frontendBase) {
		if (frontendBase == null) {
			return false;
		}

		// localhost 또는 개발 도메인이면 개발 환경
		return frontendBase.contains("localhost")
			|| frontendBase.contains("127.0.0.1")
			|| frontendBase.contains("vercel.app")
			|| frontendBase.contains("-dev.");
	}

	/**
	 * OAuth 사용자 정보 추출
	 */
	private OAuthUserInfo extractOAuthInfo(Authentication authentication) {
		OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken)authentication;
		String provider = oauthToken.getAuthorizedClientRegistrationId();
		Map<String, Object> attributes = oAuth2User.getAttributes();

		return switch (provider) {
			case "google" -> extractGoogleInfo(attributes, provider);
			case "kakao" -> extractKakaoInfo(attributes, provider);
			case "naver" -> extractNaverInfo(attributes, provider);
			default -> throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
		};
	}

	private OAuthUserInfo extractGoogleInfo(Map<String, Object> attributes, String provider) {
		return new OAuthUserInfo(
			provider,
			String.valueOf(attributes.get("email")),
			String.valueOf(attributes.get("name")),
			provider + "-" + attributes.get("sub")
		);
	}

	@SuppressWarnings("unchecked")
	private OAuthUserInfo extractKakaoInfo(Map<String, Object> attributes, String provider) {
		Map<String, Object> kakaoAccount = (Map<String, Object>)attributes.get("kakao_account");
		Map<String, Object> profile = kakaoAccount != null
			? (Map<String, Object>)kakaoAccount.get("profile")
			: null;

		String email = kakaoAccount != null ? String.valueOf(kakaoAccount.get("email")) : null;
		String name = profile != null ? String.valueOf(profile.get("nickname")) : null;
		String oauthId = provider + "-" + attributes.get("id");

		return new OAuthUserInfo(provider, email, name, oauthId);
	}

	@SuppressWarnings("unchecked")
	private OAuthUserInfo extractNaverInfo(Map<String, Object> attributes, String provider) {
		Map<String, Object> response = (Map<String, Object>)attributes.get("response");

		String email = response != null ? String.valueOf(response.get("email")) : null;
		String name = response != null ? String.valueOf(response.get("name")) : null;
		String oauthId = provider + "-" + (response != null ? response.get("id") : null);

		return new OAuthUserInfo(provider, email, name, oauthId);
	}

	/**
	 * 프론트엔드 Base URL 결정
	 */
	private String resolveFrontendBase(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		String sessionHint = session != null
			? (String)session.getAttribute(RedirectTargetResolver.REDIRECT_URI_COOKIE)
			: null;

		String frontendBase = RedirectTargetResolver.resolveFrontendBase(request, sessionHint);

		if (!RedirectTargetResolver.isAllowedRedirectBase(frontendBase)) {
			log.warn("[OAuth Success] Blocked unexpected redirect: {}", frontendBase);
			frontendBase = RedirectTargetResolver.EnvTarget.PROD_FE.base;
		}

		return frontendBase;
	}

	/**
	 * Role을 문자열로 변환
	 */
	private String getRoleString(Role role) {
		return role == Role.PRO ? "PRO" : role.name();
	}

	/**
	 * 세션 정리
	 */
	private void cleanupSession(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(RedirectTargetResolver.REDIRECT_URI_COOKIE);
		}

		org.springframework.security.core.context.SecurityContextHolder.clearContext();
	}

	/**
	 * OAuth 사용자 정보 DTO
	 */
	private record OAuthUserInfo(String provider, String email, String name, String oauthId) {
	}
}
