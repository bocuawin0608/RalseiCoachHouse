package com.ralsei.driver.dto.response.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachTypeInfoDTO {
    private Integer coachTypeId;
    private String coachTypeName;
    private Integer totalSeat;
    private String seatLayout;
    private Boolean isActive;
}
