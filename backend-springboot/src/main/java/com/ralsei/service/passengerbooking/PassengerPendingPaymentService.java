package com.ralsei.service.passengerbooking;

/**
 * Provides the business service contract for passenger pending payment.
 */
public interface PassengerPendingPaymentService {

    void expireIfOverdue(String transactionId);

    void cancelByUser(String transactionId);

    boolean canCancelByUser(String transactionId, String cancelToken, String accessToken);
}
