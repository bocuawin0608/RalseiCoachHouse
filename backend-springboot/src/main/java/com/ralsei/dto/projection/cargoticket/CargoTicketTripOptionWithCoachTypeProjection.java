package com.ralsei.dto.projection.cargoticket;

import java.time.LocalDateTime;

/**
 * Projects the cargo ticket trip option with coach typ data shape for query results.
 */
public interface CargoTicketTripOptionWithCoachTypeProjection {
    Integer getTripId();
    String getRouteName();
    LocalDateTime getDepartureTime();
    LocalDateTime getPickupTime();
    Integer getPickupStopId();
    Integer getDropoffStopId();
    String getLicensePlate();
    String getCoachTypeName();
    String getTripStatus();
}
