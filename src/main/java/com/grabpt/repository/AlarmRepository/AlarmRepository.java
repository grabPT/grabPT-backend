package com.grabpt.repository.AlarmRepository;

import com.grabpt.domain.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
	@Query("SELECT a FROM Alarm a WHERE a.user.id = :userId AND a.isRead = false")
	List<Alarm> findAllUnReadAlarmByUserId(Long userId); //읽지 않은 것들만 조회

	@Query("SELECT COUNT(a) FROM Alarm a WHERE a.user.id = :userId AND a.isRead = false")
	Long countUnReadAlarmByUserId(Long userId);
}
