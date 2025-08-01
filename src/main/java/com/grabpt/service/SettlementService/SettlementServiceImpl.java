package com.grabpt.service.SettlementService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.grabpt.domain.enums.MatchingStatus;
import com.grabpt.domain.enums.PaymentStatus;
import com.grabpt.dto.response.MemberPaymentDto;
import com.grabpt.dto.response.TrainerDashboardDto;
import com.grabpt.repository.MatchingRepository.MatchingRepository;
import com.grabpt.repository.OrderRepository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementServiceImpl implements SettlementService {

	private final OrderRepository orderRepository;
	private final MatchingRepository matchingRepository;

	@Override
	public TrainerDashboardDto getTrainerDashboard(Long proProfileId, int page, int size) {
		Long totalEarnings = orderRepository.getTrainerTotalEarnings(proProfileId, PaymentStatus.OK);
		Long totalOrders = orderRepository.getTrainerTotalOrders(proProfileId, PaymentStatus.OK);
		Long activeClients = matchingRepository.getActiveClients(proProfileId, MatchingStatus.MATCHED);

		Pageable pageable = PageRequest.of(page, size, Sort.by("paymentDate").descending());
		Page<MemberPaymentDto> memberPayments
			= orderRepository.getMemberPayments(proProfileId, PaymentStatus.OK, pageable);

		return TrainerDashboardDto.builder()
			.totalEarnings(totalEarnings)
			.totalOrders(totalOrders)
			.activeClients(activeClients)
			.memberPayments(memberPayments)
			.build();
	}
}
