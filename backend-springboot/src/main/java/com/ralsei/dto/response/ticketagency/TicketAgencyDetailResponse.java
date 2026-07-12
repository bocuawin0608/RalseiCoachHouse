package com.ralsei.dto.response.ticketagency;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TicketAgencyDetailResponse(
    Integer ticketAgencyId,
    String ticketAgencyName,
    Integer stopPointId,
    String stopPointName,
    String city,
    String address,
    @JsonProperty("active") boolean isActive,
    Long staffCount,
    List<StaffSummary> staffList,
    LocalDateTime createdAt,
    Integer createdBy,
    LocalDateTime updatedAt,
    Integer updatedBy
) {
    public record StaffSummary(Integer staffId, String staffName, String staffPosition) {}
}
