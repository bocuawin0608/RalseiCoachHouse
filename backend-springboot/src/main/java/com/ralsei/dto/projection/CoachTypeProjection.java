package com.ralsei.dto.projection;

import java.math.BigDecimal;

public interface CoachTypeProjection {
    Integer getCoachTypeId();
    String getCoachTypeName();
    Integer getTotalSeat();           
    BigDecimal getCurrentPrice();     
    Boolean getIsActive();
    Integer getTotalCoach();
}
