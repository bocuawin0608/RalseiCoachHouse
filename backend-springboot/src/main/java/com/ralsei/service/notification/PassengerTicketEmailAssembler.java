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

/**
 * Loads a confirmed passenger ticket and converts it into a detached email DTO.
 * The assembler is read-only and deliberately reuses existing repositories so
 * email delivery does not introduce a second, subtly different ticket query.
 */
@Service
@RequiredArgsConstructor
public class PassengerTicketEmailAssembler {

    private final PassengerTicketRepository passengerTicketRepository;
    private final PassengerTicketDetailRepository passengerTicketDetailRepository;
    private final PaymentRepository paymentRepository;
    private final TripRepository tripRepository;
    private final RouteStopRepository routeStopRepository;

    /**
     * Builds all information needed after the payment transaction has completed.
     *
     * @param passengerTicketId database identifier of the paid passenger ticket
     * @return detached payload safe to use after the transaction commits
     * @throws ResourceNotFoundException when the ticket or its seat details do not exist
     */
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
        String coachLicensePlate = trip != null && trip.getCoach() != null
            ? trip.getCoach().getLicensePlate()
            : null;

        LocalDateTime departureTime = trip != null ? trip.getDepartureTime() : null;
        LocalDateTime arrivalTime = resolveStopTime(trip, ticket.getDropoffStopId());
        LocalDateTime pickupPresentBy = resolveStopTime(trip, ticket.getPickupStopId());

        PassengerTicketDetail primaryDetail = details.get(0);
        List<PassengerSeatEmailItem> seats = details.stream()
            .map(detail -> new PassengerSeatEmailItem(
                detail.getSeatCodeSnapshot(),
                detail.getFullName(),
                detail.getPhone(),
                detail.getQrcode()
            ))
            .toList();

        return new PassengerTicketEmailPayload(
            ticket.getTicketCode(),
            payment != null ? payment.getTransactionId() : null,
            payment != null ? payment.getPaymentTime() : null,
            routeName,
            coachTypeName,
            coachLicensePlate,
            departureTime,
            arrivalTime,
            ticket.getPickupStopName(),
            ticket.getDropoffStopName(),
            pickupPresentBy,
            primaryDetail.getFullName(),
            primaryDetail.getPhone(),
            primaryDetail.getEmail(),
            ticket.getTotalPrice(),
            List.copyOf(seats)
        );
    }

    /**
     * Calculates when the coach reaches the selected pickup stop according to
     * the route-stop schedule.
     */
    private LocalDateTime resolveStopTime(Trip trip, int stopPointId) {
        if (trip == null || trip.getDepartureTime() == null) {
            return null;
        }
        Optional<RouteStop> stop = routeStopRepository.findByRouteIdAndStopPointId(
            trip.getRouteId(), stopPointId);
        int minutesFromStart = stop.map(RouteStop::getMinutesFromStart).orElse(0);
        return trip.getDepartureTime().plusMinutes(minutesFromStart);
    }
}
