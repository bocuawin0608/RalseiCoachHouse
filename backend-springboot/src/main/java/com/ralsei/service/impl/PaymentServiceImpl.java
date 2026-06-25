package com.ralsei.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ralsei.dto.request.payment.PaymentCheckoutRequest;
import com.ralsei.dto.request.sePay.SepayWebhookRequest;
import com.ralsei.model.Payment;
import com.ralsei.repository.PaymentRepository;
import com.ralsei.service.PaymentService;
import com.ralsei.service.TransactionIdGenerator;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final TransactionIdGenerator transactionIdGenerator;
    private final SimpMessagingTemplate messagingTemplate;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public Payment initializePayment(PaymentCheckoutRequest request) {
        String transactionId = transactionIdGenerator.generateUniqueTransactionId();

        Payment payment = Payment.builder()
                .passengerTicketId(request.getPassengerTicketId())
                .cargoTicketId(request.getCargoTicketId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "bank_transfer")
                .transactionId(transactionId)
                .status("PENDING")
                .refundAmount(BigDecimal.ZERO)
                .isActive(true)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        if ("PENDING".equals(savedPayment.getStatus())) {
            // Schedule auto-cancellation after 5 minutes
            scheduler.schedule(() -> {
                try {
                    cancelPayment(transactionId);
                } catch (Exception e) {
                    // Ignore if already deleted or not found
                }
            }, 5, TimeUnit.MINUTES);
        }

        return savedPayment;
    }

    @Override
    public void processWebhook(SepayWebhookRequest request) {
        String content = request.getContent();
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Webhook content is empty");
        }

        // Extract transactionId from content (e.g., PAYxxxxxx)
        Pattern pattern = Pattern.compile("(PAY[A-Z0-9]{6})");
        Matcher matcher = pattern.matcher(content);

        String transactionId = null;
        if (matcher.find()) {
            transactionId = matcher.group(1);
        }

        if (transactionId == null) {
            throw new IllegalArgumentException("Could not extract transactionId from content");
        }

        Optional<Payment> paymentOpt = paymentRepository.findByTransactionIdAndStatus(transactionId, "PENDING");
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();

            // Verify transfer amount
            if (payment.getAmount().compareTo(request.getTransferAmount()) <= 0) {
                payment.setStatus("COMPLETED");
                payment.setPaymentTime(LocalDateTime.now());

                try {
                    payment.setCallbackData(objectMapper.writeValueAsString(request));
                } catch (JsonProcessingException e) {
                    payment.setCallbackData(request.toString());
                }

                Payment savedPayment = paymentRepository.save(payment);

                // Broadcast payment completion to frontend
                messagingTemplate.convertAndSend("/topic/payment/" + transactionId, savedPayment);

            } else {
                throw new IllegalArgumentException("Transfer amount is less than required payment amount");
            }
        } else {
            throw new IllegalArgumentException(
                    "Payment not found or already processed for transactionId: " + transactionId);
        }
    }

    @Override
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Payment not found for transactionId: " + transactionId));
    }

    @Override
    public void softDeletePayment(int paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with id: " + paymentId));
        payment.setActive(false);
        paymentRepository.save(payment);
    }

    @Override
    public void restorePayment(int paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with id: " + paymentId));
        payment.setActive(true);
        paymentRepository.save(payment);
    }

    @Override
    public void cancelPayment(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Payment not found with transactionId: " + transactionId));
        if (!"COMPLETED".equals(payment.getStatus())) {
            payment.setStatus("FAILED");
            Payment savedPayment = paymentRepository.save(payment);
            messagingTemplate.convertAndSend("/topic/payment/" + transactionId, savedPayment);
        }
    }
}
