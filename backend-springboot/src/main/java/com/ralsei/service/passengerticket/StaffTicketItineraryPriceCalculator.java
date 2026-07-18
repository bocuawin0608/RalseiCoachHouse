package com.ralsei.service.passengerticket;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.repository.RouteStopRepository;
import com.ralsei.repository.TripRepository;
import com.ralsei.repository.TripSeatRepository;

import lombok.RequiredArgsConstructor;

/**
 * Prices a proposed itinerary change for an already-paid passenger ticket.
 * Voucher eligibility is not re-evaluated here: the discount applied at booking
 * is treated as a reserved amount and subtracted from the new raw fare.
 */
@Component
@RequiredArgsConstructor
public class StaffTicketItineraryPriceCalculator {

    private final TripRepository tripRepository;
    private final TripSeatRepository tripSeatRepository;
    private final RouteStopRepository routeStopRepository;

    public record PriceBreakdown(
        BigDecimal basePrice,
        BigDecimal surcharge,
        BigDecimal totalRaw,
        BigDecimal discountAmount,
        BigDecimal netPaid
    ) {}

    public PriceBreakdown calculateNetPaid(
        Integer tripId,
        Integer pickupStopId,
        Integer dropoffStopId,
        int seatCount,
        BigDecimal reservedDiscountAmount
    ) {
        if (seatCount <= 0) {
            throw new BusinessRuleException("Vé không có ghế hợp lệ để tính giá.");
        }

        Integer routeId = tripRepository.findById(tripId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến xe."))
            .getRouteId();

        BigDecimal basePrice = tripSeatRepository.findFirstSeatPriceByTripId(tripId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin giá ghế của chuyến xe."));

        BigDecimal surcharge = calculateSurcharge(routeId, pickupStopId, dropoffStopId);
        BigDecimal totalRaw = basePrice.add(surcharge).multiply(BigDecimal.valueOf(seatCount));
        BigDecimal discountAmount = resolveReservedDiscount(reservedDiscountAmount, totalRaw);
        BigDecimal netPaid = totalRaw.subtract(discountAmount).setScale(0, RoundingMode.HALF_UP).max(BigDecimal.ZERO);

        return new PriceBreakdown(basePrice, surcharge, totalRaw, discountAmount, netPaid);
    }

    private BigDecimal resolveReservedDiscount(BigDecimal reservedDiscountAmount, BigDecimal totalRaw) {
        if (reservedDiscountAmount == null || reservedDiscountAmount.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return reservedDiscountAmount.min(totalRaw);
    }

    private BigDecimal calculateSurcharge(Integer routeId, Integer pickupStopId, Integer dropoffStopId) {
        if (pickupStopId == null || dropoffStopId == null) {
            throw new BusinessRuleException("Vui lòng chọn điểm đón và điểm trả.");
        }
        if (pickupStopId.equals(dropoffStopId)) {
            throw new BusinessRuleException("Điểm đón và điểm trả không được trùng nhau.");
        }

        var pickup = routeStopRepository.findByRouteIdAndStopPointId(routeId, pickupStopId)
            .orElseThrow(() -> new BusinessRuleException("Điểm đón không hợp lệ trên tuyến này."));
        var dropoff = routeStopRepository.findByRouteIdAndStopPointId(routeId, dropoffStopId)
            .orElseThrow(() -> new BusinessRuleException("Điểm trả không hợp lệ trên tuyến này."));

        if (pickup.getStopOrder() >= dropoff.getStopOrder()) {
            throw new BusinessRuleException("Lộ trình không hợp lệ: điểm đón phải nằm trước điểm trả.");
        }

        BigDecimal surcharge = BigDecimal.ZERO;
        if (pickup.getCoachStop().getSurcharge() != null) {
            surcharge = surcharge.add(pickup.getCoachStop().getSurcharge());
        }
        if (dropoff.getCoachStop().getSurcharge() != null) {
            surcharge = surcharge.add(dropoff.getCoachStop().getSurcharge());
        }
        return surcharge;
    }
}
