package com.ralsei.dto.projection.trip;

import java.time.LocalDateTime;

/**
 * Read-only customer view of one ordered stop on a concrete trip route.
 */
public interface TripStopProjection {

    Integer getTripId();

    String getRouteName();

    Integer getStopPointId();

    String getStopPointName();

    String getAddress();

    String getCity();

    Integer getStopOrder();

    Integer getMinutesFromStart();

    LocalDateTime getEstimatedStopTime();
}
