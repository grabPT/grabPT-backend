package com.grabpt.service.ChatService;

import com.grabpt.repository.ChatRepository.ChatRoomRepository;
import com.grabpt.repository.ChatRepository.MessageRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ChatServiceImplTest {


	@Autowired
	private ChatRoomRepository chatRoomRepository;
	@Autowired
	private MessageRepository messageRepository;


	@Test
	void getLastUnReadMessageId(){

	}

	@Test
	void getUnreadMessageCount(){
		Long count = messageRepository.countByRoomIdAndIdGreaterThan(1L, 13L);
		System.out.println(count);
	}
}
