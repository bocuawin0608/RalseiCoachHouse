package com.ralsei.service.passengerbooking.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.ralsei.service.passengerbooking.PaymentSseService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentSseServiceImpl implements PaymentSseService {
    
    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter createConnection(Integer transactionId) {
        SseEmitter emitter = new SseEmitter(15 * 60 * 1000L); 
        emitters.put(transactionId, emitter);

        emitter.onCompletion(() -> emitters.remove(transactionId));
        emitter.onTimeout(() -> emitters.remove(transactionId));
        emitter.onError((e) -> emitters.remove(transactionId));

        try {
            emitter.send(SseEmitter.event().name("INIT").data("OK"));
        } catch (IOException e) {
            emitters.remove(transactionId);
        }
        return emitter;
    }

    @Override
    public void sendStatusUpdate(Integer transactionId, String status) {
        SseEmitter emitter = emitters.get(transactionId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("PAYMENT_STATUS").data(status));
                if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
                    emitter.complete();
                    emitters.remove(transactionId);
                }
            } catch (IOException e) {
                log.warn("SSE push failed for {}: {}", transactionId, e.getMessage());
                emitters.remove(transactionId);
                emitter.completeWithError(e);
            }
        }
    }
}
