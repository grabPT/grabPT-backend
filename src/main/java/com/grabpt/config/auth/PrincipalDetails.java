package com.grabpt.config.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.grabpt.domain.entity.Users;

// Authentication 객체에 저장할 수 있는 유일한 타입
public class PrincipalDetails implements UserDetails, OAuth2User {

	private static final long serialVersionUID = 1L;
	private Users user;
	private Map<String, Object> attributes;

	// 일반 시큐리티 로그인시 사용
	public PrincipalDetails(Users user) {
		this.user = user;
	}

	// OAuth2.0 로그인시 사용
	public PrincipalDetails(Users user, Map<String, Object> attributes) {
		this.user = user;
		this.attributes = attributes;
	}

	public Users getUser() {
		return user;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> collet = new ArrayList<GrantedAuthority>();
		collet.add(user.getAuthRole());
		return collet;
	}

	// 리소스 서버로 부터 받는 회원정보
	@Override
	public Map<String, Object> getAttributes() {
		return attributes != null ? attributes : Collections.emptyMap();
	}

	// User의 PrimaryKey
	@Override
	public String getName() {
		// 가장 안정적인 건 이메일
		if (user.getEmail() != null && !user.getEmail().isBlank()) {
			return user.getEmail();
		}
		// 이메일이 없다면 provider + "-" + oauthId 조합
		if (user.getOauthProvider() != null && user.getOauthId() != null) {
			return user.getOauthProvider() + "-" + user.getOauthId();
		}
		// 최후의 수단: 임시 UUID(하지만 가급적 위 둘 중 하나를 보장하세요)
		return java.util.UUID.randomUUID().toString();
	}

}
