package com.ralsei.dto.response.staff;

/**
 * Generic result for staff account self-service commands.
 *
 * @param success whether the command completed
 * @param message customer-facing or staff-facing confirmation text
 */
/**
 * Represents the response payload for staff account action operations.
 */
/**
 * Executes the staff account action response operation.
 *
 * @param success the value supplied for this operation
 * @param message the value supplied for this operation
 *
 * @return the operation result
 */
public record StaffAccountActionResponse(boolean success, String message) {}
