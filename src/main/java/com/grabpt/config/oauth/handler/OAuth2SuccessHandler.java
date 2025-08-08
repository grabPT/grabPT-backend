package com.grabpt.config.oauth.handler;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.domain.entity.Users;
import com.grabpt.repository.UserRepository.UserRepository;
import com.grabpt.service.UserService.UserQueryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserDetailsService userDetailsService;
	private final UserQueryService userQueryService;
	private final UserRepository userRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken)authentication;
		String oauthProvider = oauthToken.getAuthorizedClientRegistrationId();

		Map<String, Object> attributes = oAuth2User.getAttributes();
		String email = null;
		String name = null;
		String oauthId = null;

		if (oauthProvider.equals("google")) {
			email = (String)attributes.get("email");
			name = (String)attributes.get("name");
			oauthId = oauthProvider + "-" + attributes.get("sub");
			// Google은 gender 제공 안함 → null
		} else if (oauthProvider.equals("kakao")) {
			Map<String, Object> kakaoAccount = (Map<String, Object>)attributes.get("kakao_account");
			Map<String, Object> profile = (Map<String, Object>)kakaoAccount.get("profile");

			email = kakaoAccount.get("email") != null ? (String)kakaoAccount.get("email") : null;
			name = profile != null ? (String)profile.get("nickname") : null;
			oauthId = oauthProvider + "-" + attributes.get("id");
		} else if (oauthProvider.equals("naver")) {
			Map<String, Object> responseMap = (Map<String, Object>)attributes.get("response");

			email = responseMap.get("email") != null ? (String)responseMap.get("email") : null;
			name = responseMap.get("name") != null ? (String)responseMap.get("name") : null;
			oauthId = oauthProvider + "-" + responseMap.get("id");
		}

		/// 이미 존재하는 회원 검증
		log.info("findEmail = " + email);
		Users findUser = userRepository.findByEmail(email).orElse(null);
		if (findUser != null) {
			log.info("기존 존재 회원 로직에 들어옴");

			// 토큰 직접 생성
			String accessToken = jwtTokenProvider.generateToken(findUser);
			String refreshToken = jwtTokenProvider.createRefreshToken(findUser.getEmail());

			// DB에 refreshToken 저장
			findUser.setRefreshToken(refreshToken);
			userRepository.save(findUser);

			// 쿠키로 토큰 전달
			Cookie accessCookie = new Cookie("accessToken", accessToken);
			accessCookie.setHttpOnly(true);
			accessCookie.setSecure(true);
			accessCookie.setPath("/");
			accessCookie.setDomain(".grabpt.com");
			accessCookie.setMaxAge(60 * 30); // 30분

			Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
			refreshCookie.setHttpOnly(true);
			refreshCookie.setSecure(true);
			refreshCookie.setPath("/");
			refreshCookie.setDomain(".grabpt.com");
			refreshCookie.setMaxAge(60 * 60 * 24 * 7); // 7일

			response.addCookie(accessCookie);
			response.addCookie(refreshCookie);

			response.sendRedirect("https://www.grabpt.com/"); // 환경에 맞게 수정\

			return;
		}

		// 쿠키 생성
		Cookie emailCookie = new Cookie("oauthEmail", email);
		Cookie nameCookie = new Cookie("oauthName", name);
		Cookie oauthIdCookie = new Cookie("oauthId", oauthId);
		Cookie oauthProviderCookie = new Cookie("oauthProvider", oauthProvider);

		for (Cookie cookie : new Cookie[] {emailCookie, nameCookie, oauthIdCookie, oauthProviderCookie}) {
			cookie.setHttpOnly(false);
			cookie.setSecure(false);
			cookie.setPath("/");
			cookie.setDomain(".grabpt.com");
			cookie.setMaxAge(5 * 60); // 5분
			response.addCookie(cookie);
		}

		// 신규 회원 → 세션에 임시 정보 저장 (null 허용)
		HttpSession session = request.getSession();
		session.setAttribute("tempEmail", email);
		session.setAttribute("tempName", name);
		session.setAttribute("tempOauthProvider", oauthProvider);
		session.setAttribute("tempOauthId", oauthId);

		log.info("신규 회원 소셜 로그인 - provider: {}, email: {}, name: {}",
			oauthProvider, email, name);

		response.sendRedirect("https://www.grabpt.com/signup");
	}
}
