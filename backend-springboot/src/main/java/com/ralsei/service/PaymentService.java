package com.ralsei.service;

import com.ralsei.dto.request.payment.PaymentCheckoutRequest;
import com.ralsei.dto.request.sePay.SepayWebhookRequest;
import com.ralsei.model.Payment;

public interface PaymentService {

    public Payment initializePayment(PaymentCheckoutRequest request);

    public void processWebhook(SepayWebhookRequest request);

    public Payment getPaymentByTransactionId(String transactionId);

    /**
     * Chỉ đổi payment PENDING → FAILED (idempotent).
     * @return true nếu vừa fail thành công, false nếu payment không còn PENDING
     */
    boolean failPendingPayment(String transactionId);
}
