package com.ralsei.dto.projection.trip;

import java.math.BigDecimal;
import java.time.LocalDateTime;
public interface TripFilterProjection {
    Integer getTripId();
    String getCoachTypeName(); 
    String getRouteName();
    LocalDateTime getDepartureTime();
    BigDecimal getSeatPrice();
}
