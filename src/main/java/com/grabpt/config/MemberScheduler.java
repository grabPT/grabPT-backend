package com.grabpt.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Role;
import com.grabpt.repository.UserRepository.UserRepository;

@Component
public class MemberScheduler {
	@Autowired
	private UserRepository userRepository;

	// 서버를 위한 일단 주석 처리
	// @Scheduled(cron = "0 0 2 * * *")
	@Transactional
	public void deleteExpiredWithdrawnUsers() {
		LocalDateTime deleteDate = LocalDateTime.now().minusDays(30);
		List<Users> expiredUsers = userRepository.findByRoleAndDeletedAtBefore(Role.DELETED, deleteDate);

		for (Users user : expiredUsers) {
			user.setUsername("탈퇴한 회원");
			user.setNickname("탈퇴한 회원");
			user.setEmail("deleted@" + user.getId());
			user.setOauthId(null);
			user.setOauthProvider(null);
			user.setPhone_number("");
			user.setProfileImageUrl(null);
			user.setRefreshToken(null);
			user.setUserProfile(null);
			user.setProProfile(null);
		}


	}
}
