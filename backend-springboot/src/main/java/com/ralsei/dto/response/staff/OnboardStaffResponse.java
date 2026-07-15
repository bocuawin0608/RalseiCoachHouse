package com.ralsei.dto.response.staff;

/**
 * OnboardStaffResponse
 */

/**
 * Represents the response payload for onboard staff operations.
 */
public record OnboardStaffResponse(
    Integer staffId,
    Integer accountId,
    String username
) {}
