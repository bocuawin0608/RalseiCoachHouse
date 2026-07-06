package com.ralsei.dto.response.customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Complete read-only cargo order returned by the phone lookup endpoint. */
public record CargoOrderLookupResponse(
    Integer cargoTicketId,
    String ticketCode,
    String status,
    BigDecimal totalPrice,
    BigDecimal codAmount,
    String feePayer,
    String description,
    LocalDateTime bookedAt,
    Integer tripId,
    LocalDateTime departureTime,
    String routeName,
    String licensePlate,
    String driverName,
    String ticketAgencyName,
    CargoStopResponse pickupStop,
    CargoStopResponse dropoffStop,
    CargoPartyResponse sender,
    CargoPartyResponse receiver,
    List<CargoDetailResponse> items,
    List<CargoRouteStopResponse> routeStops
) {
    /** Named pickup or drop-off location displayed by the customer site. */
    public record CargoStopResponse(Integer stopPointId, String name, String address, String city) {}

    /** Sender or receiver contact snapshot saved with the order. */
    public record CargoPartyResponse(String name, String phone) {}

    /** One priced cargo line belonging to the master order. */
    public record CargoDetailResponse(
        Integer cargoTicketDetailId,
        String cargoTypeName,
        String unit,
        String description,
        Integer quantity,
        BigDecimal weightKg,
        BigDecimal dimensionVol,
        BigDecimal calculatedPrice
    ) {}

    /** One route stop between the cargo pickup and drop-off points. */
    public record CargoRouteStopResponse(
        Integer stopPointId,
        String name,
        String address,
        String city,
        Integer stopOrder,
        LocalDateTime estimatedTime
    ) {}
}
