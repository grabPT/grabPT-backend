package com.grabpt.config;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
	private SecurityUtils() {
	}

	public static com.grabpt.config.auth.PrincipalDetails currentUserOrThrow() {
		Authentication a = SecurityContextHolder.getContext().getAuthentication();
		if (a == null || !a.isAuthenticated() || "anonymousUser".equals(a.getPrincipal())) {
			throw new AuthenticationCredentialsNotFoundException("Unauthorized");
		}
		return (com.grabpt.config.auth.PrincipalDetails)a.getPrincipal();
	}

	public static Long currentUserIdOrThrow() {
		return currentUserOrThrow().getUser().getId();
	}
}
