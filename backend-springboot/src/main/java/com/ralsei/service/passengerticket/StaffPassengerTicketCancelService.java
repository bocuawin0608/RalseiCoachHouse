package com.ralsei.service.passengerticket;

import com.ralsei.dto.request.staffpassengerticket.StaffPassengerTicketCancelRequest;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerTicketDetailResponse;

/**
 * Provides the business service contract for staff passenger ticket cancel.
 */
public interface StaffPassengerTicketCancelService {

    StaffPassengerTicketDetailResponse cancelFull(
        Integer accountId,
        String ticketCode,
        StaffPassengerTicketCancelRequest request
    );
}
