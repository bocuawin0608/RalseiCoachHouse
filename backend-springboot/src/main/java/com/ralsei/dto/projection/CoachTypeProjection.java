package com.ralsei.dto.projection;

import java.math.BigDecimal;

/**
 * CoachTypeProjection
 */

/**
 * Projects the coach typ data shape for query results.
 */
public interface CoachTypeProjection {
    Integer getCoachTypeId();
    String getCoachTypeName();
    Integer getTotalSeat();           
    BigDecimal getCurrentPrice();     
    Boolean getIsActive();
    Integer getTotalCoach();
}
