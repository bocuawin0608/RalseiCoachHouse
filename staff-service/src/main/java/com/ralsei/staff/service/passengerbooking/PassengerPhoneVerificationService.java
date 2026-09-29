package com.ralsei.staff.service.passengerbooking;

import com.ralsei.staff.dto.response.passengerbooking.CheckPhoneResponse;

/**
 * Provides the business service contract for passenger phone verification.
 */
public interface PassengerPhoneVerificationService {
    CheckPhoneResponse checkPhone(String phone);

    boolean isPhoneKnown(String phone);

    void verifyFirebasePhoneToken(String phone, String idToken);
}
