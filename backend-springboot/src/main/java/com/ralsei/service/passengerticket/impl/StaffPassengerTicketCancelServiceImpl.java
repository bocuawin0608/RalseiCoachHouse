package com.ralsei.service.passengerticket.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ralsei.dto.projection.staffpassengerticket.StaffPassengerTicketRowProjection;
import com.ralsei.dto.request.staffpassengerticket.StaffPassengerTicketCancelRequest;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerTicketDetailResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Payment;
import com.ralsei.model.PassengerTicket;
import com.ralsei.model.Refund;
import com.ralsei.model.Trip;
import com.ralsei.model.enums.PassengerTicketDetailStatus;
import com.ralsei.model.enums.PassengerTicketStatus;
import com.ralsei.model.enums.TripSeatStatus;
import com.ralsei.repository.PassengerTicketDetailRepository;
import com.ralsei.repository.PassengerTicketRepository;
import com.ralsei.repository.PaymentRepository;
import com.ralsei.repository.RefundRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.repository.TripRepository;
import com.ralsei.repository.TripSeatRepository;
import com.ralsei.service.passengerticket.PassengerTicketStaffPolicy;
import com.ralsei.service.passengerticket.StaffPassengerTicketCancelService;
import com.ralsei.service.passengerticket.StaffPassengerTicketQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffPassengerTicketCancelServiceImpl implements StaffPassengerTicketCancelService {

    private final PassengerTicketDetailRepository ticketDetailRepository;
    private final PassengerTicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final TripSeatRepository tripSeatRepository;
    private final TripRepository tripRepository;
    private final StaffRepository staffRepository;
    private final PassengerTicketStaffPolicy policy;
    private final StaffPassengerTicketQueryService queryService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public StaffPassengerTicketDetailResponse cancelFull(
        Integer accountId,
        String ticketCode,
        StaffPassengerTicketCancelRequest request
    ) {
        staffRepository.findByAccountId(accountId)
            .orElseThrow(() -> new BusinessRuleException("Không tìm thấy thông tin nhân viên!"));

        String normalizedTicketCode = ticketCode == null ? "" : ticketCode.trim();
        List<StaffPassengerTicketRowProjection> rows =
            ticketDetailRepository.findStaffTicketRowsByTicketCode(normalizedTicketCode);

        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy vé.");
        }

        StaffPassengerTicketRowProjection first = rows.get(0);
        PassengerTicket ticketEntity = ticketRepository.findById(first.getPassengerTicketId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé."));
        Trip trip = tripRepository.findById(first.getTripId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến xe."));

        policy.assertCancelFullAllowed(
            first.getTicketStatus(),
            rows,
            first.getPaymentStatus(),
            first.getDepartureTime(),
            trip.getStatus(),
            ticketEntity.getMajorChangeType()
        );

        Payment payment = paymentRepository.findByPassengerTicketId(first.getPassengerTicketId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch thanh toán của vé."));

        if (refundRepository.existsByPaymentIdAndStatusIn(payment.getPaymentId(), List.of("PENDING", "COMPLETED"))) {
            throw new BusinessRuleException("Vé đã có yêu cầu hoàn tiền đang được xử lý.");
        }

        LocalDateTime refundPolicyDepartureTime = policy.resolveRefundPolicyDepartureTime(
            ticketEntity.getRefundPolicyDepartureTime(),
            first.getDepartureTime()
        );
        long refundHoursLeft = policy.hoursUntilDeparture(refundPolicyDepartureTime);
        BigDecimal refundAmount = policy.calculateRefundAmount(refundHoursLeft, payment.getAmount());
        String refundTierLabel = policy.resolveRefundTierLabel(refundHoursLeft);

        int updatedTickets = ticketRepository.updateStatusIfCurrent(
            first.getPassengerTicketId(),
            PassengerTicketStatus.CONFIRMED,
            PassengerTicketStatus.CANCELLED
        );
        if (updatedTickets == 0) {
            updatedTickets = ticketRepository.updateStatusIfCurrent(
                first.getPassengerTicketId(),
                PassengerTicketStatus.CHANGED,
                PassengerTicketStatus.CANCELLED
            );
        }
        if (updatedTickets != 1) {
            throw new BusinessRuleException("Trạng thái vé vừa thay đổi. Vui lòng tải lại chi tiết.");
        }

        ticketDetailRepository.updateStatusByPassengerTicketId(
            first.getPassengerTicketId(),
            PassengerTicketDetailStatus.CANCELLED.name()
        );

        List<Integer> seatIds = ticketDetailRepository.findTripSeatIdsByPassengerTicketId(first.getPassengerTicketId());
        if (!seatIds.isEmpty()) {
            tripSeatRepository.updateStatusByTripSeatIds(seatIds, TripSeatStatus.AVAILABLE);
        }

        payment.setRefundAmount(refundAmount);
        payment.setUpdatedBy(accountId);
        paymentRepository.save(payment);

        String reason = buildRefundReason(request, refundTierLabel);
        Refund refund = Refund.builder()
            .paymentId(payment.getPaymentId())
            .amount(refundAmount)
            .reason(reason)
            .refundMethod("BANK_TRANSFER")
            .status("PENDING")
            .callbackData(serializeBankDestination(request))
            .build();
        refund.setCreatedBy(accountId);
        refundRepository.save(refund);

        return queryService.getDetail(normalizedTicketCode);
    }

    private String buildRefundReason(StaffPassengerTicketCancelRequest request, String refundTierLabel) {
        String base = "Nhân viên hủy vé - hoàn " + refundTierLabel;
        if (request.reason() != null && !request.reason().isBlank()) {
            return base + ": " + request.reason().trim();
        }
        return base;
    }

    private String serializeBankDestination(StaffPassengerTicketCancelRequest request) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                "bankName", request.bankName().trim(),
                "accountHolder", request.accountHolder().trim(),
                "accountNumber", request.accountNumber().trim()
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Không thể lưu thông tin nhận tiền hoàn.", exception);
        }
    }
}
