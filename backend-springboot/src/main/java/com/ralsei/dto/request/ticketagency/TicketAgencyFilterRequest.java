package com.ralsei.dto.request.ticketagency;

public record TicketAgencyFilterRequest(
    String search,
    Boolean isActive
) {}
