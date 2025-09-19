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
import org.springframework.web.util.UriComponentsBuilder;

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

	private static String getCookieValue(HttpServletRequest request, String... names) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (String n : names)
			for (Cookie c : cookies)
				if (n.equals(c.getName()))
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

	/** dev/localhost 등 파라미터 방식으로 토큰/정보 전달이 필요한 대상 판정 */
	private static boolean needsParamTokens(String base) {
		if (base == null)
			return false;
		String b = base.toLowerCase();
		return b.contains("localhost")
			|| b.contains("127.0.0.1")
			|| "https://grabpt-dev.vercel.app".equalsIgnoreCase(b);
	}

	/** 과거/중복 쿠키 일괄 삭제 */
	private void deleteCookie(HttpServletResponse res, HttpServletRequest req, String name) {
		// httpOnly 버전 삭제
		add(res, DynamicCookieSupport.newCookie(name, "", req)
			.maxAge(Duration.ZERO) // Max-Age=0
			.build());
		// public(비-httpOnly) 버전 가능성도 삭제
		add(res, DynamicCookieSupport.asPublic(
				DynamicCookieSupport.newCookie(name, "", req))
			.maxAge(Duration.ZERO)
			.build());
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication)
		throws IOException, ServletException {

		// 0) 최종 리다이렉트 대상(frontend base) 판별: 세션 → 쿠키(redirect_uri / redirect_uri_hint) → 헤더
		HttpSession session = request.getSession(false);
		String sessionHint = session == null ? null :
			(String)session.getAttribute(RedirectTargetResolver.REDIRECT_URI_COOKIE);

		String cookieHint = getCookieValue(request,
			RedirectTargetResolver.REDIRECT_URI_COOKIE,
			RedirectTargetResolver.ALT_REDIRECT_URI_COOKIE);

		String frontendBase = RedirectTargetResolver.resolveFrontendBase(
			request, sessionHint != null ? sessionHint : cookieHint);

		if (!RedirectTargetResolver.isAllowedRedirectBase(frontendBase)) {
			log.warn("Blocked unexpected redirect base: {}", frontendBase);
			frontendBase = RedirectTargetResolver.EnvTarget.PROD_FE.base;
		}
		log.debug("[OAUTH][SUCCESS] frontendBase={} (sessionHint={}, cookieHint={})",
			frontendBase, sessionHint, cookieHint);

		// === (A) 중복/레거시 쿠키 선삭제 (이름/공개여부 조합 커버) ===
		String[] legacyNames = {
			"access_token", "refresh_token", // 소문자 레거시
			"ACCESS_TOKEN", "REFRESH_TOKEN"  // 대문자 현행
		};
		for (String n : legacyNames) {
			deleteCookie(response, request, n);
		}

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

		// 2) 기존 회원 여부
		Users oauthUser = userRepository.findByOauthProviderAndOauthId(oauthProvider, oauthId).orElse(null);

		if (oauthUser != null) {
			// === 기존 회원: 토큰 발급 + refresh 회전(DB 저장) ===
			String accessToken = jwtTokenProvider.generateToken(oauthUser);
			String emailForRefresh = oauthUser.getEmail() != null ? oauthUser.getEmail() : email;
			String newRefreshToken = jwtTokenProvider.createRefreshToken(emailForRefresh);

			oauthUser.setRefreshToken(newRefreshToken);
			userRepository.save(oauthUser);

			// (B) 새 쿠키 발급 (프로덕션/동일 도메인 사용 시)
			add(response, DynamicCookieSupport.newCookie("ACCESS_TOKEN", accessToken, request)
				.maxAge(Duration.ofHours(4)).build());
			add(response, DynamicCookieSupport.newCookie("REFRESH_TOKEN", newRefreshToken, request)
				.maxAge(Duration.ofDays(30)).build());

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

			// === 리다이렉트 URL 작성 ===
			final boolean paramMode = needsParamTokens(frontendBase);
			String targetUrl;

			if (paramMode) {
				// dev/localhost: URL 파라미터로 전달
				targetUrl = UriComponentsBuilder.fromUriString(frontendBase + "/authcallback")
					.queryParam("access_token", accessToken)
					.queryParam("refresh_token", newRefreshToken)
					.queryParam("role", roleStr)
					.queryParam("user_id", oauthUser.getId().toString())
					.build().toUriString();
			} else {
				// 운영: 쿠키만 사용 (URL 파라미터 금지)
				targetUrl = UriComponentsBuilder.fromUriString(frontendBase + "/authcallback")
					.build().toUriString();
			}

			response.sendRedirect(targetUrl);
			return;
		}

		// === 신규 회원: dev/localhost는 URL 파라미터, 운영은 퍼블릭 쿠키(3분) ===
		String targetUrl;
		if (needsParamTokens(frontendBase)) {
			targetUrl = UriComponentsBuilder.fromUriString(frontendBase + "/signup")
				.queryParam("oauthEmail", email)
				.queryParam("oauthName", name)
				.queryParam("oauthId", oauthId)
				.queryParam("oauthProvider", oauthProvider)
				.build().toUriString();
		} else {
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

			targetUrl = UriComponentsBuilder.fromUriString(frontendBase + "/signup")
				.build().toUriString();
		}

		response.sendRedirect(targetUrl);
	}
}
