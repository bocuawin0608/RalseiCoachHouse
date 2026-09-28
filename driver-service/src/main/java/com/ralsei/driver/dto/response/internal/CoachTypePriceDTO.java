package com.ralsei.driver.dto.response.internal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachTypePriceDTO {
    private Integer coachTypePriceId;
    private BigDecimal seatPrice;
    private LocalDateTime startEffectiveDate;
    private LocalDateTime endEffectiveDate;
}
