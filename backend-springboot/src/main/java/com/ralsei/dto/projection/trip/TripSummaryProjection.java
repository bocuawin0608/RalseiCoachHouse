package com.ralsei.dto.projection.trip;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Read-only staff trip row containing display values plus identifiers required
 * to open the complete edit form.
 */
public interface TripSummaryProjection {

    Integer getTripId();

    Integer getRouteId();

    String getRouteName();

    Integer getCoachId();

    Integer getDriverId();

    String getDriverName();

    String getDriverPhone();

    Integer getAttendantId();

    String getAttendantName();

    String getAttendantPhone();

    String getTripStatus();
    String getManufacturer();

    String getCoachTypeName();

    String getLicensePlate();

    String getCoachStatus(); 

    LocalDate getDepartureDate(); 

    LocalTime getDepartureTime(); 
    Integer getAvailableSeats(); 

    Integer getTotalSeats(); 
}
