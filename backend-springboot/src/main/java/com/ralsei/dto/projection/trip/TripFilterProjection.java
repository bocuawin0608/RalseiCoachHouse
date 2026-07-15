package com.ralsei.dto.projection.trip;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Read-only customer result returned by the advanced trip-search query.
 * Values map directly from SQL aliases and must remain safe for public display.
 */
/**
 * Projects the trip filte data shape for query results.
 */
public interface TripFilterProjection {
    Integer getTripId();
    String getCoachTypeName(); 
    String getRouteName();
    LocalDateTime getDepartureTime();
    LocalDateTime getArrivalTime();
    String getDuration();
    BigDecimal getSeatPrice();
    Integer getAvailableSeats();
    Integer getTotalSeats();
}
