package com.grabpt.repository.UserRepository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.grabpt.domain.entity.Users;

public interface UserRepository extends JpaRepository<Users, Long> {
	Page<Users> findAllByProProfile_Category_Code(String categoryCode, Pageable pageable);

	// SELECT * FROM user WHERE username = ?1
	Optional<Users> findByNickname(String nickname);

	// SELECT * FROM user WHERE provider = ?1 and providerId = ?2
	Optional<Users> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);

	Optional<Users> findByEmail(String email);

	boolean existsByNickname(String nickname);
}
