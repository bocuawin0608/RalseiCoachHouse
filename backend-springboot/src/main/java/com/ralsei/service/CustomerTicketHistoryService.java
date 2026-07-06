package com.ralsei.service;

import java.util.List;

import com.ralsei.dto.response.customer.CustomerTicketHistoryResponse;
import com.ralsei.dto.request.customer.CustomerTicketCancellationRequest;
import com.ralsei.dto.response.customer.CustomerTicketCancellationResponse;

/**
 * Defines authenticated customer operations for booking history and boarding QR images.
 */
public interface CustomerTicketHistoryService {

    /**
     * Returns every booking owned by the authenticated account.
     *
     * @param accountId verified JWT account identifier
     * @return bookings ordered from newest departure to oldest
     */
    List<CustomerTicketHistoryResponse> getHistory(Integer accountId);

    /**
     * Returns one owned booking using its public ticket code.
     *
     * @param accountId verified JWT account identifier
     * @param ticketCode public booking code
     * @return booking details including its passenger seats
     */
    CustomerTicketHistoryResponse getDetail(Integer accountId, String ticketCode);

    /**
     * Renders the persisted boarding token for an owned seat as PNG.
     *
     * @param accountId verified JWT account identifier
     * @param ticketDetailId requested passenger-seat detail identifier
     * @return PNG image bytes
     */
    byte[] getSeatQrImage(Integer accountId, Integer ticketDetailId);

    /** Cancels an owned future ticket and creates its pending bank refund. */
    CustomerTicketCancellationResponse cancelTicket(
        Integer accountId,
        String ticketCode,
        CustomerTicketCancellationRequest request
    );
}
