//package com.grabpt.repository.ProProfileRepository;
//
//import com.grabpt.domain.entity.ProProfile;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//
//import java.util.List;
//
//public interface ProProfileRepository extends JpaRepository<ProProfile, Long> {
//
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
