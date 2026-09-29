package com.ralsei.staff.dto.response.goong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the response payload for distance time operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class DistanceTimeResponse {
    private double distanceKm;
    private double durationMinutes;
}
