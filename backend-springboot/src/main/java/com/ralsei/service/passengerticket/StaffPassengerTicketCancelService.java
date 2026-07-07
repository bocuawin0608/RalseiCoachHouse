package com.ralsei.service.passengerticket;

import com.ralsei.dto.request.staffpassengerticket.StaffPassengerTicketCancelRequest;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerTicketDetailResponse;

public interface StaffPassengerTicketCancelService {

    StaffPassengerTicketDetailResponse cancelFull(
        Integer accountId,
        String ticketCode,
        StaffPassengerTicketCancelRequest request
    );
}
