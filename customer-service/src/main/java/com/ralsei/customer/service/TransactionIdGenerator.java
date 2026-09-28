package com.ralsei.customer.service;

/**
 * Provides the transaction id generator component for the application.
 */
public interface TransactionIdGenerator {
    String generateUniqueTransactionId();
}
