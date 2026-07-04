package com.ralsei.service.passengerbooking.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.ralsei.service.passengerbooking.PaymentSseService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentSseServiceImpl implements PaymentSseService {

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter createConnection(String transactionId) {
        SseEmitter emitter = new SseEmitter(15 * 60 * 1000L);

        List<SseEmitter> list = emitters.computeIfAbsent(transactionId, k -> new CopyOnWriteArrayList<>());
        list.add(emitter);

        Runnable remove = () -> {
            List<SseEmitter> current = emitters.get(transactionId);
            if (current != null) {
                current.remove(emitter);
                if (current.isEmpty()) {
                    emitters.remove(transactionId, current);
                }
            }
        };

        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(e -> remove.run());

        try {
            emitter.send(SseEmitter.event().name("INIT").data("OK"));
        } catch (IOException e) {
            remove.run();
        }

        return emitter;
    }

    @Override
    public void sendStatusUpdate(String transactionId, String status) {
        List<SseEmitter> list = emitters.getOrDefault(transactionId, List.of());
        boolean terminal = "COMPLETED".equals(status) || "FAILED".equals(status);

        List<SseEmitter> toRemove = new ArrayList<>();
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().name("PAYMENT_STATUS").data(status));
                if (terminal) {
                    emitter.complete();
                    toRemove.add(emitter);
                }
            } catch (IOException e) {
                log.warn("SSE push failed for {}: {}", transactionId, e.getMessage());
                emitter.completeWithError(e);
                toRemove.add(emitter);
            }
        }

        if (!toRemove.isEmpty()) {
            list.removeAll(toRemove);
            if (list.isEmpty()) {
                emitters.remove(transactionId);
            }
        }
    }
}
