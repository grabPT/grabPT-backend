package com.grabpt.config.oauth;

public class CookieProfiles {
	public record CookieProfile(String domain, boolean secure, String sameSite) {
	}

	public static CookieProfile decideByTarget(String targetUri) {
		try {
			var uri = java.net.URI.create(targetUri);
			String host = uri.getHost();
			String scheme = uri.getScheme();

			boolean isLocalHost = host != null && (
				host.equals("localhost") || host.equals("127.0.0.1")
			);
			boolean isHttp = "http".equalsIgnoreCase(scheme);

			if (isLocalHost || isHttp) {
				// 로컬 개발: host-only, Secure=false, SameSite=Lax (XHR 포함 대부분 요청에 쿠키 전송)
				return new CookieProfile(null, false, "Lax");
			}
			// 배포: 최상위 도메인, Secure=true, SameSite=None (크로스사이트 허용)
			return new CookieProfile("grabpt.com", true, "None");

		} catch (Exception e) {
			// 문제 시 배포 프로필로
			return new CookieProfile("grabpt.com", true, "None");
		}
	}
}
