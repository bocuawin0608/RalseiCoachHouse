package com.ralsei.dto.projection.cargoticket;

import java.time.LocalDateTime;

/**
 * Read-only summary of a coach with cargo waiting for destination-office receipt.
 */
public interface CargoReceivingTripProjection {
    Integer getTripId();
    String getRouteName();
    LocalDateTime getDepartureTime();
    String getTripStatus();
    String getLicensePlate();
    String getCoachTypeName();
    String getDriverName();
    String getDriverPhone();
    String getDriverCccd();
    String getAttendantName();
    String getAttendantPhone();
    String getAttendantCccd();
    LocalDateTime getLastCargoUpdateAt();
    Long getWaitingOrderCount();
}
