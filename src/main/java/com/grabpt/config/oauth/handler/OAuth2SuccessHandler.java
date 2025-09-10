package com.grabpt.config.oauth.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.config.oauth.CookieUtils;
import com.grabpt.config.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Role;
import com.grabpt.repository.UserRepository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	private static final String SHARED_DOMAIN = "grabpt.com";

	private static String b64(String s) {
		if (s == null)
			return "";
		return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		Optional<String> redirectUriOptional = CookieUtils.getCookie(request,
				HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
			.map(Cookie::getValue);

		// 리다이렉션 URL 결정: 쿠키에서 찾거나 기본값 사용
		String authCallbackUrl = redirectUriOptional.orElse("https://www.grabpt.com/authcallback");
		String signupUrl = redirectUriOptional.map(uri -> uri.replace("authcallback", "signup"))
			.orElse("https://www.grabpt.com/signup");

		// 리다이렉션 URL에서 도메인 추출
		String domain = getDomainFromRedirectUri(authCallbackUrl);

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

			String accessToken = jwtTokenProvider.generateToken(oauthUser);
			String refreshToken = oauthUser.getRefreshToken();

			addCookie(response, "accessToken", accessToken, Duration.ofMinutes(15), true, domain);
			addCookie(response, "refreshToken", refreshToken, Duration.ofDays(7), true, domain);
			addCookie(response, "role", b64(oauthUser.getRole() == Role.PRO ? "EXPERT" : oauthUser.getRole().name()),
				Duration.ofDays(7), false, domain);
			addCookie(response, "userId", b64(oauthUser.getId().toString()), Duration.ofDays(7), false, domain);

			response.sendRedirect(authCallbackUrl);
			return;
		}

		addCookie(response, "oauthEmail", b64(email), Duration.ofMinutes(3), false, domain);
		addCookie(response, "oauthName", b64(name), Duration.ofMinutes(3), false, domain);
		addCookie(response, "oauthId", b64(oauthId), Duration.ofMinutes(3), false, domain);
		addCookie(response, "oauthProvider", b64(oauthProvider), Duration.ofMinutes(3), false, domain);

		response.sendRedirect(signupUrl);
	}

	private String getDomainFromRedirectUri(String redirectUri) {
		try {
			java.net.URI uri = new java.net.URI(redirectUri);
			String host = uri.getHost();
			if (host != null && host.contains("localhost")) {
				return "localhost";
			}
			return SHARED_DOMAIN;
		} catch (Exception e) {
			return SHARED_DOMAIN;
		}
	}

	// 이 메서드는 CookieUtils.java로 이동해야 함
	private static void addCookie(HttpServletResponse res, String name, String value,
		Duration maxAge, boolean httpOnly, String domain) {
		ResponseCookie c = ResponseCookie.from(name, value == null ? "" : value)
			.domain(domain)
			.path("/")
			.maxAge(maxAge)
			.secure(true)
			.httpOnly(httpOnly)
			.sameSite("None")
			.build();
		res.addHeader(HttpHeaders.SET_COOKIE, c.toString());
	}
}
