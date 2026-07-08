package com.ralsei.dto.projection.staffpassengerticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One flat row per passenger seat for staff ticket search and detail assembly.
 */
public interface StaffPassengerTicketRowProjection {
    Integer getPassengerTicketId();
    Integer getTicketDetailId();
    String getTicketCode();
    String getTicketStatus();
    BigDecimal getTotalPrice();
    Integer getTripId();
    Integer getRouteId();
    String getPickupStopName();
    String getDropoffStopName();
    LocalDateTime getBookedAt();
    LocalDateTime getDepartureTime();
    String getRouteName();
    String getCoachTypeName();
    String getLicensePlate();
    String getVoucherCodeSnapshot();
    String getSoldByStaffName();
    String getPaymentMethod();
    String getPaymentStatus();
    BigDecimal getPaymentAmount();
    BigDecimal getPaymentRefundAmount();
    String getFullName();
    String getPhone();
    String getEmail();
    String getSeatCode();
    Integer getTripSeatId();
    BigDecimal getSeatPrice();
    String getDetailStatus();
    String getChildFullname();
    Integer getChildBirthYear();
}
