package com.ralsei.service.passengerbooking;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface PaymentSseService {
    SseEmitter createConnection(String transactionId);
    void sendStatusUpdate(String transactionId, String status);
}
