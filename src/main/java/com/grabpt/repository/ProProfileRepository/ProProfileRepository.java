package com.grabpt.repository.ProProfileRepository;

import com.grabpt.domain.entity.ProProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProProfileRepository extends JpaRepository<ProProfile, Long> {

	@Query("""
    SELECT p
    FROM ProProfile p
    JOIN p.categories cat
    JOIN FETCH p.user u
    JOIN FETCH p.center c
    WHERE cat.code = :categoryCode
    AND p.center.centerAddress LIKE %:region%
	""")
	List<ProProfile> findAllProByCategoryCodeAndRegion(String categoryCode, String region);

}
