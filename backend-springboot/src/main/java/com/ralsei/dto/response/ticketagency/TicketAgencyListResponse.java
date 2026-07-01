package com.ralsei.dto.response.ticketagency;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TicketAgencyListResponse(
    Integer ticketAgencyId,
    String ticketAgencyName,
    Integer stopPointId,
    String stopPointName,
    @JsonProperty("active") boolean isActive,
    Long staffCount,
    LocalDateTime createdAt
) {}
