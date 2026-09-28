package com.ralsei.customer.service.passengerbooking;

import com.ralsei.customer.dto.response.passengerbooking.CheckPhoneResponse;

/**
 * Provides the business service contract for passenger phone verification.
 */
public interface PassengerPhoneVerificationService {
    CheckPhoneResponse checkPhone(String phone);

    boolean isPhoneKnown(String phone);

    void verifyFirebasePhoneToken(String phone, String idToken);
}
