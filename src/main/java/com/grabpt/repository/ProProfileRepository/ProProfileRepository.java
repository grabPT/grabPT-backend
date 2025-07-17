package com.grabpt.repository.ProProfileRepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Users;

public interface ProProfileRepository extends JpaRepository<ProProfile, Long> {
	Optional<ProProfile> findByUser(Users user);
}
