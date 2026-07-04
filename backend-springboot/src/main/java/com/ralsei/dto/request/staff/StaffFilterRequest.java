package com.ralsei.dto.request.staff;

public record StaffFilterRequest(
    String search,
    Boolean isActive,
    String staffPosition,
    Integer ticketAgencyId
) {}
