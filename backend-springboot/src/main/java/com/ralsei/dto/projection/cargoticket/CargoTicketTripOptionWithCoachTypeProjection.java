package com.ralsei.dto.projection.cargoticket;

import java.time.LocalDateTime;

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
