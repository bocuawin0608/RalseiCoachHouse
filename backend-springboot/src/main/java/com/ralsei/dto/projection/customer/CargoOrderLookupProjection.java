package com.ralsei.dto.projection.customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Flat, read-only database row used to assemble one public cargo order.
 * Master-ticket columns repeat for each cargo detail and are grouped by the service.
 */
/**
 * Projects the cargo order looku data shape for query results.
 */
public interface CargoOrderLookupProjection {
    Integer getCargoTicketId();
    String getTicketCode();
    String getStatus();
    BigDecimal getTotalPrice();
    BigDecimal getCodAmount();
    String getFeePayer();
    String getTicketDescription();
    LocalDateTime getBookedAt();
    Integer getTripId();
    LocalDateTime getDepartureTime();
    String getRouteName();
    String getLicensePlate();
    String getDriverName();
    String getTicketAgencyName();
    Integer getPickupStopId();
    String getPickupStopName();
    String getPickupAddress();
    String getPickupCity();
    Integer getDropoffStopId();
    String getDropoffStopName();
    String getDropoffAddress();
    String getDropoffCity();
    String getSenderName();
    String getSenderPhone();
    String getReceiverName();
    String getReceiverPhone();
    Integer getCargoTicketDetailId();
    String getCargoTypeName();
    String getUnit();
    String getDetailDescription();
    Integer getQuantity();
    BigDecimal getWeightKg();
    BigDecimal getDimensionVol();
    BigDecimal getCalculatedPrice();
}
