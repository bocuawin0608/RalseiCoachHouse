package com.ralsei.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.dto.request.payment.PaymentCheckoutRequest;
import com.ralsei.dto.request.sePay.SepayWebhookRequest;
import com.ralsei.model.Payment;
import com.ralsei.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${sepay.api-token}")
    private String sepayApiToken;

    @PostMapping("/checkout")
    public ResponseEntity<?> initializePayment(@RequestBody PaymentCheckoutRequest request) {
        try {
            Payment payment = paymentService.initializePayment(request);
            return ResponseEntity.ok(Map.of(
                    "transactionId", payment.getTransactionId(),
                    "amount", payment.getAmount()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/sepay-ipn")
    public ResponseEntity<?> handleSepayWebhook(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody SepayWebhookRequest request) {

        if (authHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
        }

        String token = authHeader.replace("Bearer ", "").replace("Apikey ", "").trim();
        if (!sepayApiToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid API token");
        }

        try {
            paymentService.processWebhook(request);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
