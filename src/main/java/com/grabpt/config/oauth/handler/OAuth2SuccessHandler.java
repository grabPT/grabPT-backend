package com.grabpt.config.oauth.handler;

import static com.grabpt.config.jwt.properties.CookieSupport.*;

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

	// === 기존 상수/메서드는 유지 (prod 기본값) ===
	private static final String SHARED_DOMAIN = "grabpt.com"; // 앞에 점(.) 금지

	private static String b64(String s) {
		if (s == null)
			return "";
		return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
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

	// === 추가: 호스트 감지/분기 유틸 ===
	private static final Set<String> PROD_HOSTS = Set.of("grabpt.com", "www.grabpt.com", "api.grabpt.com");

	private static String rawForwardedHost(HttpServletRequest req) {
		String h = req.getHeader("X-Forwarded-Host");
		if (h != null && !h.isBlank())
			return h;
		return req.getServerName(); // 프록시 없을 때
	}

	private static String normalizeHost(String hostHeader) {
		if (hostHeader == null)
			return null;
		String h = hostHeader.trim();
		int comma = h.indexOf(',');
		if (comma >= 0)
			h = h.substring(0, comma).trim();
		int colon = h.indexOf(':');
		if (colon >= 0)
			h = h.substring(0, colon).trim();
		return h.toLowerCase();
	}

	private static String detectHost(HttpServletRequest req) {
		return normalizeHost(rawForwardedHost(req));
	}

	private static boolean isLocalHost(String host) {
		return "localhost".equals(host) || "127.0.0.1".equals(host);
	}

	private static boolean isProdHost(String host) {
		return host != null && (PROD_HOSTS.contains(host) || host.endsWith(".grabpt.com"));
	}

	// 리다이렉트 목적지: 로컬이면 로컬, 그 외엔 prod
	private static String resolveFrontendBase(HttpServletRequest req) {
		String host = detectHost(req);
		if (isLocalHost(host))
			return "http://localhost:5173";
		// (원하면 dev FE 도메인을 추가로 매핑 가능)
		return "https://www.grabpt.com";
	}

	// 로컬에서만 쓰는 host-only 쿠키 빌더 (도메인 미설정, Secure=false, Lax)
	private static ResponseCookie localCookie(String name, String value, Duration maxAge, boolean httpOnly) {
		return ResponseCookie.from(name, value == null ? "" : value)
			.path("/")
			.maxAge(maxAge)
			.httpOnly(httpOnly)
			.secure(false)
			.sameSite("Lax")
			.build();
	}

	// 신규 회원 임시 쿠키에서 사용할 스마트 추가기 (요청 호스트 기반)
	private static void addCookieSmart(HttpServletRequest req, HttpServletResponse res,
		String name, String value, Duration maxAge, boolean httpOnly) {
		String host = detectHost(req);
		if (isLocalHost(host)) {
			res.addHeader(HttpHeaders.SET_COOKIE, localCookie(name, value, maxAge, httpOnly).toString());
		} else {
			// 기존 prod 정책(도메인=grabpt.com, Secure, None) 유지
			addCookie(res, name, value, maxAge, httpOnly);
		}
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

		boolean isLocal = isLocalHost(detectHost(request));
		log.info("[OAuth2SuccessHandler] isLcal: " + isLocal);

		Users oauthUser = userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId).orElse(null);
		if (oauthUser != null) {
			log.info("기존 존재 회원 로직에 들어옴");

			// 토큰 직접 생성
			String accessToken = jwtTokenProvider.generateToken(oauthUser);
			String refreshToken = oauthUser.getRefreshToken();
			log.info("기존 유저 refreshToken 존재 확인: {}", refreshToken);

			// === 쿠키 발급: 로컬이면 host-only 정책, 운영이면 기존 CookieSupport 유지 ===
			if (isLocal) {
				response.addHeader(HttpHeaders.SET_COOKIE,
					localCookie("ACCESS_TOKEN", accessToken, Duration.ofHours(1), true).toString());
				response.addHeader(HttpHeaders.SET_COOKIE,
					localCookie("REFRESH_TOKEN", refreshToken, Duration.ofDays(14), true).toString());

				// 프론트에서 읽을 쿠키들 (httponly=false)
				response.addHeader(HttpHeaders.SET_COOKIE,
					localCookie("ROLE", b64(oauthUser.getRole() == Role.PRO ? "EXPERT" : oauthUser.getRole().name()),
						Duration.ofDays(7), false).toString());
				response.addHeader(HttpHeaders.SET_COOKIE,
					localCookie("USER_ID", b64(oauthUser.getId().toString()), Duration.ofDays(7), false).toString());
			} else {
				// 기존 로직 그대로 (도메인=grabpt.com, Secure, None 등 CookieSupport 규칙 유지)
				response.addHeader(HttpHeaders.SET_COOKIE, accessCookie(accessToken).toString());
				response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie(refreshToken).toString());
				log.info("기존 유저 refreshToken 발급: {}", refreshCookie(refreshToken));
				response.addHeader(HttpHeaders.SET_COOKIE, refreshCookieAtRoot(refreshToken).toString());

				response.addHeader(HttpHeaders.SET_COOKIE,
					roleCookie(
						b64(oauthUser.getRole() == Role.PRO ? "EXPERT" : oauthUser.getRole().name())).toString());
				response.addHeader(HttpHeaders.SET_COOKIE,
					userIdCookie(b64(oauthUser.getId().toString())).toString());
			}

			// 세션/컨텍스트 정리
			org.springframework.security.core.context.SecurityContextHolder.clearContext();
			var session = request.getSession(false);
			if (session != null)
				session.invalidate();

			// === 리다이렉트: 요청 호스트 기반 ===
			String feBase = resolveFrontendBase(request);
			response.sendRedirect(feBase + "/authcallback");
			log.info("[OAuth2SuccessHandler] feBase: " + feBase);
			return;
		}

		// === 신규 회원: 임시 쿠키 (요청 호스트 기반으로 addCookieSmart 사용) ===
		addCookieSmart(request, response, "oauthEmail", b64(email), Duration.ofMinutes(3), false);
		addCookieSmart(request, response, "oauthName", b64(name), Duration.ofMinutes(3), false);
		addCookieSmart(request, response, "oauthId", b64(oauthId), Duration.ofMinutes(3), false);
		addCookieSmart(request, response, "oauthProvider", b64(oauthProvider), Duration.ofMinutes(3), false);

		// 신규 회원 → 세션에 임시 정보 저장 (null 허용)
		HttpSession session = request.getSession();
		session.setAttribute("tempEmail", email);
		session.setAttribute("tempName", name);
		session.setAttribute("tempOauthProvider", oauthProvider);
		session.setAttribute("tempOauthId", oauthId);

		log.info("신규 회원 소셜 로그인 - provider: {}, email: {}, name: {}", oauthProvider, email, name);

		// === 리다이렉트: 요청 호스트 기반 ===
		String feBase = resolveFrontendBase(request);
		response.sendRedirect(feBase + "/signup");

	}
}
