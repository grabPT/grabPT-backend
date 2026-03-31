package com.grabpt.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grabpt.apiPayload.ApiResponse;
import com.grabpt.config.SecurityUtils;
import com.grabpt.dto.request.RefundRequest;
import com.grabpt.dto.response.RefundResponse;
import com.grabpt.service.RefundService.RefundService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/refund")
public class RefundController {

    private final RefundService refundService;

    @Operation(
        summary = "환불 신청 API (USER)",
        description = "결제 완료된 주문에 대해 환불을 신청합니다. " +
            "계약 시작일 전이면 자동 전액 환불, 시작일 이후이면 PRO 확인 후 처리됩니다."
    )
    @PostMapping("/request")
    public ApiResponse<RefundResponse.RefundResponseDto> requestRefund(
        @RequestBody RefundRequest.RefundRequestDto request) {
        Long userId = SecurityUtils.currentUserIdOrThrow();
        return ApiResponse.onSuccess(refundService.requestRefund(userId, request));
    }

    @Operation(
        summary = "환불 승인 API (PRO)",
        description = "PENDING 상태의 환불 신청을 승인합니다. PortOne 환불 API를 호출하여 실제 환불을 처리합니다."
    )
    @PatchMapping("/{refundId}/approve")
    public ApiResponse<RefundResponse.RefundResponseDto> approveRefund(
        @PathVariable Long refundId) {
        Long proUserId = SecurityUtils.currentUserIdOrThrow();
        return ApiResponse.onSuccess(refundService.approveRefund(proUserId, refundId));
    }

    @Operation(
        summary = "환불 거절 API (PRO)",
        description = "PENDING 상태의 환불 신청을 거절합니다."
    )
    @PatchMapping("/{refundId}/reject")
    public ApiResponse<RefundResponse.RefundResponseDto> rejectRefund(
        @PathVariable Long refundId,
        @RequestBody RefundRequest.RefundRejectDto request) {
        Long proUserId = SecurityUtils.currentUserIdOrThrow();
        return ApiResponse.onSuccess(refundService.rejectRefund(proUserId, refundId, request));
    }

    @Operation(
        summary = "환불 정보 조회 API (USER/PRO)",
        description = "특정 환불 신청의 상세 정보를 조회합니다. 해당 계약의 USER 또는 PRO만 조회 가능합니다."
    )
    @GetMapping("/{refundId}")
    public ApiResponse<RefundResponse.RefundResponseDto> getRefund(
        @PathVariable Long refundId) {
        Long userId = SecurityUtils.currentUserIdOrThrow();
        return ApiResponse.onSuccess(refundService.getRefund(userId, refundId));
    }

    @Operation(
        summary = "내 환불 내역 조회 API (USER)",
        description = "로그인한 USER의 환불 신청 내역을 페이지네이션으로 조회합니다."
    )
    @GetMapping("/my")
    public ApiResponse<Page<RefundResponse.RefundResponseDto>> getMyRefunds(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.currentUserIdOrThrow();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        return ApiResponse.onSuccess(refundService.getMyRefunds(userId, pageable));
    }
}
