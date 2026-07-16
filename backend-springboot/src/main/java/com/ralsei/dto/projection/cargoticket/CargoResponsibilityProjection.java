package com.ralsei.dto.projection.cargoticket;

/**
 * Projects the vehicle and assigned staff responsible for one cargo ticket.
 * This is operational trace data and must remain available after departure.
 */
public interface CargoResponsibilityProjection {
    String getRouteName();
    String getLicensePlate();
    String getDestinationAgencyName();
    String getDriverName();
    String getDriverPhone();
    String getDriverCccd();
    String getAttendantName();
    String getAttendantPhone();
    String getAttendantCccd();
}
