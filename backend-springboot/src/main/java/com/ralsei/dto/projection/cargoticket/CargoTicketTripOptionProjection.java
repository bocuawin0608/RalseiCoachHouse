package com.ralsei.dto.projection.cargoticket;

import java.time.LocalDateTime;

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
