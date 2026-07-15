package com.ralsei.dto.projection.cargoticket;

import java.time.LocalDateTime;

/**
 * Projects the cargo ticket trip optio data shape for query results.
 */
public interface CargoTicketTripOptionProjection {
    Integer getTripId();
    String getRouteName();
    LocalDateTime getDepartureTime();
    LocalDateTime getPickupTime();
    Integer getPickupStopId();
    Integer getDropoffStopId();
    String getLicensePlate();
    String getTripStatus();
}
