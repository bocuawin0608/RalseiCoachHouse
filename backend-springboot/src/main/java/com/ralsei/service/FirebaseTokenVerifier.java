package com.ralsei.service;

import com.google.firebase.auth.FirebaseToken;

public interface FirebaseTokenVerifier {
    FirebaseToken verifyIdToken(String idToken);
}
