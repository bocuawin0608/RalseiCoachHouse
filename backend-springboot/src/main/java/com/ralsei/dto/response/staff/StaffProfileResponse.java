package com.ralsei.dto.response.staff;

import java.time.LocalDate;
import java.util.List;

/**
 * Staff account profile displayed on the internal staff website.
 * Account and role fields are read-only so the frontend can render context
 * without allowing privilege or operational assignment edits.
 */
/**
 * Represents the response payload for staff profile operations.
 */
public record StaffProfileResponse(
    Integer staffId,
    Integer accountId,
    String username,
    String staffName,
    String phone,
    String email,
    LocalDate dob,
    String staffPosition,
    LocalDate hireDate,
    Integer ticketAgencyId,
    List<String> roles
) {}
