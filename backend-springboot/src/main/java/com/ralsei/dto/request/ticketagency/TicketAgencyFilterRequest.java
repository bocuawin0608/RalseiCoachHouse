package com.ralsei.dto.request.ticketagency;

/**
 * TicketAgencyFilterRequest
 */

/**
 * Represents the request payload for ticket agency filter operations.
 */
public record TicketAgencyFilterRequest(
    String search,
    Boolean isActive
) {}
