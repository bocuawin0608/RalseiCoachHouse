package com.ralsei.service;

import com.google.firebase.auth.FirebaseToken;

/**
 * Provides the firebase token verifier component for the application.
 */
public interface FirebaseTokenVerifier {
    FirebaseToken verifyIdToken(String idToken);
}
