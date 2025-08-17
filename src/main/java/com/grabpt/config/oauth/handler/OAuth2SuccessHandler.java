package com.grabpt.config.oauth.handler;

import static com.grabpt.config.jwt.properties.CookieSupport.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.grabpt.config.jwt.JwtTokenProvider;
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
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	private static final String SHARED_DOMAIN = "grabpt.com"; // 앞에 점(.) 금지

	private static String b64(String s) {
		if (s == null)
			return "";
		return Base64.getEncoder()
			.encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

	private static void addCookie(HttpServletResponse res, String name, String value,
		Duration maxAge, boolean httpOnly) {
		ResponseCookie c = ResponseCookie.from(name, value == null ? "" : value)
			.domain(SHARED_DOMAIN)
			.path("/")
			.maxAge(maxAge)
			.secure(true)
			.httpOnly(httpOnly)
			.sameSite("None")
			.build();
		res.addHeader(HttpHeaders.SET_COOKIE, c.toString());
	}

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

		Users oauthUser = userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId).orElse(null);
		if (oauthUser != null) {
			log.info("기존 존재 회원 로직에 들어옴");

			// 토큰 직접 생성
			String accessToken = jwtTokenProvider.generateToken(oauthUser);
			String refreshToken = oauthUser.getRefreshToken();
			log.info("기존 유저 refreshToken 존재 확인: " + refreshToken);

			// 3) 쿠키 정리 + 재발급 (충돌 방지)
			// response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookieAtRoot().toString());
			// response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie().toString());

			// access/refresh 표준 발급
			response.addHeader(HttpHeaders.SET_COOKIE, accessCookie(accessToken).toString());
			response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie(refreshToken).toString());
			log.info("기존 유저 refreshToken 발급: " + refreshCookie(refreshToken));

			// (임시 치유, 1~2주): 루트 경로에도 한 번 더 동일 값 덮어쓰기
			// 중복 쿠키가 남아 있던 브라우저를 자동 치유합니다.
			response.addHeader(HttpHeaders.SET_COOKIE, refreshCookieAtRoot(refreshToken).toString());

			// 프론트에서 읽을 쿠키들
			response.addHeader(HttpHeaders.SET_COOKIE,
				roleCookie(b64(oauthUser.getRole() == Role.PRO ? "EXPERT" : oauthUser.getRole().name())).toString());
			response.addHeader(HttpHeaders.SET_COOKIE,
				userIdCookie(b64(oauthUser.getId().toString())).toString());

			org.springframework.security.core.context.SecurityContextHolder.clearContext();
			var session = request.getSession(false);
			if (session != null)
				session.invalidate();

			response.sendRedirect("https://www.grabpt.com/authcallback");
			return;
		}

		// 쿠키 생성
		// 신규 회원: 프론트가 읽을 임시 쿠키 (ASCII만 허용 → Base64)
		addCookie(response, "oauthEmail", b64(email), Duration.ofMinutes(5), false);
		addCookie(response, "oauthName", b64(name), Duration.ofMinutes(5), false);
		addCookie(response, "oauthId", b64(oauthId), Duration.ofMinutes(5), false);
		addCookie(response, "oauthProvider", b64(oauthProvider), Duration.ofMinutes(5), false);

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
