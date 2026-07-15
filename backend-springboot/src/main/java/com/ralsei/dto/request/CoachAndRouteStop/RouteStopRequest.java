package com.ralsei.dto.request.CoachAndRouteStop;

import com.ralsei.util.validation.ValidRouteStopMetrics;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidRouteStopMetrics
/**
 * Represents the request payload for route stop operations.
 */
public class RouteStopRequest {
    @NotNull(message = "Route ID is required")
    @Min(value = 1, message = "Route ID must be at least 1")
    @Max(value = 2147483647, message = "Route ID must be less than 2147483647")
    private int routeId;

    @NotNull(message = "Coach stop ID is required")
    @Min(value = 1, message = "Coach stop ID must be at least 1")
    @Max(value = 2147483647, message = "Coach stop ID must be less than 2147483647")
    private int stopPointId;

    @Min(value = 1, message = "Stop order must be at least 1")
    @Max(value = 2147483647, message = "Stop order must be less than 2147483647")
    private int stopOrder;

    @NotNull(message = "Kilometers from start is required")
    @Min(value = 0, message = "Kilometers from start cannot be negative")
    @Max(value = 2147483647, message = "Kilometers from start must be less than 2147483647")
    private BigDecimal kilometersFromStart;

    @Min(value = 0, message = "Minutes from start cannot be negative")
    @Max(value = 2147483647, message = "Minutes from start must be less than 2147483647")
    private int minutesFromStart;
}
