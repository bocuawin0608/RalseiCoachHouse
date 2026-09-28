package com.ralsei.driver.dto.response.internal;

import com.ralsei.driver.model.CoachStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachInfoDTO {
    private Integer coachId;
    private String licensePlate;
    private String coachTypeName;
    private Integer totalSeat;
    private CoachStatus status;
    private Integer routeId;
}
