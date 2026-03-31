package com.grabpt.service.RefundService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.RefundHandler;
import com.grabpt.converter.RefundConverter;
import com.grabpt.domain.entity.Contract;
import com.grabpt.domain.entity.Order;
import com.grabpt.domain.entity.Refund;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.PaymentStatus;
import com.grabpt.domain.enums.RefundStatus;
import com.grabpt.dto.request.RefundRequest;
import com.grabpt.dto.response.RefundResponse;
import com.grabpt.repository.OrderRepository.OrderRepository;
import com.grabpt.repository.RefundRepository.RefundRepository;
import com.grabpt.repository.UserRepository.UserRepository;
import com.grabpt.service.AlarmService.AlarmService;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AlarmService alarmService;
    private final IamportClient iamportClient;

    @Value("${import.api.key}")
    private String impApiKey;

    @Value("${import.api.secret}")
    private String impApiSecret;

    @Override
    public RefundResponse.RefundResponseDto requestRefund(Long userId, RefundRequest.RefundRequestDto request) {
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new RefundHandler(ErrorStatus.REFUND_ORDER_NOT_FOUND));

        // 결제 완료 상태 확인
        if (order.getPayment() == null || order.getPayment().getStatus() != PaymentStatus.OK) {
            throw new RefundHandler(ErrorStatus.REFUND_NOT_PAID);
        }

        // 중복 환불 신청 방지
        if (refundRepository.existsByOrderId(order.getId())) {
            throw new RefundHandler(ErrorStatus.REFUND_ALREADY_REQUESTED);
        }

        Users user = userRepository.findById(userId)
            .orElseThrow(() -> new RefundHandler(ErrorStatus.REFUND_UNAUTHORIZED));

        Contract contract = order.getMatching().getContract();
        Long proUserId = order.getMatching().getSuggestion().getProProfile().getUser().getId();
        Long refundAmount = order.getPayment().getPrice();
        LocalDate startDate = contract.getStartDate();

        Refund refund = Refund.builder()
            .order(order)
            .user(user)
            .reason(request.getReason())
            .refundAmount(refundAmount)
            .status(RefundStatus.PENDING)
            .build();

        // 시작일 전이면 자동 전액 환불
        if (startDate == null || !LocalDate.now().isAfter(startDate.minusDays(1))) {
            String refundUid = cancelPortOnePayment(order.getPayment().getPaymentUid(), refundAmount, request.getReason());
            order.getPayment().changePaymentByCancel();
            refund.autoApprove(refundUid);
            refundRepository.save(refund);

            alarmService.sendAlarm(userId, "REFUND", "환불 완료",
                "환불이 자동으로 완료되었습니다.", "/refund/" + refund.getId());
            alarmService.sendAlarm(proUserId, "REFUND", "환불 처리 완료",
                "계약 시작 전 환불 신청으로 자동 처리되었습니다.", "/refund/" + refund.getId());
        } else {
            // 시작일 이후이면 PRO 확인 대기
            refundRepository.save(refund);

            alarmService.sendAlarm(proUserId, "REFUND", "환불 신청 접수",
                "수강생이 환불을 신청했습니다. 확인 후 처리해주세요.", "/refund/" + refund.getId());
            alarmService.sendAlarm(userId, "REFUND", "환불 신청 완료",
                "환불 신청이 완료되었습니다. 트레이너 확인 후 처리됩니다.", "/refund/" + refund.getId());
        }

        return RefundConverter.toRefundResponseDto(refund);
    }

    @Override
    public RefundResponse.RefundResponseDto approveRefund(Long proUserId, Long refundId) {
        Refund refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new RefundHandler(ErrorStatus.REFUND_NOT_FOUND));

        validateProAuthority(proUserId, refund);

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new RefundHandler(ErrorStatus.REFUND_NOT_PENDING);
        }

        Order order = refund.getOrder();
        String refundUid = cancelPortOnePayment(
            order.getPayment().getPaymentUid(),
            refund.getRefundAmount(),
            refund.getReason()
        );

        order.getPayment().changePaymentByCancel();
        refund.approve(refundUid);

        Long userId = refund.getUser().getId();
        alarmService.sendAlarm(userId, "REFUND", "환불 승인",
            "환불 신청이 승인되어 처리되었습니다.", "/refund/" + refundId);
        alarmService.sendAlarm(proUserId, "REFUND", "환불 승인 완료",
            "환불 승인이 완료되었습니다.", "/refund/" + refundId);

        return RefundConverter.toRefundResponseDto(refund);
    }

    @Override
    public RefundResponse.RefundResponseDto rejectRefund(Long proUserId, Long refundId, RefundRequest.RefundRejectDto request) {
        Refund refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new RefundHandler(ErrorStatus.REFUND_NOT_FOUND));

        validateProAuthority(proUserId, refund);

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new RefundHandler(ErrorStatus.REFUND_NOT_PENDING);
        }

        refund.reject(request.getRejectionReason());

        Long userId = refund.getUser().getId();
        alarmService.sendAlarm(userId, "REFUND", "환불 거절",
            "환불 신청이 거절되었습니다. 사유: " + request.getRejectionReason(), "/refund/" + refundId);

        return RefundConverter.toRefundResponseDto(refund);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundResponse.RefundResponseDto getRefund(Long userId, Long refundId) {
        Refund refund = refundRepository.findById(refundId)
            .orElseThrow(() -> new RefundHandler(ErrorStatus.REFUND_NOT_FOUND));

        Long refundUserId = refund.getUser().getId();
        Long proUserId = refund.getOrder().getMatching().getSuggestion().getProProfile().getUser().getId();

        if (!userId.equals(refundUserId) && !userId.equals(proUserId)) {
            throw new RefundHandler(ErrorStatus.REFUND_UNAUTHORIZED);
        }

        return RefundConverter.toRefundResponseDto(refund);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RefundResponse.RefundResponseDto> getMyRefunds(Long userId, Pageable pageable) {
        return refundRepository.findAllByUserId(userId, pageable)
            .map(RefundConverter::toRefundResponseDto);
    }

    private void validateProAuthority(Long proUserId, Refund refund) {
        Long contractProUserId = refund.getOrder().getMatching().getSuggestion().getProProfile().getUser().getId();
        if (!proUserId.equals(contractProUserId)) {
            throw new RefundHandler(ErrorStatus.REFUND_UNAUTHORIZED);
        }
    }

    private String cancelPortOnePayment(String paymentUid, Long amount, String reason) {
        try {
            CancelData cancelData = new CancelData(paymentUid, true, new BigDecimal(amount));
            cancelData.setReason(reason != null ? reason : "환불 신청");

            com.siot.IamportRestClient.response.IamportResponse<com.siot.IamportRestClient.response.Payment> response =
                iamportClient.cancelPaymentByImpUid(cancelData);

            if (response.getCode() != 0) {
                log.error("PortOne 환불 실패: {}", response.getMessage());
                throw new RefundHandler(ErrorStatus.REFUND_FAILED);
            }

            return response.getResponse().getImpUid();

        } catch (IamportResponseException | IOException e) {
            log.error("PortOne 환불 처리 중 오류 발생: {}", e.getMessage());
            throw new RefundHandler(ErrorStatus.REFUND_FAILED);
        }
    }
}
