package com.ralsei.dto.response.staffpassengerticket;

import java.math.BigDecimal;

public record StaffPassengerItineraryPreviewResponse(
    BigDecimal originalNetPaid,
    BigDecimal newNetPaid,
    boolean eligible,
    String ineligibleReason,
    boolean requiresSeatSelection,
    boolean sameTrip
) {}
