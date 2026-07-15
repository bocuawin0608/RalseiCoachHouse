/**
 * Response containing the cargo list for a trip.
 * Nested records represent individual cargo items and their detail lines.
 */
package com.ralsei.dto.response.tripstaff;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents the response payload for trip staff cargo operations.
 */
public record TripStaffCargoResponse(
    List<CargoItem> cargoItems
) {
    /**
     * Provides the cargo item component for the application.
     */
    public record CargoItem(
        int cargoTicketId,
        String ticketCode,
        String senderName,
        String senderPhone,
        String receiverName,
        String receiverPhone,
        String pickupStopName,
        String dropoffStopName,
        String status,
        BigDecimal totalPrice,
        String description,
        String feePayer,
        BigDecimal codAmount,
        List<CargoDetailItem> details
    ) {}

    /**
     * Provides the cargo detail item component for the application.
     */
    public record CargoDetailItem(
        int cargoTicketDetailId,
        String description,
        int quantity,
        BigDecimal weightKg,
        BigDecimal dimensionVol,
        BigDecimal calculatedPrice,
        String unit
    ) {}
}
