package com.ralsei.staff.dto.response.CoachAndRouteStop;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the response payload for coach stop operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CoachStopResponse {
    private int stopPointId;
    private String stopPointName;
    private String address;
    private String city;
    private boolean isActive;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime createdAt;
    private Integer createdBy;
    private LocalDateTime updatedAt;
    private Integer updatedBy;
}
