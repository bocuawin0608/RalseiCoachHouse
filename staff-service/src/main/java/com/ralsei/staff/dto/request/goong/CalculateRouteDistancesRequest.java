package com.ralsei.staff.dto.request.goong;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the request payload for calculate route distances operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CalculateRouteDistancesRequest {
    @NotNull(message = "Route ID is required")
    @Min(value = 1, message = "Route ID must be greater than 0")
    private Integer routeId;
}
