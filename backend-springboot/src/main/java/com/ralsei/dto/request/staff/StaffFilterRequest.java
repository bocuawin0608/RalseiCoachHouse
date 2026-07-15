package com.ralsei.dto.request.staff;

/**
 * StaffFilterRequest
 */

/**
 * Represents the request payload for staff filter operations.
 */
public record StaffFilterRequest(
    String search,
    Boolean isActive,
    String staffPosition,
    Integer ticketAgencyId
) {}
