package com.grabpt.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.grabpt.domain.enums.RequestStatus;
import com.grabpt.repository.RequestionRepository.RequestionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestionScheduler {

	private final RequestionRepository requestionRepository;

	/**
	 * 매일 자정에 만료된 요청서를 CLOSED 상태로 변경
	 */
	@Scheduled(cron = "0 0 0 * * *")
	@Transactional
	public void closeExpiredRequestions() {
		log.info("[Scheduler] Starting to close expired requestions at {}", LocalDateTime.now());

		List<RequestStatus> targetStatuses = List.of(RequestStatus.WAITING, RequestStatus.MATCHING);
		int updatedCount = requestionRepository.bulkUpdateExpiredRequestions(
			targetStatuses,
			RequestStatus.CLOSED,
			LocalDateTime.now()
		);

		log.info("[Scheduler] Closed {} expired requestions", updatedCount);
	}
}
