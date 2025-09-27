package com.grabpt.service.SettlementService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.grabpt.domain.enums.MatchingStatus;
import com.grabpt.domain.enums.PaymentStatus;
import com.grabpt.dto.response.MemberPaymentDto;
import com.grabpt.dto.response.TrainerDashboardDto;
import com.grabpt.dto.response.UserDashboardDto;
import com.grabpt.dto.response.UserDashboardResponseDto;
import com.grabpt.service.MatchingService.MatchingService;
import com.grabpt.service.OrderService.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementServiceImpl implements SettlementService {

	private final OrderService orderService;
	private final MatchingService matchingService;

	@Override
	public TrainerDashboardDto getTrainerDashboard(Long proProfileId, int page, int size) {
		Long totalEarnings = orderService.getTrainerTotalEarnings(proProfileId, PaymentStatus.OK);
		Long totalOrders = orderService.getTrainerTotalOrders(proProfileId, PaymentStatus.OK);
		Long activeClients = matchingService.getActiveClients(proProfileId, MatchingStatus.COMPLETED);

		Pageable pageable = PageRequest.of(page, size);
		Page<MemberPaymentDto> memberPayments
			= orderService.getMemberPayments(proProfileId, PaymentStatus.OK, pageable);

		return TrainerDashboardDto.builder()
			.totalEarnings(totalEarnings)
			.totalOrders(totalOrders)
			.activeClients(activeClients)
			.memberPayments(memberPayments)
			.build();
	}

	@Override
	public UserDashboardResponseDto getUserDashboard(Long userId, int page, int size) {
		Long totalSpent = orderService.getUserTotalSpent(userId, PaymentStatus.OK);
		Long totalOrders = orderService.getUserTotalOrders(userId, PaymentStatus.OK);
		Long activeContracts = matchingService.getActiveContractsByUser(userId, MatchingStatus.COMPLETED);

		Pageable pageable = PageRequest.of(page, size);
		Page<UserDashboardDto> payments
			= orderService.getUserPayments(userId, PaymentStatus.OK, pageable);

		return UserDashboardResponseDto.builder()
			.totalSpent(nvl(totalSpent))
			.totalOrders(nvl(totalOrders))
			.activeContracts(nvl(activeContracts))
			.payments(payments)
			.build();
	}

	private Long nvl(Long v) {
		return v == null ? 0L : v;
	}

}
