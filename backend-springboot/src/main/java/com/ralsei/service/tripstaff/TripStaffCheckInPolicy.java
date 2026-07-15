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
/**
 * Provides the trip staff check in policy component for the application.
 */
public class TripStaffCheckInPolicy {

    private static final int CHECK_IN_OPEN_MINUTES_BEFORE_DEPARTURE = 60;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Executes the assert staff assigned operation.
     *
     * @param trip the value supplied for this operation
     * @param staffId the value supplied for this operation
     */
    public void assertStaffAssigned(Trip trip, int staffId) {
        if (trip.getDriverId() != staffId && trip.getAttendantId() != staffId) {
            throw new BusinessRuleException("Bạn không được phân công chuyến này");
        }
    }

    /**
     * Executes the assert within check in window operation.
     *
     * @param trip the value supplied for this operation
     * @param route the value supplied for this operation
     * @param now the value supplied for this operation
     */
    public void assertWithinCheckInWindow(Trip trip, Route route, LocalDateTime now) {
        LocalDateTime departureTime = trip.getDepartureTime();
        LocalDateTime checkInOpen = departureTime.minusMinutes(CHECK_IN_OPEN_MINUTES_BEFORE_DEPARTURE);
        LocalDateTime checkInClose = departureTime.plusMinutes(route.getTotalMinutes());

        if (now.isBefore(checkInOpen)) {
            throw new BusinessRuleException(
                    "Chưa đến giờ check-in (mở từ " + checkInOpen.format(TIME_FORMAT) + ")");
        }
        if (now.isAfter(checkInClose)) {
            throw new BusinessRuleException("Đã hết thời gian check-in cho chuyến này");
        }
    }

    /**
     * Executes the assert ticket belongs to trip operation.
     *
     * @param ticket the value supplied for this operation
     * @param pathTripId the value supplied for this operation
     */
    public void assertTicketBelongsToTrip(PassengerTicket ticket, int pathTripId) {
        if (ticket.getTripId() != pathTripId) {
            throw new BusinessRuleException("Vé thuộc chuyến đi khác");
        }
    }

    /**
     * Executes the assert ticket confirmed operation.
     *
     * @param ticket the value supplied for this operation
     */
    public void assertTicketConfirmed(PassengerTicket ticket) {
        PassengerTicketStatus status = ticket.getStatus();
        if (status == PassengerTicketStatus.CONFIRMED || status == PassengerTicketStatus.CHANGED) {
            return;
        }
        if (status == PassengerTicketStatus.CANCELLED) {
            throw new BusinessRuleException("Vé đã bị hủy");
        }
        throw new BusinessRuleException("Vé chưa được xác nhận");
    }

    /**
     * Executes the assert trip not completed operation.
     *
     * @param trip the value supplied for this operation
     */
    public void assertTripNotCompleted(Trip trip) {
        String status = trip.getStatus();
        if ("COMPLETED".equals(status)) {
            throw new BusinessRuleException("Chuyến đã kết thúc, không thể thực hiện thao tác");
        }
        if ("CANCELLED".equals(status)) {
            throw new BusinessRuleException("Chuyến đã bị hủy");
        }
    }

    /**
     * Executes the assert detail ready for check in operation.
     *
     * @param detail the value supplied for this operation
     */
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
