package com.ralsei.service.notification.impl;

import org.springframework.stereotype.Service;

import com.ralsei.dto.notification.PassengerTicketEmailPayload;
import com.ralsei.service.notification.TicketEmailService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TicketEmailServiceImpl implements TicketEmailService {

    @Override
    public void sendTicketConfirmation(PassengerTicketEmailPayload payload) {
        // TODO: integrate mail template + SMTP when email feature is ready
        log.info("Ticket email send on hold for ticketCode={}", payload.ticketCode());
    }
}
