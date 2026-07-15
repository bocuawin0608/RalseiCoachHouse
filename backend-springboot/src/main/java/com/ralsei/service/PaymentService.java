package com.ralsei.service;

import com.ralsei.dto.request.payment.PaymentCheckoutRequest;
import com.ralsei.dto.request.sePay.SepayWebhookRequest;
import com.ralsei.model.Payment;

/**
 * Provides the business service contract for payment.
 */
public interface PaymentService {

    /**
     * Executes the initialize payment operation.
     *
     * @param request the value supplied for this operation
     *
     * @return the operation result
     */
    public Payment initializePayment(PaymentCheckoutRequest request);

    /**
     * Executes the process webhook operation.
     *
     * @param request the value supplied for this operation
     */
    public void processWebhook(SepayWebhookRequest request);

    /**
     * Returns the payment by transaction id.
     *
     * @param transactionId the value supplied for this operation
     *
     * @return the payment by transaction id
     */
    public Payment getPaymentByTransactionId(String transactionId);

    /**
     * Chỉ đổi payment PENDING → FAILED (idempotent).
     * @return true nếu vừa fail thành công, false nếu payment không còn PENDING
     */
    boolean failPendingPayment(String transactionId);
}
