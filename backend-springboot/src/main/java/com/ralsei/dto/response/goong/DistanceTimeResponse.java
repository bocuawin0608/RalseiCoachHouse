package com.ralsei.dto.response.goong;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistanceTimeResponse {
    private double distanceKm;
    private double durationMinutes;
}
