package com.ralsei.dto.request.staff;

/**
 * StaffFilterRequest
 */

public record StaffFilterRequest(
    String search,
    Boolean isActive,
    String staffPosition,
    Integer ticketAgencyId
) {}
