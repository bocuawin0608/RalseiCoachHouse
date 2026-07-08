package com.ralsei.dto.response.auth;

/**
 * Generic response for staff forgot-password requests.
 * The message intentionally does not reveal whether the submitted account
 * exists, which keeps the login page from becoming an account-enumeration API.
 *
 * @param success request was accepted for processing
 * @param message generic user-facing outcome
 */
public record StaffForgotPasswordResponse(
    boolean success,
    String message
) {}
