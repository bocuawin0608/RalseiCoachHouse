package com.ralsei.dto.request.ticketagency;

/**
 * TicketAgencyFilterRequest
 */

public record TicketAgencyFilterRequest(
    String search,
    Boolean isActive
) {}
