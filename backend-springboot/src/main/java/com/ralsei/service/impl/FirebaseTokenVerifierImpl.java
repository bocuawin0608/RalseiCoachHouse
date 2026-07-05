package com.ralsei.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.service.FirebaseTokenVerifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseTokenVerifierImpl implements FirebaseTokenVerifier {

    private final Optional<FirebaseAuth> firebaseAuth;

    @Override
    public FirebaseToken verifyIdToken(String idToken) {
        if (firebaseAuth.isEmpty()) {
            throw new BusinessRuleException("Firebase chưa được cấu hình!");
        }
        try {
            return firebaseAuth.get().verifyIdToken(idToken);
        } catch (Exception e) {
            log.error("Firebase token verification failed", e);
            throw new BusinessRuleException("Xác thực Firebase thất bại!");
        }
    }
}
