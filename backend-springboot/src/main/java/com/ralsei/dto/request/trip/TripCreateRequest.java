package com.ralsei.dto.request.trip;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Data
@Getter
@Setter
/**
 * Represents the request payload for trip create operations.
 */
public class TripCreateRequest {
    private Integer routeId;
    private Integer coachId;
    private LocalDateTime departureTime;
     private String status;
    private Integer driverId;
    private Integer attendantId;
}
