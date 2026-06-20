package com.ralsei.service;

import com.ralsei.dto.request.payment.PaymentCheckoutRequest;
import com.ralsei.dto.request.sePay.SepayWebhookRequest;
import com.ralsei.model.Payment;

public interface PaymentService {

    public Payment initializePayment(PaymentCheckoutRequest request);

    public void processWebhook(SepayWebhookRequest request);
}
