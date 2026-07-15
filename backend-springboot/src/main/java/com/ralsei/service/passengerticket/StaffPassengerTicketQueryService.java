package com.ralsei.service.passengerticket;

import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerTicketDetailResponse;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerTicketListItemResponse;

/**
 * Provides the business service contract for staff passenger ticket query.
 */
public interface StaffPassengerTicketQueryService {

    PagedResponse<StaffPassengerTicketListItemResponse> search(
        String phone,
        String ticketCode,
        String status,
        Integer routeId,
        Integer tripId,
        java.time.LocalDate departureDate,
        int page,
        int size
    );

    StaffPassengerTicketDetailResponse getDetail(String ticketCode);

    byte[] getSeatQrImage(String ticketCode, Integer ticketDetailId);
}
