/**
 * Business validation rules for trip staff check-in operations.
 * Enforces staff assignment, check-in time windows, ticket status, and seat eligibility.
 */
package com.ralsei.service.tripstaff;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.ralsei.exception.BusinessRuleException;
import com.ralsei.model.PassengerTicket;
import com.ralsei.model.PassengerTicketDetail;
import com.ralsei.model.Route;
import com.ralsei.model.Trip;
import com.ralsei.model.enums.PassengerTicketDetailStatus;
import com.ralsei.model.enums.PassengerTicketStatus;

@Component
public class TripStaffCheckInPolicy {

    private static final int OPEN_BEFORE_MINUTES = 30;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public void assertStaffAssigned(Trip trip, int staffId) {
        if (trip.getDriverId() != staffId && trip.getAttendantId() != staffId) {
            throw new BusinessRuleException("Bạn không được phân công chuyến này");
        }
    }

    public void assertWithinCheckInWindow(Trip trip, Route route, LocalDateTime now) {
        LocalDateTime departureTime = trip.getDepartureTime();
        LocalDateTime checkInOpen = departureTime.minusMinutes(OPEN_BEFORE_MINUTES);
        LocalDateTime checkInClose = departureTime.plusMinutes(route.getTotalMinutes());

        if (now.isBefore(checkInOpen)) {
            throw new BusinessRuleException(
                    "Chưa đến giờ check-in (mở lúc " + checkInOpen.format(TIME_FORMAT) + ")");
        }
        if (now.isAfter(checkInClose)) {
            throw new BusinessRuleException("Đã hết thời gian check-in cho chuyến này");
        }
    }

    public void assertTicketBelongsToTrip(PassengerTicket ticket, int pathTripId) {
        if (ticket.getTripId() != pathTripId) {
            throw new BusinessRuleException("Vé thuộc chuyến đi khác");
        }
    }

    public void assertTicketConfirmed(PassengerTicket ticket) {
        if (ticket.getStatus() != PassengerTicketStatus.CONFIRMED) {
            if (ticket.getStatus() == PassengerTicketStatus.CANCELLED) {
                throw new BusinessRuleException("Vé đã bị hủy");
            }
            throw new BusinessRuleException("Vé chưa được xác nhận");
        }
    }

    public void assertDetailReadyForCheckIn(PassengerTicketDetail detail) {
        String status = detail.getStatus();
        if (PassengerTicketDetailStatus.CHECKED_IN.name().equals(status)) {
            throw new BusinessRuleException("Vé đã được quét trước đó");
        }
        if (PassengerTicketDetailStatus.CANCELLED.name().equals(status)
                || PassengerTicketDetailStatus.EXPIRED.name().equals(status)) {
            throw new BusinessRuleException("Vé đã hết hiệu lực");
        }
        if (!PassengerTicketDetailStatus.CONFIRMED.name().equals(status)) {
            throw new BusinessRuleException("Vé chưa được xác nhận");
        }
    }
}
