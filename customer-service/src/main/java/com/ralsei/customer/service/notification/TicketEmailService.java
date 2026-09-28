package com.ralsei.customer.service.notification;

import com.ralsei.customer.dto.notification.PassengerRefundCompletedEmailPayload;
import com.ralsei.customer.dto.notification.PassengerTicketCancellationEmailPayload;
import com.ralsei.customer.dto.notification.PassengerTicketEmailPayload;

/** Sends customer notifications for passenger ticket lifecycle events. */
public interface TicketEmailService {

    /**
     * Renders and sends a paid-ticket confirmation to the primary passenger.
     *
     * @param payload detached ticket information assembled before transaction commit
     */
    void sendTicketConfirmation(PassengerTicketEmailPayload payload);

    /**
     * Renders and sends a cancellation/refund notice to the primary passenger.
     *
     * @param payload detached cancellation information assembled before transaction commit
     */
    void sendTicketCancellation(PassengerTicketCancellationEmailPayload payload);

    /**
     * Renders and sends a refund completion notice to the primary passenger.
     *
     * @param payload detached refund completion information assembled before transaction commit
     */
    void sendRefundCompleted(PassengerRefundCompletedEmailPayload payload);

    /**
     * Renders and sends a ticket-updated notice after staff confirms a change session.
     *
     * @param payload detached current ticket snapshot assembled before transaction commit
     */
    void sendTicketUpdated(PassengerTicketEmailPayload payload);
}
