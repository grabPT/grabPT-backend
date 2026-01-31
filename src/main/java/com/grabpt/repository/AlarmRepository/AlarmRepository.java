package com.grabpt.repository.AlarmRepository;

import com.grabpt.domain.entity.Alarm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
	@Query("SELECT a FROM Alarm a WHERE a.user.id = :userId AND a.isRead = false order by a.id DESC")
	List<Alarm> findAllUnreadAlarmByUserId(Long userId); //읽지 않은 것들만 조회

	@Query("SELECT a FROM Alarm a WHERE a.user.id = :userId order by a.id DESC")
	Page<Alarm> findAllAlarmByUserId(Pageable pageable, Long userId);

	@Query("SELECT COUNT(a) FROM Alarm a WHERE a.user.id = :userId AND a.isRead = false")
	Long countUnReadAlarmByUserId(Long userId);
}
