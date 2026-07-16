package com.ralsei.dto.response.cargoticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;
import com.ralsei.util.CargoVolumePolicy;

/**
 * Staff-facing summary of an upcoming coach and its cargo responsibility chain.
 */
@Value
@Builder
public class CargoOperationalTripResponse {
    Integer tripId;
    Integer routeId;
    String routeName;
    LocalDateTime departureTime;
    LocalDateTime pickupTime;
    Integer pickupStopId;
    String pickupStopName;
    String pickupCity;
    String tripStatus;
    String licensePlate;
    String coachTypeName;
    String driverName;
    String driverPhone;
    String driverCccd;
    String attendantName;
    String attendantPhone;
    String attendantCccd;
    String stopSummary;
    BigDecimal usedCargoVolume;
    BigDecimal cargoCapacity;
    boolean full;

    /** Returns the approved fixed cargo capacity used by the current fleet. */
    public static BigDecimal capacity() {
        return CargoVolumePolicy.maxVolumeM3();
    }
}
