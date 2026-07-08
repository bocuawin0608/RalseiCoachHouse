package com.ralsei.service.passengerticket;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Voucher;
import com.ralsei.model.enums.VoucherType;
import com.ralsei.repository.RouteStopRepository;
import com.ralsei.repository.TripRepository;
import com.ralsei.repository.TripSeatRepository;
import com.ralsei.service.VoucherService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StaffTicketItineraryPriceCalculator {

    private final TripRepository tripRepository;
    private final TripSeatRepository tripSeatRepository;
    private final RouteStopRepository routeStopRepository;
    private final VoucherService voucherService;

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
        Integer voucherId
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
        BigDecimal discountAmount = calculateDiscount(voucherId, totalRaw);
        BigDecimal netPaid = totalRaw.subtract(discountAmount).setScale(0, RoundingMode.HALF_UP).max(BigDecimal.ZERO);

        return new PriceBreakdown(basePrice, surcharge, totalRaw, discountAmount, netPaid);
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

    private BigDecimal calculateDiscount(Integer voucherId, BigDecimal totalRaw) {
        if (voucherId == null) {
            return BigDecimal.ZERO;
        }

        Voucher voucher = voucherService.getEligibleVoucher(voucherId, totalRaw);
        if (voucher == null) {
            throw new BusinessRuleException("Mã giảm giá không còn hợp lệ với giá vé mới.");
        }

        BigDecimal discount = VoucherType.FIXED.getValue().equals(voucher.getDiscountType())
            ? voucher.getDiscountValue()
            : totalRaw.multiply(voucher.getDiscountValue())
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        if (voucher.getMaxDiscountValue() != null) {
            discount = discount.min(voucher.getMaxDiscountValue());
        }
        return discount.min(totalRaw);
    }
}
