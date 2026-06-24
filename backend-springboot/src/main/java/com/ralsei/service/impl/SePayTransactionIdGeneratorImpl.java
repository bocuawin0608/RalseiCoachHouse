package com.ralsei.service.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.ralsei.repository.PaymentRepository;
import com.ralsei.service.TransactionIdGenerator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SePayTransactionIdGeneratorImpl implements TransactionIdGenerator {

    private final PaymentRepository paymentRepository;

    @Override
    public String generateUniqueTransactionId() {
        String transactionId;
        do {
            transactionId = "PAY" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (paymentRepository.existsByTransactionId(transactionId));
        return transactionId;
    }
}
