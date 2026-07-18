package com.ralsei.dto.request.staffpassengerticket;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

/**
 * Batch payload for one staff "Đổi vé" confirm session.
 */
public record StaffPassengerTicketChangesRequest(
    @Valid
    @Size(max = 10, message = "Chỉ được cập nhật tối đa 10 hành khách trong một lần.")
    List<StaffPassengerTicketPassengerUpdateItem> passengerUpdates,

    @Valid
    @Size(max = 10, message = "Chỉ được đổi tối đa 10 ghế trong một lần.")
    List<StaffPassengerTicketSeatChangeItem> seatChanges,

    @Valid
    StaffPassengerItineraryChangeRequest itineraryChange
) {}
