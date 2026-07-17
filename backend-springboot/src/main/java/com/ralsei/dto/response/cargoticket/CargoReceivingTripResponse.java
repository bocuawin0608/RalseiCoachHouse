package com.ralsei.dto.response.cargoticket;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

/**
 * Ticket-staff view of one unloaded coach and its pending receipt count.
 */
@Value
@Builder
public class CargoReceivingTripResponse {
    Integer tripId;
    String routeName;
    LocalDateTime departureTime;
    String tripStatus;
    String licensePlate;
    String coachTypeName;
    String driverName;
    String driverPhone;
    String driverCccd;
    String attendantName;
    String attendantPhone;
    String attendantCccd;
    LocalDateTime lastCargoUpdateAt;
    Long waitingOrderCount;
}
