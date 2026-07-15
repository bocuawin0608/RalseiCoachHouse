package com.ralsei.service.passengerbooking.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.exception.BusinessRuleException;
import com.ralsei.model.PassengerTicket;
import com.ralsei.model.Payment;
import com.ralsei.model.enums.PassengerPendingPaymentOutcome;
import com.ralsei.model.enums.PassengerTicketDetailStatus;
import com.ralsei.model.enums.PassengerTicketStatus;
import com.ralsei.model.enums.TripSeatStatus;
import com.ralsei.repository.CustomerRepository;
import com.ralsei.repository.PassengerTicketDetailRepository;
import com.ralsei.repository.PassengerTicketRepository;
import com.ralsei.repository.PaymentRepository;
import com.ralsei.repository.TripSeatRepository;
import com.ralsei.service.JwtService;
import com.ralsei.service.PaymentService;
import com.ralsei.service.VoucherService;
import com.ralsei.service.passengerbooking.PassengerPendingPaymentService;
import com.ralsei.service.passengerbooking.SeatHoldService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * Provides the passenger pending payment service impl component for the application.
 */
public class PassengerPendingPaymentServiceImpl implements PassengerPendingPaymentService {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final PassengerTicketRepository ticketRepository;
    private final PassengerTicketDetailRepository ticketDetailRepository;
    private final TripSeatRepository tripSeatRepository;
    private final SeatHoldService seatHoldService;
    private final VoucherService voucherService;
    private final JwtService jwtService;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    /**
     * Executes the expire if overdue operation.
     *
     * @param transactionId the value supplied for this operation
     */
    public void expireIfOverdue(String transactionId) {
        try {
            Payment payment = paymentService.getPaymentByTransactionId(transactionId);
            if (!"PENDING".equals(payment.getStatus()) || payment.getPassengerTicketId() == null) {
                return;
            }

            ticketDetailRepository.findByPassengerTicketId(payment.getPassengerTicketId())
                    .stream()
                    .findFirst()
                    .ifPresent(detail -> {
                        if (detail.getExpiredAt() != null && LocalDateTime.now().isAfter(detail.getExpiredAt())) {
                            abortPendingPassengerPayment(payment, PassengerPendingPaymentOutcome.EXPIRED);
                        }
                    });
        } catch (IllegalArgumentException ex) {
            log.warn("Không thể expire transaction: {}", transactionId);
        }
    }

    @Override
    @Transactional
    /**
     * Cancels the by user.
     *
     * @param transactionId the value supplied for this operation
     */
    public void cancelByUser(String transactionId) {
        Payment payment = paymentService.getPaymentByTransactionId(transactionId);
        if (payment.getPassengerTicketId() == null) {
            throw new BusinessRuleException("Không phải thanh toán vé hành khách!");
        }
        if (!"PENDING".equals(payment.getStatus())) {
            throw new BusinessRuleException("Giao dịch không còn ở trạng thái chờ thanh toán!");
        }

        abortPendingPassengerPayment(payment, PassengerPendingPaymentOutcome.USER_CANCELLED);
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * Executes the can cancel by user operation.
     *
     * @param transactionId the value supplied for this operation
     * @param cancelToken the value supplied for this operation
     * @param accessToken the value supplied for this operation
     *
     * @return the operation result
     */
    public boolean canCancelByUser(String transactionId, String cancelToken, String accessToken) {
        if (cancelToken != null && paymentRepository.isValidCancelToken(transactionId, cancelToken)) {
            return true;
        }

        Integer customerId = resolveCustomerId(accessToken);
        if (customerId != null) {
            return ticketRepository.existsPendingPaymentByTransactionIdAndCustomerId(transactionId, customerId);
        }

        return false;
    }

    private void abortPendingPassengerPayment(Payment payment, PassengerPendingPaymentOutcome outcome) {
        boolean failed = paymentService.failPendingPayment(payment.getTransactionId());
        if (!failed) {
            return;
        }

        Integer ticketId = payment.getPassengerTicketId();

        ticketRepository.updateStatusIfCurrent(
                ticketId,
                PassengerTicketStatus.PENDING,
                PassengerTicketStatus.CANCELLED);

        String detailStatus = outcome == PassengerPendingPaymentOutcome.USER_CANCELLED
                ? PassengerTicketDetailStatus.CANCELLED.name()
                : PassengerTicketDetailStatus.EXPIRED.name();

        ticketDetailRepository.updateStatusByPassengerTicketId(ticketId, detailStatus);

        List<Integer> tripSeatIds = ticketDetailRepository.findTripSeatIdsByPassengerTicketId(ticketId);
        if (!tripSeatIds.isEmpty()) {
            tripSeatRepository.updateStatusByTripSeatIds(tripSeatIds, TripSeatStatus.AVAILABLE);
            seatHoldService.forceReleaseSeatsByIds(tripSeatIds);
        }

        ticketRepository.findById(ticketId).ifPresent(this::releaseVoucherUsageIfApplied);
    }

    private void releaseVoucherUsageIfApplied(PassengerTicket ticket) {
        if (ticket.getVoucherId() == null || ticket.getVoucherCodeSnapshot() == null) {
            return;
        }
        int rowsUpdated = voucherService.decrementUsedCountIfApplied(ticket.getVoucherId());
        if (rowsUpdated == 0) {
            log.warn("Không thể hoàn lượt voucher id={} khi hủy vé {}", ticket.getVoucherId(), ticket.getTicketCode());
        }
    }

    private Integer resolveCustomerId(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }
        Integer accountId = jwtService.extractAccountId(accessToken);
        if (accountId == null) {
            return null;
        }
        return customerRepository.findByAccountId(accountId)
                .map(customer -> customer.getCustomerId())
                .orElse(null);
    }
}
