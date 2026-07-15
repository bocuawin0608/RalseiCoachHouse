package com.ralsei.dto.request.goong;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Represents the request payload for distance time operations.
 */
public class DistanceTimeRequest {
    @NotNull(message = "Origin latitude is required")
    private Double originLat;

    @NotNull(message = "Origin longitude is required")
    private Double originLng;

    @NotNull(message = "Destination latitude is required")
    private Double destinationLat;

    @NotNull(message = "Destination longitude is required")
    private Double destinationLng;

    @Builder.Default
    private String vehicle = "car";
}
