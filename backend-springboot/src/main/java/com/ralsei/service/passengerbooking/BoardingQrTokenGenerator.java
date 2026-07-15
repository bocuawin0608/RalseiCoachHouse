package com.ralsei.service.passengerbooking;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
/**
 * Provides the boarding qr token generator component for the application.
 */
public class BoardingQrTokenGenerator {

    /**
     * Executes the generate token operation.
     *
     * @return the operation result
     */
    public String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
