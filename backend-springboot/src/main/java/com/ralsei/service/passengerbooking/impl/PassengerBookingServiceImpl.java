package com.ralsei.service.passengerbooking.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.request.passengerbooking.PriceCalculationRequest;
import com.ralsei.dto.request.passengerbooking.SeatLockRequest;
import com.ralsei.dto.response.passengerbooking.CoachStopDropdownDTO;
import com.ralsei.dto.response.passengerbooking.PriceCalculationResponse;
import com.ralsei.dto.response.passengerbooking.SeatLockResponse;
import com.ralsei.dto.response.passengerbooking.Step2InitResponse;
import com.ralsei.dto.response.passengerbooking.TripSeatResponse;
import com.ralsei.dto.response.passengerbooking.VoucherDTO;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.CoachStop;
import com.ralsei.model.RouteStop;
import com.ralsei.model.Voucher;
import com.ralsei.model.enums.PassengerTicketStatus;
import com.ralsei.model.enums.TripSeatStatus;
import com.ralsei.model.enums.VoucherType;
import com.ralsei.repository.CoachStopRepository;
import com.ralsei.repository.PassengerTicketRepository;
import com.ralsei.repository.TripRepository;
import com.ralsei.repository.TripSeatRepository;
import com.ralsei.service.JwtService;
import com.ralsei.service.RouteStopService;
import com.ralsei.service.VoucherService;
import com.ralsei.service.passengerbooking.PassengerBookingService;
import com.ralsei.service.passengerbooking.SeatHoldService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassengerBookingServiceImpl implements PassengerBookingService {

    private final TripRepository tripRepo;
    private final TripSeatRepository tripSeatRepo;
    private final CoachStopRepository coachStopRepo;
    private final PassengerTicketRepository ticketRepo;
    //lười nên vứt con repo ở đây thay vì tạo thêm service

    private final SeatHoldService seatHoldService;
    private final RouteStopService routeStopService;
    private final VoucherService voucherService;
    private final JwtService jwtService;

    private static final long BROWSING_HOLD_TTL_SECONDS = 600;
    
    @Transactional(readOnly = true)
    @Override
    public List<TripSeatResponse> getSeatMap(Integer tripId) {
        if(!tripRepo.existsById(tripId)) {
            throw new ResourceNotFoundException("Không tìm thấy chuyến xe có ID là: " + tripId);
        }
        //TODO: check kỹ hơn tripId nào với status nào, departure time (now-8?) nào còn đc đặt vé
        
        List<TripSeatResponse> tripSeats = tripSeatRepo.getSeatMap(tripId);
        
        return tripSeats.stream().map(seat -> {
            return seatHoldService.isSeatLocked(seat.tripSeatId()) 
                ? seat.toBuilder().status(TripSeatStatus.LOCKED).build() : seat;
        }).toList();
    }

    @Transactional
    @Override
    public SeatLockResponse lockSeats(Integer tripId, SeatLockRequest request, String holdToken) {
        //TODO: check kỹ hơn tripId nào với status nào, departure time (now-8?) nào còn đc đặt vé

        List<Integer> availableSeatIds = tripSeatRepo.findTripSeatIdsByTripIdAndStatus(tripId, TripSeatStatus.AVAILABLE);
        if(availableSeatIds.isEmpty() || !availableSeatIds.containsAll(request.tripSeatIds())) {
            throw new BusinessRuleException("Có ghế ngồi không hợp lệ hoặc đã được đặt! Vui lòng chọn lại!");
        }
        
        if(!seatHoldService.lockSeats(request.tripSeatIds(), holdToken, BROWSING_HOLD_TTL_SECONDS)) {
            throw new BusinessRuleException("Có ghế ngồi đã được đặt! Vui lòng chọn lại!");
        }

        return new SeatLockResponse(
            request.tripSeatIds(),
            holdToken,
            LocalDateTime.now().plusSeconds(BROWSING_HOLD_TTL_SECONDS)
        );
    }

    @Transactional
    @Override
    public boolean releaseSeats(List<Integer> tripSeatIds, String holdToken) {
        if (tripSeatIds == null || tripSeatIds.isEmpty()) {
            return false; 
        }

        try {
            seatHoldService.releaseSeats(tripSeatIds, holdToken);
            return true;
        } catch (Exception e) {
            log.error("Có lỗi xảy ra khi dọn dẹp giải phóng ghế: ", e);
            return false;
        }
    }

    @Transactional
    @Override
    public boolean releaseSeatsByBecon(String holdToken) {
        if(holdToken == null || holdToken.isBlank()) {
            return false;
        }
            
        List<Integer> tripSeatIds = seatHoldService.getTripSeatIdsByToken(holdToken);
        return releaseSeats(tripSeatIds, holdToken);
    }

    private List<CoachStopDropdownDTO> getStopPointDropdown(List<RouteStop> stopPointList, String province){
        return stopPointList.stream().filter(stop -> stop.getCoachStop().getCity().trim().equalsIgnoreCase(province)).map(stop -> new CoachStopDropdownDTO(
            stop.getCoachStop().getStopPointId(), stop.getCoachStop().getStopPointName()
        )).toList();
    }

    private List<VoucherDTO> getEligibleVouchers(String accessToken) {
        Integer accountId = jwtService.extractAccountId(accessToken);
        List<VoucherDTO> eligibleVouchers = new ArrayList<>();
        if(accountId != null) {
            List<VoucherDTO> availableVouchers = voucherService.getEligibleVouchers();
            Set<Integer> usedVoucherIds = ticketRepo.getUsedVoucherIdsByAccountId(accountId, PassengerTicketStatus.CANCELLED);
            eligibleVouchers = availableVouchers.stream().filter(voucher -> !usedVoucherIds.contains(voucher.voucherId())).toList();
        }
        return eligibleVouchers;
    }

    @Transactional(readOnly = true)
    @Override
    public Step2InitResponse getStep2InitData(Integer tripId, String holdToken, String accessToken) {
        if(!tripRepo.existsById(tripId)) {
            throw new ResourceNotFoundException("Không tìm thấy chuyến xe có ID là: " + tripId);
        }
        //TODO: check kỹ hơn tripId nào với status nào, departure time (now-8?) nào còn đc đặt vé

        List<RouteStop> routeStops = routeStopService.getStopsByTripId(tripId);
        if(routeStops == null || routeStops.isEmpty()) {
            throw new BusinessRuleException("Có lỗi lấy dữ liệu chuyến xe. Vui lòng liên hệ nhà xe!");
        }

        String firstProvince = routeStops.get(0).getCoachStop().getCity().trim();
        String lastProvince = routeStops.get(routeStops.size() - 1).getCoachStop().getCity().trim();
        List<CoachStopDropdownDTO> pickupPoints = getStopPointDropdown(routeStops, firstProvince);
        List<CoachStopDropdownDTO> dropoffPoints = getStopPointDropdown(routeStops, lastProvince);

        List<VoucherDTO> eligibleVouchers = getEligibleVouchers(accessToken);

        PriceCalculationResponse priceData = calculatePrice(tripId, new PriceCalculationRequest(holdToken, null, null, null), accessToken);

        return new Step2InitResponse(
            pickupPoints,
            dropoffPoints,
            eligibleVouchers,
            priceData.totalRawPrice(),
            priceData.basePrice()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public PriceCalculationResponse calculatePrice(Integer tripId, PriceCalculationRequest request, String accessToken) {

        List<Integer> tripSeatIdsBooking = seatHoldService.getTripSeatIdsByToken(request.holdToken());
        if (tripSeatIdsBooking == null || tripSeatIdsBooking.isEmpty()) {
            throw new BusinessRuleException("Phiên giữ ghế đã hết hạn hoặc không hợp lệ. Vui lòng chọn lại ghế!");
        }
        
        List<Integer> availableSeatIds = tripSeatRepo.findTripSeatIdsByTripIdAndStatus(tripId, TripSeatStatus.AVAILABLE);
        if(availableSeatIds.isEmpty() || !availableSeatIds.containsAll(tripSeatIdsBooking)) {
            throw new BusinessRuleException("Có ghế ngồi không hợp lệ hoặc đã được đặt! Vui lòng chọn lại!");
        }

        BigDecimal basePrice = tripSeatRepo.findById(tripSeatIdsBooking.get(0))
            .map(tripSeat -> tripSeat.getPrice())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin giá của ghế này!"));

        BigDecimal baseSurcharge = BigDecimal.ZERO;
        if (request.pickupStopId() != null && request.dropoffStopId() != null) {
            // dính líu DB nên tạm xử lý bằng cách này
            List<CoachStop> stopPointsFound = coachStopRepo.findAllById(Arrays.asList(request.dropoffStopId(), request.pickupStopId()));
            if (request.pickupStopId().equals(request.dropoffStopId()) || stopPointsFound.size() != 2) {
                throw new BusinessRuleException("Điểm đón hoặc điểm trả không hợp lệ!");
            }
            for (CoachStop point : stopPointsFound) {
                if (point.getStopPointName() != null && point.getStopPointName().toLowerCase().contains("sân bay nội bài")) {
                    baseSurcharge = baseSurcharge.add(new BigDecimal("100000"));
                }
            }
        }

        BigDecimal totalRawPrice = basePrice.add(baseSurcharge).multiply(BigDecimal.valueOf(tripSeatIdsBooking.size()));

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.voucherId() != null) {
            Voucher voucherToBeUsed = voucherService.getEligibleVoucher(request.voucherId(), totalRawPrice);
            if (voucherToBeUsed == null) {
                throw new BusinessRuleException("Mã giảm giá không hợp lệ hoặc không đáp ứng điều kiện đơn hàng!");
            }
            Integer accountId = jwtService.extractAccountId(accessToken);
            if (accountId != null) {
                Set<Integer> usedVoucherIds = ticketRepo.getUsedVoucherIdsByAccountId(accountId, PassengerTicketStatus.CANCELLED);
                if (usedVoucherIds.contains(voucherToBeUsed.getVoucherId())) {
                    throw new BusinessRuleException("Không thành công, voucher này đã được sử dụng!");
                }
            }
            discountAmount = voucherToBeUsed.getDiscountType().equals(VoucherType.FIXED.getValue()) 
                ? voucherToBeUsed.getDiscountValue() 
                : totalRawPrice.multiply(voucherToBeUsed.getDiscountValue()).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            
            if (voucherToBeUsed.getMaxDiscountValue() != null && discountAmount.compareTo(voucherToBeUsed.getMaxDiscountValue()) > 0) {
                discountAmount = voucherToBeUsed.getMaxDiscountValue();
            }

            if (discountAmount.compareTo(totalRawPrice) > 0) {
                discountAmount = totalRawPrice;
            }
        }
        
        BigDecimal finalPrice = totalRawPrice.subtract(discountAmount).setScale(0, RoundingMode.HALF_UP);

        return new PriceCalculationResponse(
            basePrice,
            baseSurcharge,
            totalRawPrice,
            discountAmount,
            finalPrice.compareTo(BigDecimal.ZERO) >= 0 ? finalPrice : BigDecimal.ZERO
        );
    }

}
