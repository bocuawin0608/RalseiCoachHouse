package com.ralsei.payment.controller;

import com.ralsei.payment.dto.PaymentRequest;
import com.ralsei.payment.dto.PaymentResponse;
import com.ralsei.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/vnpay/create")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createVNPayPayment(request);
        return ResponseEntity.ok(response);
    }
}
