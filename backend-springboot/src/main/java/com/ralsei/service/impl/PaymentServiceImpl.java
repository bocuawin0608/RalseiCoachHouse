package com.ralsei.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Payment initializePayment(PaymentCheckoutRequest request) {
        // format SePay is PAYxxxxxx
        String transactionId = "PAY" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Payment payment = Payment.builder()
                .passengerTicketId(request.getPassengerTicketId())
                .cargoTicketId(request.getCargoTicketId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "bank_transfer")
                .transactionId(transactionId)
                .status("pending")
                .refundAmount(BigDecimal.ZERO)
                .isActive(true)
                .build();

        return paymentRepository.save(payment);
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

        Optional<Payment> paymentOpt = paymentRepository.findByTransactionIdAndStatus(transactionId, "pending");
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();

            // Verify transfer amount
            if (payment.getAmount().compareTo(request.getTransferAmount()) <= 0) {
                payment.setStatus("completed");
                payment.setPaymentTime(LocalDateTime.now());

                try {
                    payment.setCallbackData(objectMapper.writeValueAsString(request));
                } catch (JsonProcessingException e) {
                    payment.setCallbackData(request.toString());
                }

                paymentRepository.save(payment);
            } else {
                throw new IllegalArgumentException("Transfer amount is less than required payment amount");
            }
        } else {
            throw new IllegalArgumentException(
                    "Payment not found or already processed for transactionId: " + transactionId);
        }
    }
}
