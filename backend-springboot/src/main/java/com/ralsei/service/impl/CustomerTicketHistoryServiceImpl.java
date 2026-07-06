package com.ralsei.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.customer.CustomerTicketHistoryProjection;
import com.ralsei.dto.response.customer.CustomerTicketHistoryResponse;
import com.ralsei.dto.response.customer.CustomerTicketHistoryResponse.CustomerTicketSeatResponse;
import com.ralsei.dto.request.customer.CustomerTicketCancellationRequest;
import com.ralsei.dto.response.customer.CustomerTicketCancellationResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Payment;
import com.ralsei.model.Refund;
import com.ralsei.model.enums.PassengerTicketDetailStatus;
import com.ralsei.model.enums.PassengerTicketStatus;
import com.ralsei.model.enums.TripSeatStatus;
import com.ralsei.repository.PassengerTicketDetailRepository;
import com.ralsei.repository.PassengerTicketRepository;
import com.ralsei.repository.PaymentRepository;
import com.ralsei.repository.RefundRepository;
import com.ralsei.repository.TripSeatRepository;
import com.ralsei.service.CustomerTicketHistoryService;
import com.ralsei.util.QRCreateUitility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * Reads customer-owned booking rows and converts their flat seat data into API responses.
 */
@Service
@RequiredArgsConstructor
public class CustomerTicketHistoryServiceImpl implements CustomerTicketHistoryService {

    private static final long CANCELLATION_CUTOFF_HOURS = 5;

    private final PassengerTicketDetailRepository ticketDetailRepository;
    private final PassengerTicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final TripSeatRepository tripSeatRepository;
    private final QRCreateUitility qrCreateUitility;
    private final ObjectMapper objectMapper;

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<CustomerTicketHistoryResponse> getHistory(Integer accountId) {
        validateAccountId(accountId);
        return assembleTickets(ticketDetailRepository.findCustomerTicketHistory(accountId, null));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public CustomerTicketHistoryResponse getDetail(Integer accountId, String ticketCode) {
        if (ticketCode == null || ticketCode.isBlank()) {
            throw new ResourceNotFoundException("Mã vé không hợp lệ.");
        }

        validateAccountId(accountId);
        return assembleTickets(ticketDetailRepository.findCustomerTicketHistory(accountId, ticketCode))
            .stream()
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé trong tài khoản của bạn."));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public byte[] getSeatQrImage(Integer accountId, Integer ticketDetailId) {
        validateAccountId(accountId);
        String token = ticketDetailRepository.findOwnedQrToken(ticketDetailId, accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã QR của ghế trong tài khoản của bạn."));
        return qrCreateUitility.createPng(token);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public CustomerTicketCancellationResponse cancelTicket(
        Integer accountId,
        String ticketCode,
        CustomerTicketCancellationRequest request
    ) {
        validateAccountId(accountId);

        CustomerTicketHistoryResponse ownedTicket = getOwnedTicket(accountId, ticketCode);
        if (!PassengerTicketStatus.CONFIRMED.name().equals(ownedTicket.status())) {
            throw new BusinessRuleException("Chỉ vé đã thanh toán và chưa hủy mới có thể yêu cầu hoàn tiền.");
        }
        LocalDateTime cancellationDeadline = LocalDateTime.now().plusHours(CANCELLATION_CUTOFF_HOURS);
        if (ownedTicket.departureTime() == null || !ownedTicket.departureTime().isAfter(cancellationDeadline)) {
            throw new BusinessRuleException("Chỉ có thể hủy vé trước giờ xuất bến ít nhất 5 tiếng.");
        }

        Payment payment = paymentRepository.findByPassengerTicketId(ownedTicket.passengerTicketId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch thanh toán của vé."));
        if (!"COMPLETED".equals(payment.getStatus())) {
            throw new BusinessRuleException("Thanh toán chưa hoàn tất nên không thể tạo yêu cầu hoàn tiền.");
        }
        if (refundRepository.existsByPaymentIdAndStatusIn(payment.getPaymentId(), List.of("PENDING", "COMPLETED"))) {
            throw new BusinessRuleException("Vé đã có yêu cầu hoàn tiền đang được xử lý.");
        }

        int updatedTickets = ticketRepository.updateStatusIfCurrent(
            ownedTicket.passengerTicketId(),
            PassengerTicketStatus.CONFIRMED,
            PassengerTicketStatus.CANCELLED
        );
        if (updatedTickets != 1) {
            throw new BusinessRuleException("Trạng thái vé vừa thay đổi. Vui lòng tải lại lịch sử.");
        }

        ticketDetailRepository.updateStatusByPassengerTicketId(
            ownedTicket.passengerTicketId(), PassengerTicketDetailStatus.CANCELLED.name());
        List<Integer> seatIds = ticketDetailRepository
            .findTripSeatIdsByPassengerTicketId(ownedTicket.passengerTicketId());
        if (!seatIds.isEmpty()) {
            tripSeatRepository.updateStatusByTripSeatIds(seatIds, TripSeatStatus.AVAILABLE);
        }

        BigDecimal refundAmount = payment.getAmount();
        payment.setRefundAmount(refundAmount);
        paymentRepository.save(payment);

        Refund refund = refundRepository.save(Refund.builder()
            .paymentId(payment.getPaymentId())
            .amount(refundAmount)
            .reason("Khách hàng hủy vé trước giờ xuất bến")
            .refundMethod("BANK_TRANSFER")
            .status("PENDING")
            .callbackData(serializeBankDestination(request))
            .build());

        return new CustomerTicketCancellationResponse(
            ownedTicket.ticketCode(),
            PassengerTicketStatus.CANCELLED.name(),
            refundAmount,
            refund.getStatus()
        );
    }

    /** Loads one ticket through the same account-and-phone ownership query as history. */
    private CustomerTicketHistoryResponse getOwnedTicket(Integer accountId, String ticketCode) {
        return assembleTickets(ticketDetailRepository.findCustomerTicketHistory(accountId, ticketCode))
            .stream()
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé trong tài khoản của bạn."));
    }

    /** Serializes bank details into the refund audit payload without changing the DDL. */
    private String serializeBankDestination(CustomerTicketCancellationRequest request) {
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

    /**
     * Rejects access tokens that do not contain a usable account identifier.
     */
    private void validateAccountId(Integer accountId) {
        if (accountId == null || accountId < 1) {
            throw new ResourceNotFoundException("Không xác định được tài khoản khách hàng.");
        }
    }

    /**
     * Groups one-row-per-seat projections into one response per master ticket.
     */
    private List<CustomerTicketHistoryResponse> assembleTickets(List<CustomerTicketHistoryProjection> rows) {
        Map<Integer, List<CustomerTicketHistoryProjection>> rowsByTicket = new LinkedHashMap<>();
        rows.forEach(row -> rowsByTicket
            .computeIfAbsent(row.getPassengerTicketId(), ignored -> new ArrayList<>())
            .add(row));

        return rowsByTicket.values().stream().map(ticketRows -> {
            CustomerTicketHistoryProjection first = ticketRows.get(0);
            List<CustomerTicketSeatResponse> seats = ticketRows.stream()
                .map(row -> new CustomerTicketSeatResponse(
                    row.getTicketDetailId(),
                    row.getSeatCode(),
                    row.getSeatPrice()
                ))
                .toList();

            return new CustomerTicketHistoryResponse(
                first.getPassengerTicketId(),
                first.getTicketCode(),
                first.getTicketStatus(),
                first.getTotalPrice(),
                first.getPickupStopName(),
                first.getDropoffStopName(),
                first.getBookedAt(),
                first.getDepartureTime(),
                first.getRouteName(),
                first.getCoachTypeName(),
                first.getPaymentMethod(),
                first.getPaymentStatus(),
                first.getFullName(),
                first.getPhone(),
                first.getEmail(),
                seats
            );
        }).toList();
    }
}
