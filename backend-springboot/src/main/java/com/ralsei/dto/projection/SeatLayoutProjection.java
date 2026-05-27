package com.ralsei.dto.projection;

import java.math.BigDecimal;

public interface SeatLayoutProjection {
    Integer getSeatLayoutId();
    String getSeatLayoutName();
    Integer getTotalSeat();           
    BigDecimal getCurrentPrice();     
    Boolean getIsActive();
}
