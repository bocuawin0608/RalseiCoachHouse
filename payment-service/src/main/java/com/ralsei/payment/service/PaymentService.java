package com.ralsei.payment.service;

import com.ralsei.payment.dto.PaymentRequest;
import com.ralsei.payment.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse createVNPayPayment(PaymentRequest request);
}
