package com.ralsei.service.passengerbooking;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface PaymentSseService {
    SseEmitter createConnection(Integer transactionId);
    void sendStatusUpdate(Integer transactionId, String status);
}
