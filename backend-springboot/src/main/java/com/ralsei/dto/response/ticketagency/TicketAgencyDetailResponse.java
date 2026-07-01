package com.ralsei.dto.response.ticketagency;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TicketAgencyDetailResponse(
    Integer ticketAgencyId,
    String ticketAgencyName,
    Integer stopPointId,
    String stopPointName,
    @JsonProperty("active") boolean isActive,
    Long staffCount,
    LocalDateTime createdAt,
    Integer createdBy,
    LocalDateTime updatedAt,
    Integer updatedBy
) {}
