package com.ralsei.service.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.ralsei.repository.PaymentRepository;
import com.ralsei.service.TransactionIdGenerator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/**
 * Provides the se pay transaction id generator impl component for the application.
 */
public class SePayTransactionIdGeneratorImpl implements TransactionIdGenerator {

    private final PaymentRepository paymentRepository;

    @Override
    /**
     * Executes the generate unique transaction id operation.
     *
     * @return the operation result
     */
    public String generateUniqueTransactionId() {
        String transactionId;
        do {
            transactionId = "PAY" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (paymentRepository.existsByTransactionId(transactionId));
        return transactionId;
    }
}
