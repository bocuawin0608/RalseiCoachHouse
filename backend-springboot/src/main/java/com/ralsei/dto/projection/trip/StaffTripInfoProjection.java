package com.ralsei.dto.projection.trip;

import java.time.LocalDateTime;

/**
 * Read-only ticket-staff trip lookup row.
 *
 * <p>This projection exists for the internal "view trip info" flow where staff
 * need operational data for upcoming trips: city, coach plate, crew, fare, seat
 * availability, and trip status. It is intentionally separate from customer
 * search projections so staff-only fields do not leak into public APIs.</p>
 */
public interface StaffTripInfoProjection {

    Integer getTripId();

    String getDepartureCity();

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

    String getLicensePlate();

    String getDriverName();

    String getAttendantName();
}
