package com.ralsei.dto.response.ticketagency;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the response payload for ticket agency detail operations.
 */
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
    /**
     * Provides the staff summary component for the application.
     */
    /**
     * Executes the staff summary operation.
     *
     * @param staffId the value supplied for this operation
     * @param staffName the value supplied for this operation
     * @param staffPosition the value supplied for this operation
     *
     * @return the operation result
     */
    public record StaffSummary(Integer staffId, String staffName, String staffPosition) {}
}
