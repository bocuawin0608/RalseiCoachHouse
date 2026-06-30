package com.ralsei.service.passengerbooking;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BoardingQrTokenGenerator {

    public String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
