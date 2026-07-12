package com.ralsei.dto.response.ticketagency;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TicketAgencyListResponse
 */

public record TicketAgencyListResponse(
    Integer ticketAgencyId,
    String ticketAgencyName,
    Integer stopPointId,
    String stopPointName,
    String city,
    @JsonProperty("active") boolean isActive,
    Long staffCount,
    LocalDateTime createdAt
) {}
