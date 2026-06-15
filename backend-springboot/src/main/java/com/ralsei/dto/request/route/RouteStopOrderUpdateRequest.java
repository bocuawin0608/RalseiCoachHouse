package com.ralsei.dto.request.route;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteStopOrderUpdateRequest {
    @NotNull(message = "Route Stop ID is required")
    private Integer routeStopId;

    @Min(value = 1, message = "Stop order must be at least 1")
    @Max(value = 2147483647, message = "Stop order must be less than 2147483647")
    private int stopOrder;
}
