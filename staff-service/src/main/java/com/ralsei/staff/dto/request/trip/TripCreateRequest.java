package com.ralsei.staff.dto.request.trip;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
/**
 * Represents the request payload for trip create operations.
 */
@Data
@Getter
@Setter

public class TripCreateRequest {
    private Integer routeId;
    private Integer coachId;
    private LocalDateTime departureTime;
     private String status;
    private Integer driverId;
    private Integer attendantId;
}
