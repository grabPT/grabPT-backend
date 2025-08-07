package com.grabpt.repository.AlarmRepository;

import com.grabpt.domain.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
	@Query("SELECT a FROM Alarm a WHERE a.user.id = :userId")
	List<Alarm> findAllByUserId(Long userId);
}
