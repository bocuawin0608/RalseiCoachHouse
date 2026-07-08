package com.ralsei.dto.response.staff;

/**
 * Generic result for staff account self-service commands.
 *
 * @param success whether the command completed
 * @param message customer-facing or staff-facing confirmation text
 */
public record StaffAccountActionResponse(boolean success, String message) {}
