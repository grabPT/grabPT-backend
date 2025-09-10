package com.grabpt.config.oauth.handler;

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
import com.grabpt.config.oauth.DynamicCookieSupport;
import com.grabpt.config.oauth.RedirectTargetResolver;
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

	// ===== helpers =====
	private static String b64(String s) {
		if (s == null)
			return "";
		return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

	private void add(HttpServletResponse res, ResponseCookie c) {
		res.addHeader(HttpHeaders.SET_COOKIE, c.toString());
	}

	private static String getCookieValue(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (Cookie c : cookies)
			if (name.equals(c.getName()))
				return c.getValue();
		return null;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> castMap(Object o) {
		return (o instanceof Map<?, ?> m) ? (Map<String, Object>)m : null;
	}

	private static String str(Object o) {
		return o == null ? null : String.valueOf(o);
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication)
		throws IOException, ServletException {

		// 0) 최종 리다이렉트 대상(frontend base) 판별 (세션 → 쿠키 → 헤더)
		HttpSession session = request.getSession(false);
		String sessionHint = session == null ? null :
			(String)session.getAttribute(RedirectTargetResolver.REDIRECT_URI_COOKIE);
		String cookieHint = getCookieValue(request, RedirectTargetResolver.REDIRECT_URI_COOKIE);

		String frontendBase = RedirectTargetResolver.resolveFrontendBase(
			request, sessionHint != null ? sessionHint : cookieHint);
		if (!RedirectTargetResolver.isAllowedRedirectBase(frontendBase)) {
			log.warn("Blocked unexpected redirect base: {}", frontendBase);
			frontendBase = RedirectTargetResolver.EnvTarget.PROD_FE.base;
		}
		log.debug("[OAUTH][SUCCESS] frontendBase={} (sessionHint={}, cookieHint={})",
			frontendBase, sessionHint, cookieHint);

		// 1) 공급자/속성 파싱
		OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken)authentication;
		String oauthProvider = oauthToken.getAuthorizedClientRegistrationId();

		Map<String, Object> attributes = oAuth2User.getAttributes();
		String email = null, name = null, oauthId = null;

		if ("google".equals(oauthProvider)) {
			email = str(attributes.get("email"));
			name = str(attributes.get("name"));
			oauthId = oauthProvider + "-" + str(attributes.get("sub"));
		} else if ("kakao".equals(oauthProvider)) {
			Map<String, Object> kakaoAccount = castMap(attributes.get("kakao_account"));
			Map<String, Object> profile = kakaoAccount == null ? null : castMap(kakaoAccount.get("profile"));
			email = kakaoAccount != null ? str(kakaoAccount.get("email")) : null;
			name = profile != null ? str(profile.get("nickname")) : null;
			oauthId = oauthProvider + "-" + str(attributes.get("id"));
		} else if ("naver".equals(oauthProvider)) {
			Map<String, Object> resp = castMap(attributes.get("response"));
			email = resp != null ? str(resp.get("email")) : null;
			name = resp != null ? str(resp.get("name")) : null;
			oauthId = oauthProvider + "-" + (resp != null ? str(resp.get("id")) : null);
		}

		// 2) 기존 회원 확인
		Users oauthUser = userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId).orElse(null);

		if (oauthUser != null) {
			// 기존 회원: 토큰 발급
			String accessToken = jwtTokenProvider.generateToken(oauthUser);
			String refreshToken = oauthUser.getRefreshToken();

			// HttpOnly 토큰 쿠키
			add(response, DynamicCookieSupport.newCookie("ACCESS_TOKEN", accessToken, request)
				.maxAge(Duration.ofHours(4)).build());
			add(response, DynamicCookieSupport.newCookie("REFRESH_TOKEN", refreshToken, request)
				.maxAge(Duration.ofDays(30)).build());

			// 공개 쿠키 (프론트 읽음)
			String roleStr = oauthUser.getRole() == Role.PRO ? "EXPERT" : oauthUser.getRole().name();
			add(response, DynamicCookieSupport.asPublic(
					DynamicCookieSupport.newCookie("ROLE", b64(roleStr), request))
				.maxAge(Duration.ofDays(30)).build());
			add(response, DynamicCookieSupport.asPublic(
					DynamicCookieSupport.newCookie("USER_ID", b64(oauthUser.getId().toString()), request))
				.maxAge(Duration.ofDays(30)).build());

			// 세션/컨텍스트 정리
			org.springframework.security.core.context.SecurityContextHolder.clearContext();
			if (session != null)
				session.invalidate();

			response.sendRedirect(frontendBase + "/authcallback");
			return;
		}

		// 신규 회원: 임시 공개 쿠키 3분 + 세션 보관 + /signup
		add(response, DynamicCookieSupport.asPublic(
				DynamicCookieSupport.newCookie("oauthEmail", b64(email), request))
			.maxAge(Duration.ofMinutes(3)).build());
		add(response, DynamicCookieSupport.asPublic(
				DynamicCookieSupport.newCookie("oauthName", b64(name), request))
			.maxAge(Duration.ofMinutes(3)).build());
		add(response, DynamicCookieSupport.asPublic(
				DynamicCookieSupport.newCookie("oauthId", b64(oauthId), request))
			.maxAge(Duration.ofMinutes(3)).build());
		add(response, DynamicCookieSupport.asPublic(
				DynamicCookieSupport.newCookie("oauthProvider", b64(oauthProvider), request))
			.maxAge(Duration.ofMinutes(3)).build());

		if (session == null)
			session = request.getSession(true);
		session.setAttribute("tempEmail", email);
		session.setAttribute("tempName", name);
		session.setAttribute("tempOauthProvider", oauthProvider);
		session.setAttribute("tempOauthId", oauthId);

		response.sendRedirect(frontendBase + "/signup");
	}
}
