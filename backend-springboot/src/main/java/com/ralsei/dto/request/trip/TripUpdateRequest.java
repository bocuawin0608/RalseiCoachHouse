package com.ralsei.dto.request.trip;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class TripUpdateRequest {
    private int driverId;
    private Integer coachId;
    private Integer attendantId;
    private LocalDateTime departureTime;
}
