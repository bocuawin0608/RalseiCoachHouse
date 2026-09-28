package com.ralsei.driver.dto.response.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDTO {
    private Integer seatId;
    private String seatCode;
    private Integer rowIndex;
    private Integer colIndex;
    private Integer floorIndex;
    private Boolean isActive;
}
