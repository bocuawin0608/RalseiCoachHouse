package com.ralsei.service.tripstaff;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.ralsei.exception.BusinessRuleException;
import com.ralsei.model.PassengerTicket;
import com.ralsei.model.PassengerTicketDetail;
import com.ralsei.model.Route;
import com.ralsei.model.Trip;
import com.ralsei.model.enums.PassengerTicketStatus;

/** Unit tests for UC-31 and BR-F02 through BR-F03 passenger check-in rules. */
class TripStaffCheckInPolicyTest {

    private final TripStaffCheckInPolicy policy = new TripStaffCheckInPolicy();

    @Test
    void permitsAssignedStaffToCheckInConfirmedSeatAtOpeningTime() {
        LocalDateTime departure = LocalDateTime.of(2026, 9, 20, 10, 0);
        Trip trip = Trip.builder()
                .tripId(17)
                .driverId(101)
                .attendantId(202)
                .departureTime(departure)
                .status("SCHEDULED")
                .build();
        Route route = Route.builder().totalMinutes(360).build();
        PassengerTicket ticket = PassengerTicket.builder()
                .tripId(17)
                .status(PassengerTicketStatus.CONFIRMED)
                .build();
        PassengerTicketDetail detail = PassengerTicketDetail.builder()
                .status("CONFIRMED")
                .build();

        assertDoesNotThrow(() -> policy.assertStaffAssigned(trip, 101));
        assertDoesNotThrow(() -> policy.assertWithinCheckInWindow(
                trip, route, departure.minusMinutes(60)));
        assertDoesNotThrow(() -> policy.assertTicketBelongsToTrip(ticket, 17));
        assertDoesNotThrow(() -> policy.assertTicketConfirmed(ticket));
        assertDoesNotThrow(() -> policy.assertDetailReadyForCheckIn(detail));
    }

    @Test
    void rejectsCheckInBeforeTheSixtyMinuteWindow() {
        LocalDateTime departure = LocalDateTime.of(2026, 9, 20, 10, 0);
        Trip trip = Trip.builder().departureTime(departure).build();
        Route route = Route.builder().totalMinutes(360).build();

        assertThrows(BusinessRuleException.class, () -> policy.assertWithinCheckInWindow(
                trip, route, departure.minusMinutes(61)));
    }

    @Test
    void rejectsUnassignedStaffAndPreviouslyCheckedInSeat() {
        Trip trip = Trip.builder().driverId(101).attendantId(202).build();
        PassengerTicketDetail detail = PassengerTicketDetail.builder().status("CHECKED_IN").build();

        assertThrows(BusinessRuleException.class, () -> policy.assertStaffAssigned(trip, 303));
        assertThrows(BusinessRuleException.class, () -> policy.assertDetailReadyForCheckIn(detail));
    }
}
