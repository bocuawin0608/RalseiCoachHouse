package com.ralsei.customer.service.notification;

import com.ralsei.customer.repository.RouteStopRepository;

import com.ralsei.customer.repository.TripRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.customer.dto.notification.PassengerSeatEmailItem;
import com.ralsei.customer.dto.notification.PassengerTicketEmailPayload;
import com.ralsei.customer.exception.ResourceNotFoundException;
import com.ralsei.customer.model.PassengerTicket;
import com.ralsei.customer.model.PassengerTicketDetail;
import com.ralsei.customer.model.Payment;
import com.ralsei.customer.repository.PassengerTicketDetailRepository;
import com.ralsei.customer.repository.PassengerTicketRepository;
import com.ralsei.customer.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Loads a confirmed passenger ticket and converts it into a detached email DTO.
 * The assembler is read-only and deliberately reuses existing repositories so
 * email delivery does not introduce a second, subtly different ticket query.
 */
@Service
@RequiredArgsConstructor
/**
 * Provides the passenger ticket email assembler component for the application.
 */
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
    /**
     * Executes the assemble operation.
     *
     * @param passengerTicketId the value supplied for this operation
     *
     * @return the operation result
     */
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

        // [MICROSERVICE-REFACTOR]: Fetch trip info via FeignClient
        String routeName = null;
        String coachTypeName = null;
        String coachLicensePlate = null;

        LocalDateTime departureTime = null;
        LocalDateTime arrivalTime = null;
        LocalDateTime pickupPresentBy = null;

        PassengerTicketDetail primaryDetail = details.get(0);
        List<PassengerSeatEmailItem> seats = details.stream()
            .map(detail -> new PassengerSeatEmailItem(
                null, // seatCodeSnapshot not available here
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
            null, // pickupStopName
            null, // dropoffStopName
            pickupPresentBy,
            primaryDetail.getFullName(),
            primaryDetail.getPhone(),
            null, // email
            ticket.getTotalPrice(),
            List.copyOf(seats)
        );
    }
}
