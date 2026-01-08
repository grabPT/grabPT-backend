package com.grabpt.service.MatchingService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.RequestionHandler;
import com.grabpt.apiPayload.exception.handler.SuggestionHandler;
import com.grabpt.domain.entity.Contract;
import com.grabpt.domain.entity.Matching;
import com.grabpt.domain.entity.Requestions;
import com.grabpt.domain.entity.Suggestions;
import com.grabpt.domain.enums.MatchingStatus;
import com.grabpt.domain.enums.RequestStatus;
import com.grabpt.domain.enums.SuggestStatus;
import com.grabpt.dto.response.ContractResponse;
import com.grabpt.repository.MatchingRepository.MatchingRepository;
import com.grabpt.repository.RequestionRepository.RequestionRepository;
import com.grabpt.repository.SuggestionRepository.SuggestionRepository;
import com.grabpt.service.ContractService.ContractService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingServiceImpl implements MatchingService {

	private final MatchingRepository matchingRepository;

	private final RequestionRepository requestionRepository;
	private final SuggestionRepository suggestionRepository;
	private final ContractService contractService;

	@Override
	@Transactional
	public ContractResponse.CreateMatchingAndContractResponseDto createMatching(Long requestionId, Long suggestionId) {

		log.info("[MATCH] reqId={}", requestionId);

		// 1) 잠금 걸고 가져오기
		Requestions requestion = requestionRepository.findByIdForUpdate(requestionId)
			.orElseThrow(() -> new RequestionHandler(ErrorStatus.REQUESTION_NOT_FOUND));

		log.info("[MATCH] req.status={} (enumName={})",
			requestion.getStatus(),
			requestion.getStatus() != null ? requestion.getStatus().name() : "null");

		// 2) 매칭 가능 상태만 허용
		if (requestion.getStatus() != RequestStatus.MATCHING) {
			log.warn("[MATCH] BLOCK: status is not MATCHING. actual={}", requestion.getStatus());
			throw new RequestionHandler(ErrorStatus.REQUESTION_ALREADY_MATCHED);
		}

		// 3) 제안서 로드 + 연결 검증
		Suggestions suggestion = suggestionRepository.findById(suggestionId)
			.orElseThrow(() -> new SuggestionHandler(ErrorStatus.SUGGESTION_NOT_FOUND));
		if (!suggestion.getRequestion().getId().equals(requestionId)) {
			log.warn("[MATCH] suggestion {} belongs to requestion {}, not {}",
				suggestionId, suggestion.getRequestion().getId(), requestionId);
			throw new SuggestionHandler(ErrorStatus.INVALID_SUGGESTION_FOR_REQUESTION);
		}

		// 4) 선중복 체크
		if (matchingRepository.existsByRequestionId(requestionId)) {
			throw new RequestionHandler(ErrorStatus.REQUESTION_ALREADY_MATCHED);
		}
		if (matchingRepository.existsBySuggestionId(suggestionId)) {
			throw new SuggestionHandler(ErrorStatus.SUGGESTION_ALREADY_MATCHED);
		}

		// 5) 매칭 생성/저장
		Matching matching = Matching.builder()
			.requestion(requestion)
			.suggestion(suggestion)
			.agreedPrice(suggestion.getPrice())
			.matchedAt(LocalDateTime.now())
			.status(MatchingStatus.WAITING)
			.build();

		// 연관관계 설정
		requestion.setMatching(matching);

		try {
			matchingRepository.save(matching);
		} catch (DataIntegrityViolationException e) {
			throw new RequestionHandler(ErrorStatus.REQUESTION_ALREADY_MATCHED);
		}

		// 6) 요청서 상태 변경
		requestion.setStatus(RequestStatus.MATCHED);
		suggestion.setStatus(SuggestStatus.MATCHED);

		// 7) 계약 생성
		Contract contract = contractService.createContract(matching, requestion, suggestion);

		return ContractResponse.CreateMatchingAndContractResponseDto.builder()
			.matchingId(matching.getId())
			.contractId(contract.getId())
			.build();
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

	@Override
	public Matching findMatchingBySuggestionId(Long suggestionId) {
		return matchingRepository.findMatchingBySuggestionId(suggestionId);
	}

	@Override
	public boolean existsByRequestionId(Long requestionId) {
		return matchingRepository.existsByRequestionId(requestionId);
	}

	@Override
	public List<Matching> findAllWithProByRequestionIds(List<Long> requestionIds) {
		return matchingRepository.findAllWithProByRequestionIds(requestionIds);
	}

	@Override
	public Long getActiveClients(Long proProfileId, MatchingStatus matchingStatus) {
		return matchingRepository.getActiveClients(proProfileId, matchingStatus);
	}

	@Override
	public Long getActiveContractsByUser(Long userId, MatchingStatus matchingStatus) {
		return matchingRepository.getActiveContractsByUser(userId, matchingStatus);
	}

	@Override
	public Optional<Matching> findById(Long matchingId) {
		return matchingRepository.findById(matchingId);
	}

	@Override
	public List<Matching> matchings(List<Long> requestionIds) {
		return matchingRepository.findAllWithProByRequestionIds(requestionIds);
	}

}
