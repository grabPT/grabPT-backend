package com.grabpt.domain.enums;

import org.springframework.security.core.GrantedAuthority;

public enum AuthRole implements GrantedAuthority {
	ROLE_USER, ROLE_ADMIN;

	@Override
	public String getAuthority() {
		return name();
	}
}
