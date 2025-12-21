package com.grabpt.config.oauth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 환경(로컬/개발/운영) 감지 및 설정 제공
 *
 * 환경 분류:
 * - LOCAL: localhost, 127.0.0.1
 * - DEV: Vercel 개발 서버 (grabpt-dev.vercel.app, dev-grabpt.vercel.app)
 * - PROD: 운영 서버 (grabpt.com, www.grabpt.com, api.grabpt.com)
 */
@Slf4j
public class EnvironmentDetector {

	public enum Environment {
		LOCAL,   // localhost 개발 환경
		DEV,     // Vercel 개발 서버
		PROD     // 운영 서버
	}

	/**
	 * 환경 설정 프로필
	 */
	public static class EnvironmentProfile {
		public final Environment env;
		public final String cookieDomain;  // null = host-only
		public final String sameSite;      // "None" or "Lax"
		public final boolean secure;       // HTTPS 전용 여부
		public final boolean useUrlParams; // URL 파라미터 사용 여부

		public EnvironmentProfile(Environment env, String cookieDomain, String sameSite,
			boolean secure, boolean useUrlParams) {
			this.env = env;
			this.cookieDomain = cookieDomain;
			this.sameSite = sameSite;
			this.secure = secure;
			this.useUrlParams = useUrlParams;
		}

		@Override
		public String toString() {
			return String.format("Env[%s, domain=%s, sameSite=%s, secure=%s, urlParams=%s]",
				env, cookieDomain, sameSite, secure, useUrlParams);
		}
	}

	/**
	 * 요청으로부터 환경 프로필 결정
	 */
	public static EnvironmentProfile detectEnvironment(HttpServletRequest request) {
		String host = getHost(request);
		Environment env = classifyEnvironment(host);

		EnvironmentProfile profile = createProfile(env);

		log.debug("[ENV] Detected environment: host={}, profile={}", host, profile);

		return profile;
	}

	/**
	 * Frontend Base URL로부터 환경 프로필 결정
	 */
	public static EnvironmentProfile detectEnvironment(String frontendBase) {
		if (frontendBase == null) {
			return createProfile(Environment.PROD);
		}

		String normalized = frontendBase.toLowerCase();
		Environment env;

		if (normalized.contains("localhost") || normalized.contains("127.0.0.1")) {
			env = Environment.LOCAL;
		} else if (normalized.contains("grabpt-dev.vercel.app")
			|| normalized.contains("dev-grabpt.vercel.app")) {
			env = Environment.DEV;
		} else {
			env = Environment.PROD;
		}

		EnvironmentProfile profile = createProfile(env);
		log.debug("[ENV] Detected from frontend base: base={}, profile={}", frontendBase, profile);

		return profile;
	}

	/**
	 * 호스트명 추출
	 */
	private static String getHost(HttpServletRequest request) {
		// 프록시 환경 대응: X-Forwarded-Host 우선
		String forwardedHost = request.getHeader("X-Forwarded-Host");
		if (forwardedHost != null && !forwardedHost.isBlank()) {
			return forwardedHost;
		}

		return request.getServerName();
	}

	/**
	 * 호스트명으로부터 환경 분류
	 */
	private static Environment classifyEnvironment(String host) {
		if (host == null) {
			return Environment.PROD;
		}

		String normalized = host.toLowerCase();

		// 로컬 개발 환경
		if (normalized.contains("localhost") || normalized.contains("127.0.0.1")) {
			return Environment.LOCAL;
		}

		// Vercel 개발 서버
		if (normalized.contains("vercel.app")) {
			return Environment.DEV;
		}

		// 그 외 운영 환경
		return Environment.PROD;
	}

	/**
	 * 환경별 프로필 생성
	 */
	private static EnvironmentProfile createProfile(Environment env) {
		return switch (env) {
			case LOCAL -> new EnvironmentProfile(
				Environment.LOCAL,
				null,           // host-only cookie
				"Lax",          // same-site 충분
				false,          // HTTP 허용
				true            // URL 파라미터 사용
			);

			case DEV -> new EnvironmentProfile(
				Environment.DEV,
				null,           // host-only (vercel.app ≠ grabpt.com)
				"None",         // cross-site 필요
				true,           // HTTPS 필수
				true            // URL 파라미터 사용 (쿠키 도메인 불일치)
			);

			case PROD -> new EnvironmentProfile(
				Environment.PROD,
				"grabpt.com",   // 도메인 공유
				"None",         // cross-site 필요
				true,           // HTTPS 필수
				false           // 쿠키만 사용 (보안 강화)
			);
		};
	}

	/**
	 * 현재 환경이 개발 환경인지 확인 (LOCAL 또는 DEV)
	 */
	public static boolean isDevelopment(EnvironmentProfile profile) {
		return profile.env == Environment.LOCAL || profile.env == Environment.DEV;
	}

	/**
	 * 현재 환경이 운영 환경인지 확인
	 */
	public static boolean isProduction(EnvironmentProfile profile) {
		return profile.env == Environment.PROD;
	}
}
