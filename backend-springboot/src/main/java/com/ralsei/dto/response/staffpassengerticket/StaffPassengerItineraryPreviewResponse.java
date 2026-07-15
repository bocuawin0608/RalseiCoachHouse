package com.ralsei.dto.response.staffpassengerticket;

import java.math.BigDecimal;

/**
 * Represents the response payload for staff passenger itinerary preview operations.
 */
public record StaffPassengerItineraryPreviewResponse(
    BigDecimal originalNetPaid,
    BigDecimal newNetPaid,
    boolean eligible,
    String ineligibleReason,
    boolean requiresSeatSelection,
    boolean sameTrip
) {}
