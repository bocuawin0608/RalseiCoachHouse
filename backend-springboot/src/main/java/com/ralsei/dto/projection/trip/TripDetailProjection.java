package com.ralsei.dto.projection.trip;
import java.time.LocalDateTime;

public interface TripDetailProjection {
    String getRouteName();
    String getSeatLayoutName();
    String getStatus();
    LocalDateTime getDepartureTime();
    Double getSeatPrice();
}