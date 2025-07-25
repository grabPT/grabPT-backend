package com.grabpt.repository.ProProfileRepository;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProProfileRepository extends JpaRepository<ProProfile, Long> {
	Optional<ProProfile> findByUser(Users user);
	@Query("""
    SELECT p
    FROM ProProfile p
    JOIN p.category cat
    JOIN FETCH p.user u
    JOIN u.address addr
    WHERE cat.code = :categoryCode
    AND addr.street LIKE %:region%
	""")
	List<ProProfile> findAllProByCategoryCodeAndRegion(String categoryCode, String region);

	Page<ProProfile> findByCategory_Code(String categoryCode, Pageable pageable);
}
