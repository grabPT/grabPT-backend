package com.grabpt.repository.ChatRepository;

import com.grabpt.domain.entity.Messages;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Messages, Long> {
}
