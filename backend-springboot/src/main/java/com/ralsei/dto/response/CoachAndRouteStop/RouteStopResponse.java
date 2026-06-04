package com.ralsei.dto.response.CoachAndRouteStop;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteStopResponse {
    private int routeStopId;
    private int routeId;
    private String routeName;
    private int stopPointId;
    private String stopPointName;
    private String address;
    private int stopOrder;
    private BigDecimal kilometersFromStart;
    private int minutesFromStart;
    private boolean isActive;
    private LocalDateTime createdAt;
    private Integer createdBy;
    private LocalDateTime updatedAt;
    private Integer updatedBy;
}
