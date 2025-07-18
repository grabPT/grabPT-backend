package com.grabpt.repository.ProProfileRepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Users;

public interface ProProfileRepository extends JpaRepository<ProProfile, Long> {
	Optional<ProProfile> findByUser(Users user);

	//	@Query("""
	//    SELECT DISTINCT p
	//    FROM ProProfile p
	//    JOIN p.center c
	//    JOIN p.categories cat
	//    WHERE cat.code = :categoryCode
	//    AND c.address LIKE %:region%
	//	""")
	//	List<ProProfile> findAllProByCategoryCodeAndRegion(String categoryCode, String region);
	//}
}
