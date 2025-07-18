package com.grabpt.service.ChatService;

import com.grabpt.domain.entity.ChatRooms;
import com.grabpt.domain.entity.MessageRead;
import com.grabpt.repository.ChatRepository.ChatRoomRepository;
import com.grabpt.repository.ChatRepository.MessageReadRepository;
import com.grabpt.repository.ChatRepository.MessageRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChatServiceImplTest {

	@Autowired
	private MessageReadRepository messageReadRepository;
	@Autowired
	private ChatRoomRepository chatRoomRepository;
	@Autowired
	private MessageRepository messageRepository;


	@Test
	void getLastUnReadMessageId(){
		MessageRead messageRead = messageReadRepository.findByRoomIdAndUserId(1L, 1L).get();
		//System.out.println(messageRead);
		Assertions.assertThat(messageRead.getLastReadMessageId()).isEqualTo(17L);
	}

	@Test
	void getUnreadMessageCount(){
		Long count = messageRepository.countByRoomIdAndIdGreaterThan(1L, 13L);
		System.out.println(count);
	}
}
