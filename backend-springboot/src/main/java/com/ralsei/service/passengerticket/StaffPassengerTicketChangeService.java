package com.ralsei.service.passengerticket;

import com.ralsei.dto.request.staffpassengerticket.StaffPassengerChangePassengerRequest;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerTicketDetailResponse;

public interface StaffPassengerTicketChangeService {

    StaffPassengerTicketDetailResponse changePassengerInfo(
        Integer accountId,
        String ticketCode,
        Integer ticketDetailId,
        StaffPassengerChangePassengerRequest request
    );
}
