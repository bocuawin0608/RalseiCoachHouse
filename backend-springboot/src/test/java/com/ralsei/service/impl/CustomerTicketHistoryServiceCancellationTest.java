package com.ralsei.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ralsei.dto.projection.customer.CustomerTicketHistoryProjection;
import com.ralsei.dto.request.customer.CustomerTicketCancellationRequest;
import com.ralsei.dto.response.customer.CustomerTicketCancellationResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.model.PassengerTicket;
import com.ralsei.model.PassengerTicketDetail;
import com.ralsei.model.Payment;
import com.ralsei.model.Refund;
import com.ralsei.model.enums.PassengerTicketStatus;
import com.ralsei.model.enums.TripSeatStatus;
import com.ralsei.repository.PassengerTicketDetailRepository;
import com.ralsei.repository.PassengerTicketRepository;
import com.ralsei.repository.PaymentRepository;
import com.ralsei.repository.RefundRepository;
import com.ralsei.repository.TripSeatRepository;
import com.ralsei.service.notification.PassengerTicketEmailAssembler;
import com.ralsei.service.notification.TicketEmailService;
import com.ralsei.service.passengerbooking.SeatHoldService;
import com.ralsei.util.QRCreateUitility;

/** Unit tests for customer self-cancellation (UC-12 and BR-D13). */
class CustomerTicketHistoryServiceCancellationTest {

    private PassengerTicketDetailRepository ticketDetailRepository;
    private PassengerTicketRepository ticketRepository;
    private PaymentRepository paymentRepository;
    private RefundRepository refundRepository;
    private TripSeatRepository tripSeatRepository;
    private SeatHoldService seatHoldService;
    private CustomerTicketHistoryServiceImpl service;

    @BeforeEach
    void setUp() {
        ticketDetailRepository = mock(PassengerTicketDetailRepository.class);
        ticketRepository = mock(PassengerTicketRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        refundRepository = mock(RefundRepository.class);
        tripSeatRepository = mock(TripSeatRepository.class);
        seatHoldService = mock(SeatHoldService.class);
        service = new CustomerTicketHistoryServiceImpl(
                ticketDetailRepository,
                ticketRepository,
                paymentRepository,
                refundRepository,
                tripSeatRepository,
                seatHoldService,
                mock(QRCreateUitility.class),
                new ObjectMapper(),
                mock(PassengerTicketEmailAssembler.class),
                mock(TicketEmailService.class));
    }

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void rejectsSelfCancellationAtTheFiveHourDepartureBoundary() {
        CustomerTicketHistoryProjection ticket = ticketRow(
                LocalDateTime.now().minusHours(25), LocalDateTime.now().plusHours(5));
        when(ticketDetailRepository.findCustomerTicketHistory(10, "PT-100"))
                .thenReturn(List.of(ticket));

        assertThrows(BusinessRuleException.class, () -> service.cancelTicket(
                10, "PT-100", cancellationRequest()));

        verify(ticketRepository, never()).findById(any());
        verify(paymentRepository, never()).findByPassengerTicketId(any());
    }

    @Test
    void cancelsEligibleTicketReleasesSeatsAndCreatesFullPendingRefund() {
        LocalDateTime now = LocalDateTime.now();
        CustomerTicketHistoryProjection ticket = ticketRow(now.minusHours(25), now.plusHours(6));
        when(ticketDetailRepository.findCustomerTicketHistory(10, "PT-100"))
                .thenReturn(List.of(ticket));
        when(ticketRepository.findById(100)).thenReturn(Optional.of(PassengerTicket.builder().build()));
        when(ticketDetailRepository.findByPassengerTicketId(100)).thenReturn(List.of(
                PassengerTicketDetail.builder().status("CONFIRMED").build()));
        Payment payment = Payment.builder()
                .paymentId(77)
                .amount(new BigDecimal("350000"))
                .status("COMPLETED")
                .build();
        when(paymentRepository.findByPassengerTicketId(100)).thenReturn(Optional.of(payment));
        when(refundRepository.existsByPaymentIdAndStatusIn(eq(77), any())).thenReturn(false);
        when(ticketRepository.updateStatusIfCurrent(
                100, PassengerTicketStatus.CONFIRMED, PassengerTicketStatus.CANCELLED)).thenReturn(1);
        when(ticketDetailRepository.findTripSeatIdsByPassengerTicketId(100)).thenReturn(List.of(41, 42));
        TransactionSynchronizationManager.initSynchronization();

        CustomerTicketCancellationResponse response = service.cancelTicket(10, "PT-100", cancellationRequest());

        assertEquals("CANCELLED", response.ticketStatus());
        assertEquals(new BigDecimal("350000"), response.refundAmount());
        assertEquals("PENDING", response.refundStatus());
        assertEquals(new BigDecimal("350000"), payment.getRefundAmount());
        verify(ticketDetailRepository).updateStatusByPassengerTicketId(100, "CANCELLED");
        verify(tripSeatRepository).updateStatusByTripSeatIds(List.of(41, 42), TripSeatStatus.AVAILABLE);
        verify(seatHoldService).forceReleaseSeatsByIds(List.of(41, 42));
        ArgumentCaptor<Refund> refundCaptor = ArgumentCaptor.forClass(Refund.class);
        verify(refundRepository).save(refundCaptor.capture());
        assertEquals("PENDING", refundCaptor.getValue().getStatus());
        assertEquals("BANK_TRANSFER", refundCaptor.getValue().getRefundMethod());
    }

    private CustomerTicketCancellationRequest cancellationRequest() {
        return new CustomerTicketCancellationRequest("VCB", "Ralsei", "123456789");
    }

    private CustomerTicketHistoryProjection ticketRow(LocalDateTime bookedAt, LocalDateTime departureTime) {
        CustomerTicketHistoryProjection row = mock(CustomerTicketHistoryProjection.class);
        when(row.getPassengerTicketId()).thenReturn(100);
        when(row.getTicketDetailId()).thenReturn(1);
        when(row.getTicketCode()).thenReturn("PT-100");
        when(row.getTicketStatus()).thenReturn("CONFIRMED");
        when(row.getTotalPrice()).thenReturn(new BigDecimal("350000"));
        when(row.getPickupStopName()).thenReturn("Quang Tri");
        when(row.getDropoffStopName()).thenReturn("Ha Noi");
        when(row.getBookedAt()).thenReturn(bookedAt);
        when(row.getDepartureTime()).thenReturn(departureTime);
        when(row.getSeatCode()).thenReturn("A01");
        when(row.getSeatPrice()).thenReturn(new BigDecimal("350000"));
        return row;
    }
}
