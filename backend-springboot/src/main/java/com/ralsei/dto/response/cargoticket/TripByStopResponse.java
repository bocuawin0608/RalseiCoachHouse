package com.ralsei.dto.response.cargoticket;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripByStopResponse {
    private int tripId;
    private int routeId;
    private int coachId;
    private String coachTypeName;
    private LocalDateTime departureTime;
    private String status;
}
