package com.ralsei.staff.service;

import com.google.firebase.auth.FirebaseToken;

public interface FirebaseTokenVerifier {
    FirebaseToken verifyIdToken(String idToken);
}
