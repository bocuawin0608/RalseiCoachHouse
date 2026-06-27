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

import com.ralsei.dto.request.passengerbooking.BookingConfirmRequest;
import com.ralsei.dto.request.passengerbooking.PassengerDTO;
import com.ralsei.dto.request.passengerbooking.PriceCalculationRequest;
import com.ralsei.dto.request.passengerbooking.SeatLockRequest;
import com.ralsei.dto.response.passengerbooking.BookingConfirmResponse;
import com.ralsei.dto.response.passengerbooking.CoachStopDropdownDTO;
import com.ralsei.dto.response.passengerbooking.PriceCalculationResponse;
import com.ralsei.dto.response.passengerbooking.SeatLockResponse;
import com.ralsei.dto.response.passengerbooking.Step2InitResponse;
import com.ralsei.dto.response.passengerbooking.TripSeatResponse;
import com.ralsei.dto.response.passengerbooking.VoucherDTO;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.AccompaniedChild;
import com.ralsei.model.CoachStop;
import com.ralsei.model.Customer;
import com.ralsei.model.PassengerTicket;
import com.ralsei.model.PassengerTicketDetail;
import com.ralsei.model.Payment;
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

        CoreCalculationResult coreResult = performCorePriceCalculation(
            tripId, request.holdToken(), request.pickupStopId(), 
            request.dropoffStopId(), request.voucherId(), accessToken
        );

        return new PriceCalculationResponse(
            coreResult.basePrice(),
            coreResult.baseSurcharge(),
            coreResult.totalRawPrice(),
            coreResult.discountAmount(),
            coreResult.totalFinalPrice()
        );
    }

    private record CoreCalculationResult(
        List<Integer> lockedSeatIds,
        BigDecimal basePrice,
        BigDecimal baseSurcharge,
        BigDecimal totalRawPrice,
        BigDecimal discountAmount,
        BigDecimal totalFinalPrice,
        CoachStop pickupStop,
        CoachStop dropoffStop,
        Voucher voucherToBeUsed
    ) {}

    private CoreCalculationResult performCorePriceCalculation(
        Integer tripId, String holdToken, Integer pickupStopId, 
        Integer dropoffStopId, Integer voucherId, String accessToken
    ) {
        //TODO: check kỹ hơn tripId nào với status nào, departure time (now-8?) nào còn đc đặt vé. Chú ý, ở hàm step2InitData() có check tripId, nếu mà chỗ này check ngon hơn thì xóa chỗ check tripId đó ở hàm kia đi, dùng cái này là ổn r
        List<Integer> tripSeatIdsBooking = seatHoldService.getTripSeatIdsByToken(holdToken);
        if (tripSeatIdsBooking == null || tripSeatIdsBooking.isEmpty()) {
            throw new BusinessRuleException("Phiên giữ ghế đã hết hạn hoặc không hợp lệ. Vui lòng chọn lại ghế!");
        }

        List<Integer> availableSeatIds = tripSeatRepo.findTripSeatIdsByTripIdAndStatus(tripId, TripSeatStatus.AVAILABLE);
        //bản chất là status LOCK ko đổ database, nên ở dưới đó nó vẫn là status available thôi
        if (availableSeatIds.isEmpty() || !availableSeatIds.containsAll(tripSeatIdsBooking)) {
            throw new BusinessRuleException("Có ghế ngồi không hợp lệ hoặc đã được đặt! Vui lòng chọn lại!");
        }

        BigDecimal basePrice = tripSeatRepo.findById(tripSeatIdsBooking.get(0))
            .map(tripSeat -> tripSeat.getPrice())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin giá của ghế này!"));

        BigDecimal baseSurcharge = BigDecimal.ZERO;
        CoachStop pickupStop = null;
        CoachStop dropoffStop = null;

        if (pickupStopId != null && dropoffStopId != null) {
            if (pickupStopId.equals(dropoffStopId)) {
                throw new BusinessRuleException("Điểm đón và điểm trả không được trùng nhau!");
            }
            
            pickupStop = coachStopRepo.findById(pickupStopId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy điểm đón hợp lệ!"));
            dropoffStop = coachStopRepo.findById(dropoffStopId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy điểm trả hợp lệ!"));

            if (pickupStop.getSurcharge() != null) {
                baseSurcharge = baseSurcharge.add(pickupStop.getSurcharge());
            }
            if (dropoffStop.getSurcharge() != null) {
                baseSurcharge = baseSurcharge.add(dropoffStop.getSurcharge());
            }
        }

        BigDecimal totalRawPrice = basePrice.add(baseSurcharge).multiply(BigDecimal.valueOf(tripSeatIdsBooking.size()));

        BigDecimal discountAmount = BigDecimal.ZERO;
        Voucher voucherToBeUsed = null;

        if (voucherId != null) {
            voucherToBeUsed = voucherService.getEligibleVoucher(voucherId, totalRawPrice);
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

        return new CoreCalculationResult(
            tripSeatIdsBooking, 
            basePrice, 
            baseSurcharge, 
            totalRawPrice, 
            discountAmount, 
            finalPrice.compareTo(BigDecimal.ZERO) >= 0 ? finalPrice : BigDecimal.ZERO, 
            pickupStop, 
            dropoffStop, 
            voucherToBeUsed
        );
    }

    @Transactional
    @Override
    public BookingConfirmResponse confirmBooking(Integer tripId, BookingConfirmRequest request, String holdToken, String accessToken) {
        // // 1. Chạy Core Helper: Tính toán & Verify, móc luôn Entity lên đây
        // CoreCalculationResult coreResult = performCorePriceCalculation(
        //     tripId, holdToken, request.pickupStopId(), 
        //     request.dropoffStopId(), request.voucherId(), accessToken
        // );

        // // 2. Định danh Account (Khách vãng lai = null)
        // Integer customerId = null;
        // if (accessToken != null && !accessToken.isBlank()) {
        //     Integer accountId = jwtService.extractAccountId(accessToken);
        //     if (accountId != null) {
        //         customerId = customerRepo.findByAccountId(accountId)
        //             .map(Customer::getCustomerId)
        //             .orElse(null);
        //     }
        // }

        // // 3. Xử lý Voucher Concurrent (Atomic Update chống Race Condition)
        // String voucherCodeSnapshot = null;
        // if (coreResult.voucherToBeUsed() != null && coreResult.discountAmount().compareTo(BigDecimal.ZERO) > 0) {
        //     int rowsUpdated = voucherRepo.incrementUsedCountIfAvailable(coreResult.voucherToBeUsed().getVoucherId());
        //     if (rowsUpdated == 0) {
        //         throw new BusinessRuleException("Voucher vừa hết lượt sử dụng! Vui lòng chọn lại.");
        //     }
        //     voucherCodeSnapshot = coreResult.voucherToBeUsed().getVoucherCode();
        // }

        // // 4. Khởi tạo Passenger Ticket (Vé Tổng)
        // String ticketCode = "TK" + System.currentTimeMillis(); 
        // PassengerTicket ticket = PassengerTicket.builder()
        //     .customerId(customerId)
        //     .tripId(tripId)
        //     .ticketCode(ticketCode)
        //     .totalPrice(coreResult.totalFinalPrice())
        //     .voucherId(request.voucherId())
        //     // TẬN DỤNG LUÔN Entity từ CoreResult mà không cần Query lại
        //     .pickupStopId(coreResult.pickupStop().getStopPointId())
        //     .dropoffStopId(coreResult.dropoffStop().getStopPointId())
        //     .pickupStopName(coreResult.pickupStop().getStopPointName())
        //     .dropoffStopName(coreResult.dropoffStop().getStopPointName())
        //     .voucherCodeSnapshot(voucherCodeSnapshot)
        //     .status(PassengerTicketStatus.PENDING.getValue())
        //     .build();
        // PassengerTicket savedTicket = ticketRepo.save(ticket);

        // // 5. Khởi tạo Passenger Ticket Detail & Accompanied Child
        // LocalDateTime expiredAt = LocalDateTime.now().plusSeconds(PAYMENT_HOLD_TTL_SECONDS); 
        // BigDecimal pricePerSeat = coreResult.basePrice().add(coreResult.baseSurcharge());

        // for (PassengerDTO passenger : request.passengers()) {
        //     PassengerTicketDetail detail = PassengerTicketDetail.builder()
        //         .passengerTicketId(savedTicket.getPassengerTicketId())
        //         .tripSeatId(passenger.tripSeatId())
        //         .seatCodeSnapshot(passenger.seatCode())
        //         .fullName(passenger.fullname())
        //         .phone(passenger.phone())
        //         .email(passenger.email())
        //         .price(pricePerSeat)
        //         .status(PassengerTicketStatus.PENDING.getValue())
        //         .expiredAt(expiredAt)
        //         .build();
        //     PassengerTicketDetail savedDetail = ticketDetailRepo.save(detail);

        //     if (passenger.accompaniedChild() != null) {
        //         AccompaniedChild child = AccompaniedChild.builder()
        //             .ticketDetailId(savedDetail.getTicketDetailId())
        //             .fullname(passenger.accompaniedChild().fullname())
        //             .birthYear(passenger.accompaniedChild().birthYear())
        //             .build();
        //         accompaniedChildRepo.save(child);
        //     }
        // }

        // // 6. Giao tiếp với Module Payment (Tạo giao dịch PENDING chờ FE quét QR)
        // PaymentCheckoutRequest paymentRequest = new PaymentCheckoutRequest(
        //     savedTicket.getPassengerTicketId(),
        //     null, // cargoTicketId = null
        //     coreResult.totalFinalPrice(),
        //     "BANK_TRANSFER"
        // );
        // Payment payment = paymentService.initializePayment(paymentRequest);

        // // 7. Gia hạn Redis Lock cho khớp với thời gian chờ quét QR
        // seatHoldService.extendLock(coreResult.lockedSeatIds(), holdToken, PAYMENT_HOLD_TTL_SECONDS);

        // // 8. Trả Response về cho FE render QR
        // return new BookingConfirmResponse(
        //     savedTicket.getTicketCode(),
        //     payment.getTransactionId(),
        //     coreResult.totalFinalPrice(),
        //     sepayBankAccount, 
        //     sepayBankName,
        //     expiredAt
        // );
        return null;
    }

}
