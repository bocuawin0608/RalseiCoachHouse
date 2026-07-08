package com.ralsei.service.passengerticket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ralsei.dto.projection.staffpassengerticket.StaffPassengerTicketRowProjection;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.model.enums.PassengerTicketDetailStatus;
import com.ralsei.model.enums.PassengerTicketMajorChangeType;
import com.ralsei.model.enums.PassengerTicketStatus;

@Component
public class PassengerTicketStaffPolicy {

    private static final long CHANGE_CUTOFF_HOURS = 3;
    private static final long FULL_REFUND_CUTOFF_HOURS = 5;

    public long hoursUntilDeparture(LocalDateTime departureTime) {
        if (departureTime == null) {
            return Long.MIN_VALUE;
        }
        return Duration.between(LocalDateTime.now(), departureTime).toHours();
    }

    public LocalDateTime resolveRefundPolicyDepartureTime(
        LocalDateTime refundPolicyDepartureTime,
        LocalDateTime liveDepartureTime
    ) {
        return refundPolicyDepartureTime != null ? refundPolicyDepartureTime : liveDepartureTime;
    }

    public String resolveRefundTierLabel(long hoursUntilDeparture) {
        if (hoursUntilDeparture < CHANGE_CUTOFF_HOURS) {
            return "Không được hủy";
        }
        if (hoursUntilDeparture > FULL_REFUND_CUTOFF_HOURS) {
            return "100%";
        }
        return "50%";
    }

    public boolean canModify(long hoursUntilDeparture) {
        return hoursUntilDeparture >= CHANGE_CUTOFF_HOURS;
    }

    public boolean canCancel(long hoursUntilDeparture) {
        return hoursUntilDeparture >= CHANGE_CUTOFF_HOURS;
    }

    public BigDecimal calculateRefundAmount(long hoursUntilDeparture, BigDecimal paymentAmount) {
        if (paymentAmount == null) {
            throw new BusinessRuleException("Không xác định được số tiền thanh toán.");
        }
        if (hoursUntilDeparture < CHANGE_CUTOFF_HOURS) {
            throw new BusinessRuleException(
                "Không thể hủy vé trong vòng " + CHANGE_CUTOFF_HOURS + " giờ trước giờ khởi hành."
            );
        }
        if (hoursUntilDeparture > FULL_REFUND_CUTOFF_HOURS) {
            return paymentAmount;
        }
        return paymentAmount.multiply(new BigDecimal("0.5")).setScale(0, RoundingMode.HALF_UP);
    }

    public void assertMajorChangeQuotaAvailable(PassengerTicketMajorChangeType majorChangeType) {
        if (majorChangeType != null) {
            throw new BusinessRuleException(
                "Vé đã sử dụng quyền đổi chuyến hoặc hủy vé. Không thể thực hiện thêm thao tác này."
            );
        }
    }

    public void assertCancelFullAllowed(
        String ticketStatus,
        List<StaffPassengerTicketRowProjection> seatRows,
        String paymentStatus,
        LocalDateTime departureTime,
        String tripStatus,
        PassengerTicketMajorChangeType majorChangeType
    ) {
        assertMajorChangeQuotaAvailable(majorChangeType);

        boolean ticketActive = PassengerTicketStatus.CONFIRMED.name().equals(ticketStatus)
            || PassengerTicketStatus.CHANGED.name().equals(ticketStatus);

        if (!ticketActive) {
            throw new BusinessRuleException("Chỉ được hủy vé đã xác nhận hoặc đã có thay đổi.");
        }

        if (!"COMPLETED".equals(paymentStatus)) {
            throw new BusinessRuleException("Vé chưa thanh toán đầy đủ, không thể hủy.");
        }

        if (!"SCHEDULED".equals(tripStatus)) {
            throw new BusinessRuleException("Chỉ được hủy vé trên chuyến đang lên lịch.");
        }

        if (!canCancel(hoursUntilDeparture(departureTime))) {
            throw new BusinessRuleException(
                "Không thể hủy vé trong vòng " + CHANGE_CUTOFF_HOURS + " giờ trước giờ khởi hành."
            );
        }

        boolean hasCheckedInSeat = seatRows.stream()
            .anyMatch(row -> PassengerTicketDetailStatus.CHECKED_IN.name().equals(row.getDetailStatus()));
        if (hasCheckedInSeat) {
            throw new BusinessRuleException("Không thể hủy vé khi có ghế đã check-in.");
        }
    }

    public void assertItineraryChangeAllowed(
        String ticketStatus,
        List<StaffPassengerTicketRowProjection> seatRows,
        String paymentStatus,
        LocalDateTime departureTime,
        String tripStatus
    ) {
        boolean ticketActive = PassengerTicketStatus.CONFIRMED.name().equals(ticketStatus)
            || PassengerTicketStatus.CHANGED.name().equals(ticketStatus);

        if (!ticketActive) {
            throw new BusinessRuleException("Chỉ được đổi hành trình trên vé đã xác nhận hoặc đã có thay đổi.");
        }

        if (!"COMPLETED".equals(paymentStatus)) {
            throw new BusinessRuleException("Vé chưa thanh toán đầy đủ, không thể đổi hành trình.");
        }

        if (!"SCHEDULED".equals(tripStatus)) {
            throw new BusinessRuleException("Chỉ được đổi hành trình trên chuyến đang lên lịch.");
        }

        if (!canModify(hoursUntilDeparture(departureTime))) {
            throw new BusinessRuleException(
                "Không thể đổi hành trình trong vòng " + CHANGE_CUTOFF_HOURS + " giờ trước giờ khởi hành."
            );
        }

        long confirmedSeats = seatRows.stream()
            .filter(row -> PassengerTicketDetailStatus.CONFIRMED.name().equals(row.getDetailStatus()))
            .count();

        if (confirmedSeats == 0) {
            throw new BusinessRuleException("Không có ghế hợp lệ để đổi hành trình.");
        }
    }

    public void assertTransferTripEligible(
        LocalDateTime newTripDepartureTime,
        String newTripStatus,
        int availableSeats,
        int requiredSeats,
        PassengerTicketMajorChangeType majorChangeType
    ) {
        assertMajorChangeQuotaAvailable(majorChangeType);

        if (!"SCHEDULED".equals(newTripStatus)) {
            throw new BusinessRuleException("Chỉ được chuyển sang chuyến đang lên lịch.");
        }

        if (!canModify(hoursUntilDeparture(newTripDepartureTime))) {
            throw new BusinessRuleException(
                "Chuyến mới phải khởi hành sau ít nhất " + CHANGE_CUTOFF_HOURS + " giờ nữa."
            );
        }

        if (availableSeats < requiredSeats) {
            throw new BusinessRuleException("Chuyến mới không đủ ghế trống cho vé này.");
        }
    }

    public void assertPriceEligible(BigDecimal originalNetPaid, BigDecimal newNetPaid) {
        if (originalNetPaid == null) {
            throw new BusinessRuleException("Không xác định được số tiền đã thanh toán.");
        }
        if (newNetPaid == null) {
            throw new BusinessRuleException("Không thể tính giá vé mới.");
        }
        if (newNetPaid.compareTo(originalNetPaid) > 0) {
            throw new BusinessRuleException(
                "Giá vé mới cao hơn giá đã thanh toán. Chỉ được đổi sang lựa chọn có giá bằng hoặc thấp hơn."
            );
        }
    }

    public void assertChangeSeatAllowed(
        String ticketStatus,
        String detailStatus,
        String paymentStatus,
        LocalDateTime departureTime,
        String tripStatus,
        int currentTripSeatId,
        int newTripSeatId
    ) {
        assertPassengerInfoChangeAllowed(
            ticketStatus, detailStatus, paymentStatus, departureTime, tripStatus
        );

        if (currentTripSeatId == newTripSeatId) {
            throw new BusinessRuleException("Ghế mới phải khác ghế hiện tại.");
        }
    }

    public void assertPassengerInfoChangeAllowed(
        String ticketStatus,
        String detailStatus,
        String paymentStatus,
        LocalDateTime departureTime,
        String tripStatus
    ) {
        boolean ticketActive = PassengerTicketStatus.CONFIRMED.name().equals(ticketStatus)
            || PassengerTicketStatus.CHANGED.name().equals(ticketStatus);

        if (!ticketActive) {
            throw new BusinessRuleException("Chỉ được sửa thông tin hành khách trên vé đã xác nhận.");
        }

        if (!PassengerTicketDetailStatus.CONFIRMED.name().equals(detailStatus)) {
            throw new BusinessRuleException("Chỉ được sửa thông tin ghế đang ở trạng thái đã xác nhận.");
        }

        if (!"COMPLETED".equals(paymentStatus)) {
            throw new BusinessRuleException("Vé chưa thanh toán đầy đủ, không thể sửa thông tin hành khách.");
        }

        if (!"SCHEDULED".equals(tripStatus)) {
            throw new BusinessRuleException("Chỉ được sửa thông tin hành khách trên chuyến đang lên lịch.");
        }

        if (!canModify(hoursUntilDeparture(departureTime))) {
            throw new BusinessRuleException(
                "Không thể sửa thông tin hành khách trong vòng " + CHANGE_CUTOFF_HOURS + " giờ trước giờ khởi hành."
            );
        }
    }

    public List<String> resolveAllowedActions(
        String ticketStatus,
        List<StaffPassengerTicketRowProjection> seatRows,
        LocalDateTime departureTime,
        String paymentStatus,
        String tripStatus,
        PassengerTicketMajorChangeType majorChangeType
    ) {
        List<String> actions = new ArrayList<>();
        long hoursLeft = hoursUntilDeparture(departureTime);
        boolean modifiableWindow = canModify(hoursLeft);
        boolean cancellableWindow = canCancel(hoursLeft);
        boolean paymentCompleted = "COMPLETED".equals(paymentStatus);
        boolean tripScheduled = "SCHEDULED".equals(tripStatus);
        boolean majorChangeAvailable = majorChangeType == null;

        boolean ticketActive = PassengerTicketStatus.CONFIRMED.name().equals(ticketStatus)
            || PassengerTicketStatus.CHANGED.name().equals(ticketStatus);

        if (!ticketActive || !paymentCompleted || !tripScheduled) {
            return actions;
        }

        long confirmedSeats = seatRows.stream()
            .filter(row -> PassengerTicketDetailStatus.CONFIRMED.name().equals(row.getDetailStatus()))
            .count();

        if (confirmedSeats == 0) {
            return actions;
        }

        boolean hasCheckedInSeat = seatRows.stream()
            .anyMatch(row -> PassengerTicketDetailStatus.CHECKED_IN.name().equals(row.getDetailStatus()));

        if (modifiableWindow) {
            boolean hasConfirmedSeat = seatRows.stream()
                .anyMatch(row -> PassengerTicketDetailStatus.CONFIRMED.name().equals(row.getDetailStatus()));
            if (hasConfirmedSeat) {
                actions.add("CHANGE_PASSENGER_INFO");
                actions.add("CHANGE_SEAT");
            }
            if(majorChangeAvailable) actions.add("TRANSFER_TRIP");
        }

        if (majorChangeAvailable && cancellableWindow && !hasCheckedInSeat) {
            actions.add("CANCEL_FULL");
            if (confirmedSeats >= 2) {
                actions.add("CANCEL_PARTIAL");
            }
        }

        return actions;
    }
}
