package com.ralsei.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.request.cargoticket.CargoTicketRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.cargoticket.CargoTicketResponse;
import com.ralsei.dto.response.cargoticket.CargoTicketFormOptionsResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.CargoTicket;
import com.ralsei.repository.CargoTicketRepository;
import com.ralsei.repository.CoachStopRepository;
import com.ralsei.repository.CustomerRepository;
import com.ralsei.repository.PaymentRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.repository.TripRepository;
import com.ralsei.service.CargoTicketService;

import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CargoTicketServiceImpl implements CargoTicketService {
    private static final String STATUS_ABANDONED = "ABANDONED";

    private final CargoTicketRepository cargoTicketRepository;
    private final TripRepository tripRepository;
    private final CustomerRepository customerRepository;
    private final CoachStopRepository coachStopRepository;
    private final StaffRepository staffRepository;
    private final PaymentRepository paymentRepository;

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
                .trips(pickupStopId != null && dropoffStopId != null && !pickupStopId.equals(dropoffStopId)
                        ? tripRepository.findCargoTicketTripOptions(pickupStopId, dropoffStopId)
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
        return mapToResponse(cargoTicketRepository.save(ticket));
    }

    @Override
    @Transactional
    public CargoTicketResponse updateCargoTicket(int id, CargoTicketRequest request) {
        CargoTicket ticket = findByIdOrThrow(id);
        String ticketCode = ticket.getTicketCode();
        validateReferences(request);

        copyRequest(request, ticket, ticketCode);
        return mapToResponse(cargoTicketRepository.save(ticket));
    }

    @Override
    @Transactional
    public void disable(int id) {
        CargoTicket ticket = findByIdOrThrow(id);

        ticket.setStatus("CANCELLED");
        cargoTicketRepository.save(ticket);
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

    private void requireStaff(Integer id) {
        if (id != null && !staffRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy nhân viên có ID là: " + id);
        }
    }

    private void requireStaffPosition(Integer id, String position, String fieldName) {
        if (id != null && !staffRepository.existsByStaffIdAndIsActiveTrueAndStaffPosition(id, position)) {
            throw new BusinessRuleException(fieldName + " không đúng chức vụ yêu cầu.");
        }
    }

    private void requireHandlerPosition(Integer id, String fieldName) {
        if (id != null && !staffRepository.existsByStaffIdAndIsActiveTrueAndStaffPositionIn(
                id, List.of("ATTENDANT", "TICKET_STAFF"))) {
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
                () -> new ResourceNotFoundException("Không tìm thấy vé hàng hóa có ID là: " + id));
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
        ticket.setSoldBy(staffRepository.findById(request.getSoldBy()).orElse(null));
        ticket.setLoadedBy(
                request.getLoadedBy() != null ? staffRepository.findById(request.getLoadedBy()).orElse(null) : null);
        ticket.setUnloadedBy(
                request.getUnloadedBy() != null ? staffRepository.findById(request.getUnloadedBy()).orElse(null)
                        : null);
        ticket.setDeliveredBy(
                request.getDeliveredBy() != null ? staffRepository.findById(request.getDeliveredBy()).orElse(null)
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

        return CargoTicketResponse.builder()
                .cargoTicketId(ticket.getCargoTicketId())
                .tripId(ticket.getTripId())
                .customerId(ticket.getCustomerId())
                .senderName(ticket.getSenderName())
                .senderPhone(ticket.getSenderPhone())
                .receiverName(ticket.getReceiverName())
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
                .soldBy(ticket.getSoldBy() != null ? ticket.getSoldBy().getStaffId() : 0)
                .loadedBy(ticket.getLoadedBy() != null ? ticket.getLoadedBy().getStaffId() : null)
                .unloadedBy(ticket.getUnloadedBy() != null ? ticket.getUnloadedBy().getStaffId() : null)
                .deliveredBy(ticket.getDeliveredBy() != null ? ticket.getDeliveredBy().getStaffId() : null)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
