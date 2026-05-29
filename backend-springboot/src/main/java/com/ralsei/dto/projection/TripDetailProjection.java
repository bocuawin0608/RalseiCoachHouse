package com.ralsei.dto.projection;

import java.time.LocalDateTime;


public interface TripDetailProjection {
    String getRouteName();
    LocalDateTime getDepartureTime();
}