package com.ralsei.dto.projection;

import java.math.BigDecimal;

/**
 * CoachTypeProjection
 */

public interface CoachTypeProjection {
    Integer getCoachTypeId();
    String getCoachTypeName();
    Integer getTotalSeat();           
    BigDecimal getCurrentPrice();     
    Boolean getIsActive();
    Integer getTotalCoach();
}
