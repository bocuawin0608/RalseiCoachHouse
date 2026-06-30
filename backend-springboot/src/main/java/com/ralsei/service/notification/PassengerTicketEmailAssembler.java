package com.ralsei.service.notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.notification.PassengerSeatEmailItem;
import com.ralsei.dto.notification.PassengerTicketEmailPayload;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.PassengerTicket;
import com.ralsei.model.PassengerTicketDetail;
import com.ralsei.model.Payment;
import com.ralsei.model.RouteStop;
import com.ralsei.model.Trip;
import com.ralsei.repository.PassengerTicketDetailRepository;
import com.ralsei.repository.PassengerTicketRepository;
import com.ralsei.repository.PaymentRepository;
import com.ralsei.repository.RouteStopRepository;
import com.ralsei.repository.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PassengerTicketEmailAssembler {

    private final PassengerTicketRepository passengerTicketRepository;
    private final PassengerTicketDetailRepository passengerTicketDetailRepository;
    private final PaymentRepository paymentRepository;
    private final TripRepository tripRepository;
    private final RouteStopRepository routeStopRepository;

    @Transactional(readOnly = true)
    public PassengerTicketEmailPayload assemble(Integer passengerTicketId) {
        PassengerTicket ticket = passengerTicketRepository.findById(passengerTicketId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé!"));

        List<PassengerTicketDetail> details = passengerTicketDetailRepository
            .findByPassengerTicketId(passengerTicketId);
        if (details.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy chi tiết vé!");
        }

        Payment payment = paymentRepository.findByPassengerTicketId(passengerTicketId)
            .orElse(null);

        Trip trip = tripRepository.findByIdWithRouteAndCoach(ticket.getTripId()).orElse(null);
        String routeName = trip != null && trip.getRoute() != null ? trip.getRoute().getRouteName() : null;
        String coachTypeName = trip != null && trip.getCoach() != null && trip.getCoach().getCoachType() != null
            ? trip.getCoach().getCoachType().getCoachTypeName()
            : null;

        LocalDateTime departureTime = trip != null ? trip.getDepartureTime() : null;
        LocalDateTime pickupPresentBy = resolvePickupPresentBy(trip, ticket.getPickupStopId());

        PassengerTicketDetail primaryDetail = details.get(0);
        List<PassengerSeatEmailItem> seats = details.stream()
            .map(detail -> new PassengerSeatEmailItem(
                detail.getSeatCodeSnapshot(),
                detail.getFullName(),
                detail.getPhone(),
                null // TODO: render QR image URL/CID from detail.getQrcode() when mailer is ready
            ))
            .toList();

        return new PassengerTicketEmailPayload(
            ticket.getTicketCode(),
            payment != null ? payment.getTransactionId() : null,
            payment != null ? payment.getPaymentTime() : null,
            routeName,
            coachTypeName,
            departureTime,
            null,
            ticket.getPickupStopName(),
            ticket.getDropoffStopName(),
            pickupPresentBy,
            primaryDetail.getFullName(),
            primaryDetail.getPhone(),
            primaryDetail.getEmail(),
            ticket.getTotalPrice(),
            seats
        );
    }

    private LocalDateTime resolvePickupPresentBy(Trip trip, int pickupStopId) {
        if (trip == null || trip.getDepartureTime() == null) {
            return null;
        }
        Optional<RouteStop> pickupStop = routeStopRepository.findByRouteIdAndStopPointId(
            trip.getRouteId(), pickupStopId);
        int minutesFromStart = pickupStop.map(RouteStop::getMinutesFromStart).orElse(0);
        return trip.getDepartureTime().plusMinutes(minutesFromStart);
    }
}
