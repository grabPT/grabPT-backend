package com.grabpt.config.oauth.handler;

import static com.grabpt.config.oauth.handler.DynamicCookieSupport.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.grabpt.config.jwt.JwtTokenProvider;
import com.grabpt.config.oauth.CookieProfiles;
import com.grabpt.config.oauth.CookieUtils;
import com.grabpt.config.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Role;
import com.grabpt.repository.UserRepository.UserRepository;

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
	private final UserRepository userRepository;

	// 허용 리다이렉트 호스트(오픈 리다이렉트 방지)
	private static final Set<String> ALLOWED_REDIRECT_HOSTS = Set.of(
		"www.grabpt.com", "grabpt.com", "api.grabpt.com", "localhost", "127.0.0.1"
	);

	private static String b64(String s) {
		if (s == null)
			return "";
		return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

	private String resolveRedirectUri(HttpServletRequest request, String fallbackPath) {
		String defaultBase = "https://www.grabpt.com";
		String raw = CookieUtils.getCookie(request,
				HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
			.map(Cookie::getValue)
			.orElse(null);

		try {
			if (raw != null && !raw.isBlank()) {
				var uri = java.net.URI.create(raw);
				String host = uri.getHost();
				String scheme = uri.getScheme();
				if (host != null && scheme != null && ALLOWED_REDIRECT_HOSTS.contains(host)) {
					return uri.toString();
				}
			}
		} catch (Exception e) {
			log.warn("Invalid redirect_uri cookie: {}", raw);
		}
		return defaultBase + fallbackPath;
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
		} else if (oauthProvider.equals("kakao")) {
			Map<String, Object> kakaoAccount = (Map<String, Object>)attributes.get("kakao_account");
			Map<String, Object> profile =
				kakaoAccount == null ? null : (Map<String, Object>)kakaoAccount.get("profile");
			email = kakaoAccount != null ? (String)kakaoAccount.get("email") : null;
			name = profile != null ? (String)profile.get("nickname") : null;
			oauthId = oauthProvider + "-" + attributes.get("id");
		} else if (oauthProvider.equals("naver")) {
			Map<String, Object> responseMap = (Map<String, Object>)attributes.get("response");
			email = responseMap != null ? (String)responseMap.get("email") : null;
			name = responseMap != null ? (String)responseMap.get("name") : null;
			oauthId = oauthProvider + "-" + (responseMap != null ? responseMap.get("id") : null);
		}

		Users oauthUser = userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId).orElse(null);

		// 대상 redirect_uri 계산 (신규/기존 모두 공통)
		final String targetForExisting = resolveRedirectUri(request, "/authcallback");
		final String targetForNew = resolveRedirectUri(request, "/signup");

		if (oauthUser != null) {
			log.info("기존 존재 회원: provider={} email={} id={}", oauthProvider, oauthUser.getEmail(), oauthUser.getId());

			// 토큰 생성/조회
			String accessToken = jwtTokenProvider.generateToken(oauthUser);
			String refreshToken = oauthUser.getRefreshToken();

			// 리다이렉트 대상에 맞춘 쿠키 프로필 결정
			var prof = CookieProfiles.decideByTarget(targetForExisting);

			// access/refresh 쿠키
			response.addHeader(HttpHeaders.SET_COOKIE, accessCookie(accessToken, Duration.ofDays(1), prof).toString());
			response.addHeader(HttpHeaders.SET_COOKIE,
				refreshCookie(refreshToken, Duration.ofDays(30), prof).toString());

			// (배포에서만 필요하다면) 루트 경로 치유 쿠키
			if (prof.domain() != null) {
				response.addHeader(HttpHeaders.SET_COOKIE,
					refreshCookieAtRoot(refreshToken, Duration.ofDays(30), prof).toString());
			}

			// 프론트에서 읽을 쿠키들 (HttpOnly=false)
			String roleStr = oauthUser.getRole() == Role.PRO ? "EXPERT" : oauthUser.getRole().name();
			response.addHeader(HttpHeaders.SET_COOKIE,
				roleCookie(b64(roleStr), Duration.ofMinutes(10), prof).toString());
			response.addHeader(HttpHeaders.SET_COOKIE,
				userIdCookie(b64(oauthUser.getId().toString()), Duration.ofMinutes(10), prof).toString());

			// 세션/컨텍스트 정리
			org.springframework.security.core.context.SecurityContextHolder.clearContext();
			var session = request.getSession(false);
			if (session != null)
				session.invalidate();

			// 동적 리다이렉트
			response.sendRedirect(targetForExisting);
			return;
		}

		// 신규 회원: 프론트가 읽을 임시 쿠키 (Base64)
		var profNew = CookieProfiles.decideByTarget(targetForNew);

		// 신규회원 임시정보는 HttpOnly=false로 3분만 유지
		response.addHeader(HttpHeaders.SET_COOKIE,
			ResponseCookie.from("oauthEmail", b64(email))
				.domain(profNew.domain()).path("/")
				.maxAge(Duration.ofMinutes(3))
				.secure(profNew.secure()).httpOnly(false).sameSite(profNew.sameSite())
				.build().toString());
		response.addHeader(HttpHeaders.SET_COOKIE,
			ResponseCookie.from("oauthName", b64(name))
				.domain(profNew.domain()).path("/")
				.maxAge(Duration.ofMinutes(3))
				.secure(profNew.secure()).httpOnly(false).sameSite(profNew.sameSite())
				.build().toString());
		response.addHeader(HttpHeaders.SET_COOKIE,
			ResponseCookie.from("oauthId", b64(oauthId))
				.domain(profNew.domain()).path("/")
				.maxAge(Duration.ofMinutes(3))
				.secure(profNew.secure()).httpOnly(false).sameSite(profNew.sameSite())
				.build().toString());
		response.addHeader(HttpHeaders.SET_COOKIE,
			ResponseCookie.from("oauthProvider", b64(oauthProvider))
				.domain(profNew.domain()).path("/")
				.maxAge(Duration.ofMinutes(3))
				.secure(profNew.secure()).httpOnly(false).sameSite(profNew.sameSite())
				.build().toString());

		// 세션 임시 저장 (서버 측)
		HttpSession session = request.getSession();
		session.setAttribute("tempEmail", email);
		session.setAttribute("tempName", name);
		session.setAttribute("tempOauthProvider", oauthProvider);
		session.setAttribute("tempOauthId", oauthId);

		log.info("신규 회원 소셜 로그인 - provider: {}, email: {}, name: {}", oauthProvider, email, name);

		// 동적 리다이렉트
		response.sendRedirect(targetForNew);
	}
}
