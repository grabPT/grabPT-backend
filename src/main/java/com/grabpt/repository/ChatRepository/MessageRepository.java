package com.grabpt.repository.ChatRepository;

import com.grabpt.domain.entity.Messages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Messages, Long> {
	@Query("SELECT m FROM Messages m WHERE m.chatRoom.id = :roomId")
	List<Messages> findAllByChatRoom(@Param("roomId") Long roomId);
}
