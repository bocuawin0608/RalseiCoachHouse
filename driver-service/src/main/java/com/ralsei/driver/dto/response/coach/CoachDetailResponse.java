package com.ralsei.driver.dto.response.coach;

import java.util.List;

import com.ralsei.driver.model.CoachStatus;

/**
 * Represents the response payload for coach detail operations.
 */
public record CoachDetailResponse(
    Integer coachId,
    Integer routeId,
    String routeName,
    Integer coachTypeId,
    String coachTypeName,
    String licensePlate,
    String manufacturer,
    Integer year,
    CoachStatus status,
    Integer totalActiveSeats,
    List<SeatDTO> seats,
    CoachStatusLogResponse latestStatusLog,
    boolean canReportMaintenance,
    boolean canReactivate,
    boolean canRetire
) {}
