package com.ralsei.service.notification;

import com.ralsei.dto.notification.PassengerTicketEmailPayload;

public interface TicketEmailService {

    void sendTicketConfirmation(PassengerTicketEmailPayload payload);
}
