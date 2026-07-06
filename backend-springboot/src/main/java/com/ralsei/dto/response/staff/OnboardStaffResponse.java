package com.ralsei.dto.response.staff;

/**
 * OnboardStaffResponse
 */

public record OnboardStaffResponse(
    Integer staffId,
    Integer accountId,
    String username
) {}
