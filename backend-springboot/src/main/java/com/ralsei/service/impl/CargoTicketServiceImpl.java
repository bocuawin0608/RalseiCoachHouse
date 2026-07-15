package com.ralsei.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.request.cargoticket.CargoTicketRequest;
import com.ralsei.dto.request.cargoticket.CargoTicketWithDetailsRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.cargoticket.CargoTicketResponse;
import com.ralsei.dto.response.cargoticket.CargoTicketFormOptionsResponse;
import com.ralsei.dto.response.cargoticket.CustomerContactResponse;
import com.ralsei.dto.response.cargoticketdetail.CargoTicketDetailResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.CargoTicket;
import com.ralsei.model.CargoTicketDetail;
import com.ralsei.model.Payment;
import com.ralsei.repository.CargoTicketRepository;
import com.ralsei.repository.CargoTicketDetailRepository;
import com.ralsei.repository.CoachStopRepository;
import com.ralsei.repository.CustomerRepository;
import com.ralsei.repository.PaymentRepository;
import com.ralsei.repository.RouteRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.repository.TripRepository;
import com.ralsei.service.CargoTicketService;
import com.ralsei.service.TransactionIdGenerator;
import com.ralsei.model.Staff;
import com.ralsei.dto.request.cargoticket.TripByStopRequest;
import com.ralsei.dto.request.cargoticketdetail.CargoTicketDetailRequest;
import com.ralsei.dto.response.cargoticket.TripByStopResponse;
import com.ralsei.dto.response.CoachAndRouteStop.RouteDropdownDTO;
import com.ralsei.repository.CargoTypePriceRepository;
import com.ralsei.model.CargoTypePrice;
import com.ralsei.util.FreightCalculatorUtility;

import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CargoTicketServiceImpl implements CargoTicketService {
    private static final String STATUS_ABANDONED = "ABANDONED";

    private final CargoTicketRepository cargoTicketRepository;
    private final CargoTicketDetailRepository cargoTicketDetailRepository;
    private final TripRepository tripRepository;
    private final CustomerRepository customerRepository;
    private final CoachStopRepository coachStopRepository;
    private final StaffRepository staffRepository;
    private final PaymentRepository paymentRepository;
    private final CargoTypePriceRepository cargoTypePriceRepository;
    private final TransactionIdGenerator transactionIdGenerator;
    private final RouteRepository routeRepository;

    @Override
    public PagedResponse<CargoTicketResponse> getAllCargoTickets(int page, int size) {
        Page<CargoTicket> result = cargoTicketRepository.findByStatusNot(
                STATUS_ABANDONED,
                PageRequest.of(page, size, Sort.by("cargoTicketId").descending()));
        return new PagedResponse<>(
                result.map(this::mapToResponse).getContent(),
                result.getNumber(), result.getSize(), result.getTotalElements(),
                result.getTotalPages(), result.isLast());
    }

    @Override
    public CargoTicketFormOptionsResponse getFormOptions(Integer pickupStopId, Integer dropoffStopId) {
        return CargoTicketFormOptionsResponse.builder()
                .routes(routeRepository.findRoutesForDropdown())
                .trips(pickupStopId != null && dropoffStopId != null && !pickupStopId.equals(dropoffStopId)
                        ? tripRepository.findCargoTicketTripOptionsWithCoachType(pickupStopId, dropoffStopId)
                        : List.of())
                .customers(customerRepository.findCargoTicketCustomerOptions())
                .stops(coachStopRepository.findCargoTicketStopOptions())
                .sellers(staffRepository.findCargoTicketSellerOptionsWithUsername())
                .handlers(staffRepository.findCargoTicketHandlerOptions())
                .drivers(staffRepository.findCargoTicketDriverOptions())
                .build();
    }

    @Override
    public CargoTicketResponse getCargoTicketById(int id) {
        return mapToResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional
    public CargoTicketResponse createCargoTicket(CargoTicketRequest request) {
        String ticketCode = generateTicketCode();
        validateReferences(request);

        CargoTicket ticket = new CargoTicket();
        copyRequest(request, ticket, ticketCode);
        CargoTicket savedTicket = cargoTicketRepository.save(ticket);

        Payment payment = Payment.builder()
                .cargoTicket(savedTicket)
                .amount(savedTicket.getTotalPrice())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(transactionIdGenerator.generateUniqueTransactionId())
                .status("PENDING")
                .refundAmount(BigDecimal.ZERO)
                .build();
        paymentRepository.save(payment);

        return mapToResponse(savedTicket);
    }

    @Override
    @Transactional
    public CargoTicketResponse createCargoTicketWithDetails(CargoTicketWithDetailsRequest request) {
        // Calculate the prices first to satisfy the Payment DB constraint (> 0)
        List<CargoTicketDetail> mappedDetails = new java.util.ArrayList<>();
        BigDecimal preCalculatedTotal = BigDecimal.ZERO;

        for (var d : request.getDetails()) {
            BigDecimal calcPrice = calculateDetailPrice(d.getCargoTypePriceId(), d.getDimensionVol(), d.getQuantity());
            preCalculatedTotal = preCalculatedTotal.add(calcPrice);

            mappedDetails.add(CargoTicketDetail.builder()
                    .cargoTypePriceId(d.getCargoTypePriceId())
                    .description(d.getDescription())
                    .quantity(d.getQuantity())
                    .weightKg(d.getWeightKg())
                    .dimensionVol(d.getDimensionVol())
                    .calculatedPrice(calcPrice)
                    .build());
        }

        // Pass the pre-calculated total so the Payment row can be inserted successfully
        request.setTotalPrice(preCalculatedTotal);
        CargoTicketResponse response = createCargoTicket(request);

        CargoTicket ticket = cargoTicketRepository.findById(response.getCargoTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn gửi hàng."));

        for (CargoTicketDetail detail : mappedDetails) {
            detail.setCargoTicket(ticket);
        }

        cargoTicketDetailRepository.saveAll(mappedDetails);

        // Ensure flushed to DB so updateTicketTotalPrice can read them
        cargoTicketDetailRepository.flush();
        updateTicketTotalPrice(ticket);

        return mapToResponse(ticket);
    }

    @Override
    public List<CargoTicketDetailResponse> getCargoTicketDetailsByTicketId(int cargoTicketId) {
        List<CargoTicketDetail> details = cargoTicketDetailRepository.findByCargoTicket_CargoTicketId(cargoTicketId);
        return details.stream().map(d -> CargoTicketDetailResponse.builder()
                .cargoTicketDetailId(d.getCargoTicketDetailId())
                .cargoTicketId(d.getCargoTicket().getCargoTicketId())
                .cargoTypePriceId(d.getCargoTypePriceId())
                .description(d.getDescription())
                .quantity(d.getQuantity())
                .weightKg(d.getWeightKg())
                .dimensionVol(d.getDimensionVol())
                .calculatedPrice(d.getCalculatedPrice())
                .build()).toList();
    }

    @Override
    @Transactional
    public CargoTicketResponse updateCargoTicket(int id, CargoTicketRequest request) {
        CargoTicket ticket = findByIdOrThrow(id);
        String ticketCode = ticket.getTicketCode();
        validateReferences(request);

        copyRequest(request, ticket, ticketCode);
        CargoTicket savedTicket = cargoTicketRepository.save(ticket);

        Payment payment = paymentRepository.findByCargoTicket_CargoTicketId(savedTicket.getCargoTicketId())
                .orElse(null);
        if (payment != null && request.getPaymentMethod() != null) {
            payment.setPaymentMethod(request.getPaymentMethod());
            paymentRepository.save(payment);
        } else if (payment == null && request.getPaymentMethod() != null) {
            payment = Payment.builder()
                    .cargoTicket(savedTicket)
                    .amount(savedTicket.getTotalPrice())
                    .paymentMethod(request.getPaymentMethod())
                    .transactionId(transactionIdGenerator.generateUniqueTransactionId())
                    .status("PENDING")
                    .refundAmount(BigDecimal.ZERO)
                    .build();
            paymentRepository.save(payment);
        }

        return mapToResponse(savedTicket);
    }

    @Override
    @Transactional
    public CargoTicketDetailResponse createCargoTicketDetail(int ticketId, CargoTicketDetailRequest request) {
        CargoTicket ticket = cargoTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn gửi hàng."));

        BigDecimal calcPrice = calculateDetailPrice(request.getCargoTypePriceId(), request.getDimensionVol(),
                request.getQuantity());

        CargoTicketDetail detail = CargoTicketDetail.builder()
                .cargoTicket(ticket)
                .cargoTypePriceId(request.getCargoTypePriceId())
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .weightKg(request.getWeightKg())
                .dimensionVol(request.getDimensionVol())
                .calculatedPrice(calcPrice)
                .build();

        CargoTicketDetail saved = cargoTicketDetailRepository.save(detail);

        cargoTicketDetailRepository.flush();
        updateTicketTotalPrice(ticket);

        return CargoTicketDetailResponse.builder()
                .cargoTicketDetailId(saved.getCargoTicketDetailId())
                .cargoTicketId(saved.getCargoTicket().getCargoTicketId())
                .cargoTypePriceId(saved.getCargoTypePriceId())
                .description(saved.getDescription())
                .quantity(saved.getQuantity())
                .weightKg(saved.getWeightKg())
                .dimensionVol(saved.getDimensionVol())
                .calculatedPrice(saved.getCalculatedPrice())
                .build();
    }

    @Override
    @Transactional
    public CargoTicketDetailResponse updateCargoTicketDetail(int detailId, CargoTicketDetailRequest request) {
        CargoTicketDetail detail = cargoTicketDetailRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiết đơn gửi hàng."));

        BigDecimal calcPrice = calculateDetailPrice(request.getCargoTypePriceId(), request.getDimensionVol(),
                request.getQuantity());

        detail.setCargoTypePriceId(request.getCargoTypePriceId());
        detail.setDescription(request.getDescription());
        detail.setQuantity(request.getQuantity());
        detail.setWeightKg(request.getWeightKg());
        detail.setDimensionVol(request.getDimensionVol());
        detail.setCalculatedPrice(calcPrice);

        CargoTicketDetail saved = cargoTicketDetailRepository.save(detail);

        cargoTicketDetailRepository.flush();
        updateTicketTotalPrice(detail.getCargoTicket());

        return CargoTicketDetailResponse.builder()
                .cargoTicketDetailId(saved.getCargoTicketDetailId())
                .cargoTicketId(saved.getCargoTicket().getCargoTicketId())
                .cargoTypePriceId(saved.getCargoTypePriceId())
                .description(saved.getDescription())
                .quantity(saved.getQuantity())
                .weightKg(saved.getWeightKg())
                .dimensionVol(saved.getDimensionVol())
                .calculatedPrice(saved.getCalculatedPrice())
                .build();
    }

    @Override
    @Transactional
    public void deleteCargoTicketDetail(int detailId) {
        CargoTicketDetail detail = cargoTicketDetailRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiết đơn gửi hàng."));
        CargoTicket ticket = detail.getCargoTicket();
        cargoTicketDetailRepository.delete(detail);

        cargoTicketDetailRepository.flush();
        updateTicketTotalPrice(ticket);
    }

    @Override
    @Transactional
    public void disable(int id) {
        CargoTicket ticket = findByIdOrThrow(id);

        ticket.setStatus("CANCELLED");
        cargoTicketRepository.save(ticket);
    }

    @Override
    public List<CustomerContactResponse> searchContacts(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return List.of();
        }
        List<Object[]> results = cargoTicketRepository.findContactsByPhoneNative(phone.trim());
        return results.stream()
                .map(obj -> CustomerContactResponse.builder()
                        .phone(obj[0] != null ? obj[0].toString() : null)
                        .name(obj[1] != null ? obj[1].toString() : null)
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void completePayment(int cargoTicketId) {
        Payment payment = paymentRepository.findByCargoTicket_CargoTicketId(cargoTicketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thanh toán cho đơn gửi hàng ID: " + cargoTicketId));

        if (!"CASH".equals(payment.getPaymentMethod())) {
            throw new BusinessRuleException("Chỉ có thể hoàn thành thanh toán tiền mặt.");
        }
        if (!"PENDING".equals(payment.getStatus())) {
            throw new BusinessRuleException("Thanh toán đã được xử lý.");
        }

        payment.setStatus("COMPLETED");
        payment.setPaymentTime(java.time.LocalDateTime.now());
        paymentRepository.save(payment);
    }

    @Override
    public List<TripByStopResponse> getTripsByStopsInOrder(TripByStopRequest request) {
        return tripRepository.findTripsByStopsInOrder(request.getPickupStopId(), request.getDropoffStopId())
                .stream()
                .map(t -> TripByStopResponse.builder()
                        .tripId(t.getTripId())
                        .routeId(t.getRouteId())
                        .coachId(t.getCoachId())
                        .coachTypeName(t.getCoach() != null && t.getCoach().getCoachType() != null 
                                ? t.getCoach().getCoachType().getCoachTypeName() : null)
                        .departureTime(t.getDepartureTime())
                        .status(t.getStatus())
                        .build())
                .toList();
    }

    private BigDecimal calculateDetailPrice(int cargoTypePriceId, BigDecimal dimensionVol, int quantity) {
        CargoTypePrice price = cargoTypePriceRepository.findById(cargoTypePriceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn giá cước hàng hóa."));
        BigDecimal unitPrice = FreightCalculatorUtility.calculatePriceWithSurcharge(dimensionVol,
                price.getPricePerUnit());
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private void updateTicketTotalPrice(CargoTicket ticket) {
        List<CargoTicketDetail> details = cargoTicketDetailRepository
                .findByCargoTicket_CargoTicketId(ticket.getCargoTicketId());
        BigDecimal total = details.stream()
                .map(CargoTicketDetail::getCalculatedPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        ticket.setTotalPrice(total);
        cargoTicketRepository.save(ticket);

        Payment payment = paymentRepository.findByCargoTicket_CargoTicketId(ticket.getCargoTicketId()).orElse(null);
        if (payment != null) {
            payment.setAmount(total);
            paymentRepository.save(payment);
        }
    }

    private void validateReferences(CargoTicketRequest request) {
        if (request.getPickupStopId() == request.getDropoffStopId()) {
            throw new BusinessRuleException("Điểm nhận và điểm trả hàng phải khác nhau.");
        }
        if (request.getTripId() != null && !tripRepository.existsById(request.getTripId())) {
            throw new ResourceNotFoundException("Không tìm thấy chuyến đi có ID là: " + request.getTripId());
        }
        if (request.getCustomerId() != null && !customerRepository.existsById(request.getCustomerId())) {
            throw new ResourceNotFoundException("Không tìm thấy khách hàng có ID là: " + request.getCustomerId());
        }
        requireStop(request.getPickupStopId());
        requireStop(request.getDropoffStopId());
        requireStaff(request.getSoldBy());
        requireStaff(request.getLoadedBy());
        requireStaff(request.getUnloadedBy());
        requireStaff(request.getDeliveredBy());

        if (request.getTripId() != null && !tripRepository.isEligibleForCargo(
                request.getTripId(), request.getPickupStopId(), request.getDropoffStopId())) {
            throw new BusinessRuleException("Chuyến xe không còn phù hợp với điểm nhận và điểm trả đã chọn.");
        }
        requireStaffPosition(request.getSoldBy(), "TICKET_STAFF", "Nhân viên bán vé");
        requireHandlerPosition(request.getLoadedBy(), "Nhân viên xếp hàng");
        requireHandlerPosition(request.getUnloadedBy(), "Nhân viên dỡ hàng");
        requireStaffPosition(request.getDeliveredBy(), "DRIVER", "Nhân viên giao hàng");
    }

    private void requireStop(int id) {
        if (!coachStopRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy điểm dừng có ID là: " + id);
        }
    }

    private void requireStaff(Staff staff) {
        if (staff != null && !staffRepository.existsById(staff.getStaffId())) {
            throw new ResourceNotFoundException("Không tìm thấy nhân viên có ID là: " + staff.getStaffId());
        }
    }

    private void requireStaffPosition(Staff staff, String position, String fieldName) {
        if (staff != null
                && !staffRepository.existsByStaffIdAndIsActiveTrueAndStaffPosition(staff.getStaffId(), position)) {
            throw new BusinessRuleException(fieldName + " không đúng chức vụ yêu cầu.");
        }
    }

    private void requireHandlerPosition(Staff staff, String fieldName) {
        if (staff != null && !staffRepository.existsByStaffIdAndIsActiveTrueAndStaffPositionIn(
                staff.getStaffId(), List.of("ATTENDANT", "TICKET_STAFF"))) {
            throw new BusinessRuleException(fieldName + " phải là phụ xe hoặc nhân viên bán vé.");
        }
    }

    private String generateTicketCode() {
        String code;
        do {
            code = "CG-" + LocalDate.now().toString().replace("-", "") + "-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (cargoTicketRepository.existsByTicketCodeIgnoreCase(code));
        return code;
    }

    private CargoTicket findByIdOrThrow(int id) {
        return cargoTicketRepository.findByCargoTicketIdAndStatusNot(id, STATUS_ABANDONED).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy đơn gửi hàng có ID là: " + id));
    }

    private void copyRequest(CargoTicketRequest request, CargoTicket ticket, String ticketCode) {
        ticket.setTripId(request.getTripId());
        ticket.setCustomerId(request.getCustomerId());
        ticket.setSenderName(request.getSenderName().trim());
        ticket.setSenderPhone(request.getSenderPhone().trim());
        ticket.setReceiverName(request.getReceiverName().trim());
        ticket.setReceiverPhone(request.getReceiverPhone().trim());
        ticket.setTicketCode(ticketCode);
        ticket.setTotalPrice(request.getTotalPrice());
        ticket.setDescription(trimToNull(request.getDescription()));
        ticket.setFeePayer(request.getFeePayer());
        ticket.setCodAmount(request.getCodAmount());
        ticket.setPickupStopId(request.getPickupStopId());
        ticket.setDropoffStopId(request.getDropoffStopId());
        ticket.setStatus(request.getStatus());
        ticket.setSoldBy(staffRepository.findById(request.getSoldBy().getStaffId()).orElse(null));
        ticket.setLoadedBy(
                request.getLoadedBy() != null
                        ? staffRepository.findById(request.getLoadedBy().getStaffId()).orElse(null)
                        : null);
        ticket.setUnloadedBy(
                request.getUnloadedBy() != null
                        ? staffRepository.findById(request.getUnloadedBy().getStaffId()).orElse(null)
                        : null);
        ticket.setDeliveredBy(
                request.getDeliveredBy() != null
                        ? staffRepository.findById(request.getDeliveredBy().getStaffId()).orElse(null)
                        : null);
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private CargoTicketResponse mapToResponse(CargoTicket ticket) {
        String pickupStopName = coachStopRepository.findById(ticket.getPickupStopId())
                .map(stop -> stop.getStopPointName()).orElse(null);
        String dropoffStopName = coachStopRepository.findById(ticket.getDropoffStopId())
                .map(stop -> stop.getStopPointName()).orElse(null);

        Payment payment = paymentRepository.findByCargoTicket_CargoTicketId(ticket.getCargoTicketId()).orElse(null);

        return CargoTicketResponse.builder()
                .cargoTicketId(ticket.getCargoTicketId())
                .tripId(ticket.getTripId())
                .customerId(ticket.getCustomerId())
                .senderName(ticket.getSenderName())
                .senderPhone(ticket.getSenderPhone())
                .receiverName(ticket.getReceiverName())
                .receiverPhone(ticket.getReceiverPhone())
                .ticketCode(ticket.getTicketCode())
                .totalPrice(ticket.getTotalPrice())
                .description(ticket.getDescription())
                .feePayer(ticket.getFeePayer())
                .codAmount(ticket.getCodAmount())
                .pickupStopId(ticket.getPickupStopId())
                .pickupStopName(pickupStopName)
                .dropoffStopId(ticket.getDropoffStopId())
                .dropoffStopName(dropoffStopName)
                .status(ticket.getStatus())
                .soldBy(ticket.getSoldBy())
                .loadedBy(ticket.getLoadedBy())
                .unloadedBy(ticket.getUnloadedBy())
                .deliveredBy(ticket.getDeliveredBy())
                .payment(payment)
                .qrUrl(buildQrUrl(payment, ticket.getTotalPrice()))
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    private String buildQrUrl(Payment payment, java.math.BigDecimal totalPrice) {
        if (payment == null || !"BANK_TRANSFER".equals(payment.getPaymentMethod())
                || !"PENDING".equals(payment.getStatus())) {
            return null;
        }
        long amount = totalPrice.longValue();
        return "https://vietqr.app/img?bank=Vietcombank&acc=SBSEPAYHCNTZK98PS6F&template=compact&amount=" + amount
                + "&des=" + payment.getTransactionId();
    }
}
