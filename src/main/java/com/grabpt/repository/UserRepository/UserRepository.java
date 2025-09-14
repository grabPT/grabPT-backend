package com.grabpt.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.Role;

public interface UserRepository extends JpaRepository<Users, Long> {
	Page<Users> findAllByProProfile_Category_Code(String categoryCode, Pageable pageable);

	// SELECT * FROM user WHERE username = ?1
	Optional<Users> findByNickname(String nickname);

	// SELECT * FROM user WHERE provider = ?1 and providerId = ?2
	Optional<Users> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);

	Optional<Users> findByEmail(String email);

	boolean existsByNickname(String nickname);

	@Query("""
		    select case when count(u) > 0 then true else false end
		    from Users u
		    where u.phone_number = :phone
		      and u.deletedAt is null
		""")
	boolean existsActiveByPhone(@Param("phone") String phone);

	List<Users> findByRoleAndDeletedAtBefore(Role role, LocalDateTime deletedAt);

	boolean existsByEmail(String email);
}
