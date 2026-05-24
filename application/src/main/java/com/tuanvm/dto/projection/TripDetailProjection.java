package com.tuanvm.dto.projection;

import java.time.LocalDateTime;

public interface TripDetailProjection {
    String getRouteName();
    String getSeatLayoutName();
    String getStatus();
    LocalDateTime getDepartureTime();
}