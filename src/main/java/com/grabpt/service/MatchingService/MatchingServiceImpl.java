package com.grabpt.service.MatchingService;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.RequestionHandler;
import com.grabpt.apiPayload.exception.handler.SuggestionHandler;
import com.grabpt.domain.entity.Matching;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.entity.Suggestions;
import com.grabpt.domain.enums.MatchingStatus;
import com.grabpt.domain.enums.RequestStatus;
import com.grabpt.repository.MatchingRepository.MatchingRepository;
import com.grabpt.repository.RequestionRepository.RequestionRepository;
import com.grabpt.repository.SuggestionRepository.SuggestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

	private final RequestionRepository requestionRepository;
	private final SuggestionRepository suggestionRepository;
	private final MatchingRepository matchingRepository;

	@Override
	public Matching createMatching(Long requestionId, Long suggestionId) {

		Requestions requestion = requestionRepository.findById(requestionId)
			.orElseThrow(() -> new RequestionHandler(ErrorStatus.REQUESTION_NOT_FOUND));

		if (requestion.getStatus() == RequestStatus.MATCHED) {
			throw new RequestionHandler(ErrorStatus.REQUESTION_ALREADY_MATCHED);
		}

		Suggestions suggestion = suggestionRepository.findById(suggestionId)
			.orElseThrow(() -> new SuggestionHandler(ErrorStatus.SUGGESTION_NOT_FOUND));

		// Matching 생성
		Matching matching = Matching.builder()
			.requestion(requestion)
			.suggestion(suggestion)
			.agreedPrice(suggestion.getPrice())
			.matchedAt(LocalDateTime.now())
			.status(MatchingStatus.MATCHED)
			.build();

		// 요청서 상태 변경
		requestion.setStatus(RequestStatus.MATCHED);

		matchingRepository.save(matching);
		requestionRepository.save(requestion); // 상태 저장 반영

		return matching;

	}

	@Override
	public Matching updateStatus(Long matchingId, MatchingStatus newStatus) {
		Matching matching = matchingRepository.findById(matchingId)
			.orElseThrow(() -> new RuntimeException("매칭 정보를 찾을 수 없습니다."));

		if (matching.getStatus() == MatchingStatus.CANCELLED || matching.getStatus() == MatchingStatus.COMPLETED) {
			throw new IllegalStateException("이미 처리된 매칭입니다.");
		}

		// 상태 조건 체크
		if (matching.getStatus() != MatchingStatus.MATCHED) {
			throw new IllegalStateException("현재 상태에서는 변경할 수 없습니다. (현재 상태: " + matching.getStatus() + ")");
		}

		matching.setStatus(newStatus);

		if (newStatus == MatchingStatus.CANCELLED || newStatus == MatchingStatus.COMPLETED) {
			Requestions requestion = matching.getRequestion();
			requestion.setStatus(RequestStatus.MATCHING);
			requestionRepository.save(requestion);
		}

		return matchingRepository.save(matching);
	}
}
