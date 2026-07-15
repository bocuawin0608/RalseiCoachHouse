package com.ralsei.service.passengerbooking.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.auth.FirebaseToken;
import com.ralsei.dto.projection.passengerbooking.PassengerProfileProjection;
import com.ralsei.dto.response.passengerbooking.CheckPhoneResponse;
import com.ralsei.dto.response.passengerbooking.SuggestedPassengerProfile;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.repository.AccountRepository;
import com.ralsei.repository.CustomerRepository;
import com.ralsei.repository.PassengerTicketDetailRepository;
import com.ralsei.service.FirebaseTokenVerifier;
import com.ralsei.service.passengerbooking.PassengerPhoneVerificationService;
import com.ralsei.util.PhoneNumberUtility;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/**
 * Provides the passenger phone verification service impl component for the application.
 */
public class PassengerPhoneVerificationServiceImpl implements PassengerPhoneVerificationService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final PassengerTicketDetailRepository ticketDetailRepository;
    private final FirebaseTokenVerifier firebaseTokenVerifier;

    @Override
    @Transactional(readOnly = true)
    /**
     * Executes the check phone operation.
     *
     * @param phone the value supplied for this operation
     *
     * @return the operation result
     */
    public CheckPhoneResponse checkPhone(String phone) {
        String normalizedPhone = PhoneNumberUtility.normalizeToLocalFormat(phone);
        boolean knownFromAccount = accountRepository.existsByUsername(normalizedPhone);
        boolean knownFromCustomer = customerRepository.existsByPhone(normalizedPhone);
        boolean knownFromTicketHistory = ticketDetailRepository.existsConfirmedPaidByPhone(normalizedPhone);

        boolean isKnown = knownFromAccount || knownFromCustomer || knownFromTicketHistory;

        SuggestedPassengerProfile suggestedProfile = null;
        if (knownFromTicketHistory && !knownFromAccount) {
            suggestedProfile = findLatestTicketProfile(normalizedPhone);
        }

        return new CheckPhoneResponse(isKnown, suggestedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * Returns whether the phone known is active.
     *
     * @param phone the value supplied for this operation
     *
     * @return {@code true} if the phone known is active; otherwise {@code false}
     */
    public boolean isPhoneKnown(String phone) {
        String normalizedPhone = PhoneNumberUtility.normalizeToLocalFormat(phone);
        return accountRepository.existsByUsername(normalizedPhone)
                || customerRepository.existsByPhone(normalizedPhone)
                || ticketDetailRepository.existsConfirmedPaidByPhone(normalizedPhone);
    }

    @Override
    /**
     * Executes the verify firebase phone token operation.
     *
     * @param phone the value supplied for this operation
     * @param idToken the value supplied for this operation
     */
    public void verifyFirebasePhoneToken(String phone, String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new BusinessRuleException("Số điện thoại chưa được xác thực OTP!");
        }

        FirebaseToken firebaseToken = firebaseTokenVerifier.verifyIdToken(idToken);
        String phoneFromClaims = (String) firebaseToken.getClaims().get("phone_number");
        if (!PhoneNumberUtility.matchesLocalPhone(phone, phoneFromClaims)) {
            throw new BusinessRuleException("Mã OTP không khớp với số điện thoại đã nhập!");
        }
    }

    private SuggestedPassengerProfile findLatestTicketProfile(String phone) {
        List<PassengerProfileProjection> profiles = ticketDetailRepository
                .findLatestConfirmedProfilesByPhone(phone, PageRequest.of(0, 1));

        if (profiles.isEmpty()) {
            return null;
        }

        PassengerProfileProjection profile = profiles.get(0);
        return new SuggestedPassengerProfile(profile.getFullName(), profile.getEmail());
    }
}
