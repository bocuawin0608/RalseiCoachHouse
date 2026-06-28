package com.ralsei.service.passengerbooking.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.ralsei.dto.request.passengerbooking.BookingConfirmRequest;
import com.ralsei.dto.request.passengerbooking.PassengerDTO;
import com.ralsei.dto.request.passengerbooking.PriceCalculationRequest;
import com.ralsei.dto.request.passengerbooking.SeatLockRequest;
import com.ralsei.dto.request.payment.PaymentCheckoutRequest;
import com.ralsei.dto.response.passengerbooking.BookingConfirmResponse;
import com.ralsei.dto.response.passengerbooking.BookingPaymentPageResponse;
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
import com.ralsei.model.PassengerTicket;
import com.ralsei.model.PassengerTicketDetail;
import com.ralsei.model.Payment;
import com.ralsei.model.RouteStop;
import com.ralsei.model.TripSeat;
import com.ralsei.model.Voucher;
import com.ralsei.model.enums.PassengerTicketDetailStatus;
import com.ralsei.model.enums.PassengerTicketStatus;
import com.ralsei.model.enums.TripSeatStatus;
import com.ralsei.model.enums.VoucherType;
import com.ralsei.repository.AccompaniedChildRepository;
import com.ralsei.repository.CustomerRepository;
import com.ralsei.repository.PassengerTicketDetailRepository;
import com.ralsei.repository.PassengerTicketRepository;
import com.ralsei.repository.RouteStopRepository;
import com.ralsei.repository.TripRepository;
import com.ralsei.repository.TripSeatRepository;
import com.ralsei.service.JwtService;
import com.ralsei.service.PaymentService;
import com.ralsei.service.RouteStopService;
import com.ralsei.service.VoucherService;
import com.ralsei.service.passengerbooking.PassengerBookingService;
import com.ralsei.service.passengerbooking.PaymentSseService;
import com.ralsei.service.passengerbooking.SeatHoldService;
import com.ralsei.service.ticketgenerator.TicketCodeGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassengerBookingServiceImpl implements PassengerBookingService {

    private final TripRepository tripRepo;
    private final TripSeatRepository tripSeatRepo;
    private final RouteStopRepository routeStopRepo;
    private final CustomerRepository customerRepo;
    private final PassengerTicketRepository ticketRepo;
    private final PassengerTicketDetailRepository ticketDetailRepo;
    private final AccompaniedChildRepository accompaniedChildRepo;
    //lười nên vứt con repo ở đây thay vì tạo thêm service

    private final SeatHoldService seatHoldService;
    private final RouteStopService routeStopService;
    private final VoucherService voucherService;
    private final PaymentService paymentService;
    private final PaymentSseService paymentSseService;
    private final TicketCodeGenerator ticketCodeGenerator;
    private final JwtService jwtService;

    private static final long BROWSING_HOLD_TTL_SECONDS = 600;
    private static final long PAYMENT_HOLD_TTL_SECONDS = 300;
    private static final String BANK_TRANSFER_METHOD = "SEPAY";

    @Value("${sepay.bank.account}")
    private String sepayBankAccount;

    @Value("${sepay.bank.name}")
    private String sepayBankName;
    
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
        Voucher voucherToBeUsed,
        Map<Integer, TripSeat> tripSeatMap
    ) {}

    private CoreCalculationResult performCorePriceCalculation(
        Integer tripId, String holdToken, Integer pickupStopId, 
        Integer dropoffStopId, Integer voucherId, String accessToken
    ) {
        Integer routeId = tripRepo.findById(tripId).map(trip -> trip.getRouteId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến xe có ID là: " + tripId));
        //TODO: check kỹ hơn tripId nào với status nào, departure time (now-8?) nào còn đc đặt vé

        List<Integer> tripSeatIdsBooking = seatHoldService.getTripSeatIdsByToken(holdToken);
        if (tripSeatIdsBooking == null || tripSeatIdsBooking.isEmpty()) {
            throw new BusinessRuleException("Phiên giữ ghế đã hết hạn hoặc không hợp lệ. Vui lòng chọn lại ghế!");
        }

        List<TripSeat> bookedSeats = tripSeatRepo.findByTripIdAndTripSeatIdInWithSeat(tripId, tripSeatIdsBooking);

        if (bookedSeats.size() != tripSeatIdsBooking.size()) {
            throw new BusinessRuleException("Có ghế ngồi không hợp lệ hoặc đã bị thay đổi! Vui lòng chọn lại!");
        }

        // Kiểm tra trạng thái AVAILABLE từ DB (vì trạng thái LOCK chỉ lưu ở Redis)
        boolean isAllAvailable = bookedSeats.stream()
            .allMatch(seat -> TripSeatStatus.AVAILABLE.equals(seat.getStatus()));
        if (!isAllAvailable) {
            throw new BusinessRuleException("Có ghế ngồi đã được đặt bởi người khác! Vui lòng chọn lại!");
        }

        BigDecimal basePrice = bookedSeats.get(0).getPrice();
        if (basePrice == null) {
            throw new ResourceNotFoundException("Không tìm thấy thông tin giá của ghế này!");
        }

        BigDecimal baseSurcharge = BigDecimal.ZERO;
        CoachStop pickupStop = null;
        CoachStop dropoffStop = null;

        if (pickupStopId != null && dropoffStopId != null) {
            if (pickupStopId.equals(dropoffStopId)) {
                throw new BusinessRuleException("Điểm đón và điểm trả không được trùng nhau!");
            }
            
            RouteStop pickupRouteStop = routeStopRepo.findByRouteIdAndStopPointId(routeId, pickupStopId)
                .orElseThrow(() -> new BusinessRuleException("Điểm đón không hợp lệ hoặc không thuộc tuyến đường này!"));
                
            RouteStop dropoffRouteStop = routeStopRepo.findByRouteIdAndStopPointId(routeId, dropoffStopId)
                .orElseThrow(() -> new BusinessRuleException("Điểm trả không hợp lệ hoặc không thuộc tuyến đường này!"));

            if (pickupRouteStop.getStopOrder() >= dropoffRouteStop.getStopOrder()) {
                throw new BusinessRuleException("Lộ trình không hợp lệ: Điểm đón phải nằm trước điểm trả!");
            }

            pickupStop = pickupRouteStop.getCoachStop();
            dropoffStop = dropoffRouteStop.getCoachStop();

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

        Map<Integer, TripSeat> tripSeatMap = bookedSeats.stream()
            .collect(Collectors.toMap(tripSeat -> tripSeat.getTripSeatId(), Function.identity()));

        return new CoreCalculationResult(
            tripSeatIdsBooking, 
            basePrice, 
            baseSurcharge, 
            totalRawPrice, 
            discountAmount, 
            finalPrice.compareTo(BigDecimal.ZERO) >= 0 ? finalPrice : BigDecimal.ZERO, 
            pickupStop, 
            dropoffStop, 
            voucherToBeUsed,
            tripSeatMap
        );
    }

    @Transactional
    @Override
    public BookingConfirmResponse confirmBooking(Integer tripId, BookingConfirmRequest request, String holdToken, String accessToken) {
        
        CoreCalculationResult coreResult = performCorePriceCalculation(
            tripId, holdToken, request.pickupStopId(), 
            request.dropoffStopId(), request.voucherId(), accessToken
        );

        Integer customerId = null;
        if (accessToken != null && !accessToken.isBlank()) {
            Integer accountId = jwtService.extractAccountId(accessToken);
            if (accountId != null) {
                customerId = customerRepo.findByAccountId(accountId)
                    .map(customer -> customer.getCustomerId())
                    .orElse(null);
            }
        }

        String voucherCodeSnapshot = null;
        if (coreResult.voucherToBeUsed() != null && coreResult.discountAmount().compareTo(BigDecimal.ZERO) > 0) {
            int rowsUpdated = voucherService.incrementUsedCountIfAvailable(coreResult.voucherToBeUsed().getVoucherId());
            if (rowsUpdated == 0) {
                throw new BusinessRuleException("Voucher vừa hết lượt sử dụng! Vui lòng chọn lại.");
            }
            voucherCodeSnapshot = coreResult.voucherToBeUsed().getVoucherCode();
        }

        Set<Integer> lockedSeatIds = Set.copyOf(coreResult.lockedSeatIds());
        Set<Integer> requestedSeatIds = request.passengers().stream()
            .map(passenger -> passenger.tripSeatId())
            .collect(Collectors.toSet());
        if (requestedSeatIds.size() != request.passengers().size() || !requestedSeatIds.equals(lockedSeatIds)) {
            throw new BusinessRuleException("Danh sách hành khách không khớp với ghế đang giữ!");
        }

        Map<Integer, TripSeat> tripSeatById = coreResult.tripSeatMap();

        String ticketCode = ticketCodeGenerator.generatePassengerTicketCode(); 
        PassengerTicket ticket = PassengerTicket.builder()
            .customerId(customerId)
            .tripId(tripId)
            .ticketCode(ticketCode)
            .totalPrice(coreResult.totalFinalPrice())
            .voucherId(request.voucherId())
            .pickupStopId(coreResult.pickupStop().getStopPointId())
            .dropoffStopId(coreResult.dropoffStop().getStopPointId())
            .pickupStopName(coreResult.pickupStop().getStopPointName())
            .dropoffStopName(coreResult.dropoffStop().getStopPointName())
            .voucherCodeSnapshot(voucherCodeSnapshot)
            .status(PassengerTicketStatus.PENDING)
            .build();
        PassengerTicket savedTicket = ticketRepo.save(ticket);

        LocalDateTime expiredAt = LocalDateTime.now().plusSeconds(PAYMENT_HOLD_TTL_SECONDS); 
        BigDecimal pricePerSeat = coreResult.basePrice().add(coreResult.baseSurcharge());

        for (PassengerDTO passenger : request.passengers()) {
            PassengerTicketDetail detail = PassengerTicketDetail.builder()
                .passengerTicketId(savedTicket.getPassengerTicketId())
                .tripSeatId(passenger.tripSeatId())
                .seatCodeSnapshot(tripSeatById.get(passenger.tripSeatId()).getSeat().getSeatCode())
                .fullName(passenger.fullname())
                .phone(passenger.phone())
                .email(passenger.email())
                .price(pricePerSeat)
                .status(PassengerTicketDetailStatus.PENDING.name())
                .expiredAt(expiredAt)
                .build();
            PassengerTicketDetail savedDetail = ticketDetailRepo.save(detail);

            if (passenger.accompaniedChild() != null) {
                AccompaniedChild child = AccompaniedChild.builder()
                    .ticketDetailId(savedDetail.getTicketDetailId())
                    .fullname(passenger.accompaniedChild().fullname())
                    .birthYear(passenger.accompaniedChild().birthYear())
                    .build();
                accompaniedChildRepo.save(child);
            }
        }

        PaymentCheckoutRequest paymentRequest = new PaymentCheckoutRequest(
            savedTicket.getPassengerTicketId(),
            null, // cargoTicketId = null
            coreResult.totalFinalPrice(),
            BANK_TRANSFER_METHOD
        );
        Payment payment = paymentService.initializePayment(paymentRequest);

        seatHoldService.extendLock(coreResult.lockedSeatIds(), holdToken, PAYMENT_HOLD_TTL_SECONDS);

        return new BookingConfirmResponse(
            savedTicket.getTicketCode(),
            payment.getTransactionId(),
            coreResult.totalFinalPrice(),
            sepayBankAccount, 
            sepayBankName,
            expiredAt
        );
    }

    @Transactional(readOnly = true)
    @Override
    public BookingPaymentPageResponse getPaymentPage(String transactionId) {
        Payment payment = paymentService.getPaymentByTransactionId(transactionId);
        if (payment.getPassengerTicketId() == null) {
            throw new ResourceNotFoundException("Không tìm thấy thông tin thanh toán vé hành khách!");
        }

        PassengerTicket ticket = ticketRepo.findById(payment.getPassengerTicketId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé với mã giao dịch: " + transactionId));

        List<PassengerTicketDetail> details = ticketDetailRepo.findByPassengerTicketId(ticket.getPassengerTicketId());
        if (details.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy chi tiết vé!");
        }

        PassengerTicketDetail primaryDetail = details.get(0);
        List<String> seatCodes = details.stream()
            .map(ticketDetail -> ticketDetail.getSeatCodeSnapshot())
            .toList();

        return new BookingPaymentPageResponse(
            ticket.getTicketCode(),
            payment.getTransactionId(),
            payment.getAmount(),
            sepayBankAccount,
            sepayBankName,
            primaryDetail.getExpiredAt(),
            payment.getStatus(),
            primaryDetail.getFullName(),
            primaryDetail.getPhone(),
            seatCodes,
            ticket.getTripId()
        );
    }

    @Transactional
    @Override
    public void expirePendingPaymentIfOverdue(String transactionId) {
        Payment payment;
        try {
            payment = paymentService.getPaymentByTransactionId(transactionId);
        } catch (IllegalArgumentException ex) {
            return;
        }

        if (!"PENDING".equals(payment.getStatus()) || payment.getPassengerTicketId() == null) {
            return;
        }

        List<PassengerTicketDetail> details = ticketDetailRepo.findByPassengerTicketId(payment.getPassengerTicketId());
        if (details.isEmpty()) {
            return;
        }

        LocalDateTime expiredAt = details.get(0).getExpiredAt();
        if (expiredAt != null && LocalDateTime.now().isAfter(expiredAt)) {
            paymentService.cancelPayment(transactionId);
        }
    }

    @Override
    public SseEmitter subscribePaymentStatus(String transactionId) {
        Payment payment = paymentService.getPaymentByTransactionId(transactionId);
        SseEmitter emitter = paymentSseService.createConnection(transactionId);
        paymentSseService.sendStatusUpdate(transactionId, payment.getStatus());
        return emitter;
    }

}