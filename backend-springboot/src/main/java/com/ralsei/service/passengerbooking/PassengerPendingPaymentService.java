package com.ralsei.service.passengerbooking;

public interface PassengerPendingPaymentService {

    void expireIfOverdue(String transactionId);

    void cancelByUser(String transactionId);

    boolean canCancelByUser(String transactionId, String cancelToken, String accessToken);
}
