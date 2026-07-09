package com.ralsei.service;

import com.ralsei.dto.request.customer.CustomerProfileUpdateRequest;
import com.ralsei.dto.response.customer.CustomerAccountActionResponse;
import com.ralsei.dto.response.customer.CustomerProfileResponse;

/**
 * Customer website self-service account operations.
 * This boundary keeps profile ownership checks in the backend instead of
 * trusting the browser with account identifiers.
 */
public interface CustomerAccountService {

    /**
     * Loads the profile owned by the current customer JWT principal.
     *
     * @return customer profile for the signed-in account
     */
    CustomerProfileResponse getCurrentProfile();

    /**
     * Updates safe editable customer profile fields.
     *
     * @param request sanitized profile data from the customer page
     * @return updated profile
     */
    CustomerProfileResponse updateCurrentProfile(CustomerProfileUpdateRequest request);

    /**
     * Soft-deactivates the current customer account and revokes refresh tokens.
     *
     * @return generic success response
     */
    CustomerAccountActionResponse deactivateCurrentAccount();
}
