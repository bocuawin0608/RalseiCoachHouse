package com.ralsei.dto.response.CoachAndRouteStop;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteResponse {
    private int routeId;
    private String routeName;
    private BigDecimal totalKilometers;
    private int totalMinutes;
    private boolean isActive;
    private LocalDateTime createdAt;
    private Integer createdBy;
    private LocalDateTime updatedAt;
    private Integer updatedBy;
    private List<RouteStopResponse> routeStops;
}
