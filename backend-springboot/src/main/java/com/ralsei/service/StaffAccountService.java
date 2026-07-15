package com.ralsei.service;

import com.ralsei.dto.request.staff.StaffPasswordChangeRequest;
import com.ralsei.dto.request.staff.StaffProfileUpdateRequest;
import com.ralsei.dto.response.staff.StaffAccountActionResponse;
import com.ralsei.dto.response.staff.StaffProfileResponse;

/**
 * Internal staff self-service account operations.
 * This boundary keeps profile ownership checks in the backend instead of
 * trusting the browser with staff or account identifiers.
 */
/**
 * Provides the business service contract for staff account.
 */
public interface StaffAccountService {

    /**
     * Loads the profile owned by the current staff JWT principal.
     *
     * @return staff profile for the signed-in account
     */
    StaffProfileResponse getCurrentProfile();

    /**
     * Updates safe editable staff profile fields.
     *
     * @param request sanitized profile data from the internal profile page
     * @return updated profile
     */
    StaffProfileResponse updateCurrentProfile(StaffProfileUpdateRequest request);

    /**
     * Changes the current local staff password and revokes refresh tokens.
     *
     * @param request current and replacement password data
     * @return generic success response
     */
    StaffAccountActionResponse changeCurrentPassword(StaffPasswordChangeRequest request);
}
