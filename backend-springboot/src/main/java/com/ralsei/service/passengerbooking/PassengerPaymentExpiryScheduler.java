package com.ralsei.service.passengerbooking;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ralsei.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * Provides the passenger payment expiry scheduler component for the application.
 */
public class PassengerPaymentExpiryScheduler {

    private static final int BATCH_SIZE = 200;

    private final PaymentRepository paymentRepository;
    private final PassengerPendingPaymentService passengerPendingPaymentService;

    @Scheduled(fixedDelayString = "${booking.payment-expiry-scan-delay-ms:30000}")
    /**
     * Executes the expire overdue pending passenger payments operation.
     */
    public void expireOverduePendingPassengerPayments() {
        List<String> transactionIds = paymentRepository.findOverduePendingPassengerTransactionIds(
                LocalDateTime.now(),
                PageRequest.of(0, BATCH_SIZE));

        for (String transactionId : transactionIds) {
            try {
                passengerPendingPaymentService.expireIfOverdue(transactionId);
            } catch (RuntimeException ex) {
                log.warn("Failed to expire overdue payment transactionId={}: {}", transactionId, ex.getMessage());
            }
        }
    }
}
