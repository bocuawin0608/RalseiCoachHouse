package com.ralsei.dto.response.goong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Represents the response payload for distance time operations.
 */
public class DistanceTimeResponse {
    private double distanceKm;
    private double durationMinutes;
}
