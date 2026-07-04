package com.ralsei.dto.projection.trip;
import java.time.LocalDateTime;

public interface TripDetailProjection {
    Integer getTripId();
    String getCoachTypeName();
    String getRouteName();
    String getSeatLayoutName();
    String getStatus();
    LocalDateTime getDepartureTime();
    LocalDateTime getArrivalTime();
    String getDuration();
    Double getSeatPrice();
    Integer getAvailableSeats();
    Integer getTotalSeats();
}
