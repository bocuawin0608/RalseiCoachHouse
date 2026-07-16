package com.ralsei.dto.projection.cargoticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Read-only database view of an upcoming coach that can accept cargo.
 *
 * <p>The projection deliberately keeps the capacity calculation in the query so
 * every staff screen uses the same load figure. A cargo detail's volume is per
 * package, therefore the occupied volume is {@code dimensionVol * quantity}.</p>
 */
public interface CargoOperationalTripProjection {
    Integer getTripId();
    Integer getRouteId();
    String getRouteName();
    LocalDateTime getDepartureTime();
    LocalDateTime getPickupTime();
    Integer getPickupStopId();
    String getPickupStopName();
    String getPickupCity();
    String getTripStatus();
    String getLicensePlate();
    String getCoachTypeName();
    String getDriverName();
    String getDriverPhone();
    String getDriverCccd();
    String getAttendantName();
    String getAttendantPhone();
    String getAttendantCccd();
    String getStopSummary();
    BigDecimal getUsedCargoVolume();
}
