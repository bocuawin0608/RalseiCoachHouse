package com.ralsei.service.notification;

import com.ralsei.dto.notification.PassengerTicketEmailPayload;

/** Sends customer notifications for confirmed passenger tickets. */
public interface TicketEmailService {

    /**
     * Renders and sends a paid-ticket confirmation to the primary passenger.
     *
     * @param payload detached ticket information assembled before transaction commit
     */
    void sendTicketConfirmation(PassengerTicketEmailPayload payload);
}
