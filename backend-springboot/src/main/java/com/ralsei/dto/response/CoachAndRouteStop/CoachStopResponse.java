package com.ralsei.dto.response.CoachAndRouteStop;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Represents the response payload for coach stop operations.
 */
public class CoachStopResponse {
    private int stopPointId;
    private String stopPointName;
    private String address;
    private String city;
    private boolean isActive;
    private LocalDateTime createdAt;
    private Integer createdBy;
    private LocalDateTime updatedAt;
    private Integer updatedBy;
}
