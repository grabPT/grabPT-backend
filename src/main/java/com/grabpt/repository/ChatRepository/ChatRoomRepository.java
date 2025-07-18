package com.grabpt.repository.ChatRepository;

import com.grabpt.domain.entity.ChatRooms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRooms,Long> {
}
