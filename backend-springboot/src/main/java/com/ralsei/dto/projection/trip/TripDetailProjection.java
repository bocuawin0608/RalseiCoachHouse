package com.ralsei.dto.projection.trip;
import java.time.LocalDateTime;

/**
 * Read-only customer result returned by the default trip-search query.
 * Its common fields intentionally match {@link TripFilterProjection} so React
 * can use one card renderer for default and filtered searches.
 */
/**
 * Projects the trip detai data shape for query results.
 */
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
