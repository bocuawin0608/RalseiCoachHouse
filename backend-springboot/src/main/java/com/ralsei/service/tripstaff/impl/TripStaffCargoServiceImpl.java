/**
 * Implementation of on-coach cargo hand-off for trip staff.
 * Supports loading and unloading only; destination receipt is ticket-staff owned.
 */
package com.ralsei.service.tripstaff.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.response.tripstaff.TripStaffCargoResponse;
import com.ralsei.dto.response.tripstaff.TripStaffCargoResponse.CargoDetailItem;
import com.ralsei.dto.response.tripstaff.TripStaffCargoResponse.CargoItem;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.CargoTicket;
import com.ralsei.model.CargoTicketDetail;
import com.ralsei.model.CargoTypePrice;
import com.ralsei.model.CoachStop;
import com.ralsei.model.Staff;
import com.ralsei.repository.CargoTicketDetailRepository;
import com.ralsei.repository.CargoTicketRepository;
import com.ralsei.repository.CargoTypePriceRepository;
import com.ralsei.repository.CoachStopRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.service.JwtService;
import com.ralsei.service.cargoticket.CargoTicketPaymentPolicy;
import com.ralsei.service.tripstaff.TripStaffCargoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/**
 * Provides the trip staff cargo service impl component for the application.
 */
public class TripStaffCargoServiceImpl implements TripStaffCargoService {

    /** Statuses trip staff still handles on the coach (load / unload / just unloaded). */
    private static final List<String> TRIP_STAFF_CARGO_STATUSES = List.of(
            "RECEIVED",
            "LOADED",
            "ARRIVED"
    );

    private final JwtService jwtService;
    private final StaffRepository staffRepository;
    private final CargoTicketRepository cargoTicketRepository;
    private final CargoTicketDetailRepository cargoTicketDetailRepository;
    private final CargoTypePriceRepository cargoTypePriceRepository;
    private final CoachStopRepository coachStopRepository;
    private final CargoTicketPaymentPolicy cargoTicketPaymentPolicy;

    @Override
    @Transactional(readOnly = true)
    /**
     * Returns the cargo list.
     *
     * @param authorizationHeader the value supplied for this operation
     * @param tripId the value supplied for this operation
     *
     * @return the cargo list
     */
    public TripStaffCargoResponse getCargoList(String authorizationHeader, int tripId) {
        resolveStaff(authorizationHeader);

        List<CargoTicket> tickets = cargoTicketRepository.findByTripIdAndStatusIn(
                tripId, TRIP_STAFF_CARGO_STATUSES);

        List<CargoItem> items = tickets.stream()
                .map(this::mapToCargoItem)
                .toList();

        return new TripStaffCargoResponse(items);
    }

    @Override
    @Transactional
    /**
     * Executes the load cargo operation.
     *
     * @param authorizationHeader the value supplied for this operation
     * @param tripId the value supplied for this operation
     * @param cargoTicketId the value supplied for this operation
     */
    public void loadCargo(String authorizationHeader, int tripId, int cargoTicketId) {
        Staff staff = resolveStaff(authorizationHeader);
        CargoTicket ticket = findCargoForTrip(tripId, cargoTicketId);

        if (!"RECEIVED".equals(ticket.getStatus())) {
            throw new BusinessRuleException(
                    "Hàng hóa đang ở trạng thái " + ticket.getStatus() + ", không thể xác nhận lên xe");
        }
        cargoTicketPaymentPolicy.requireSenderPaidBeforeLoad(ticket);

        ticket.setStatus("LOADED");
        ticket.setLoadedBy(staff);
        cargoTicketRepository.save(ticket);
    }

    @Override
    @Transactional
    /**
     * Executes the unload cargo operation.
     *
     * @param authorizationHeader the value supplied for this operation
     * @param tripId the value supplied for this operation
     * @param cargoTicketId the value supplied for this operation
     */
    public void unloadCargo(String authorizationHeader, int tripId, int cargoTicketId) {
        Staff staff = resolveStaff(authorizationHeader);
        CargoTicket ticket = findCargoForTrip(tripId, cargoTicketId);

        if (!"LOADED".equals(ticket.getStatus())) {
            throw new BusinessRuleException(
                    "Hàng hóa chưa được xác nhận lên xe, không thể dỡ xuống");
        }

        ticket.setStatus("ARRIVED");
        ticket.setUnloadedBy(staff);
        cargoTicketRepository.save(ticket);
    }

    private CargoTicket findCargoForTrip(int tripId, int cargoTicketId) {
        CargoTicket ticket = cargoTicketRepository.findById(cargoTicketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        if (ticket.getTripId() != tripId) {
            throw new BusinessRuleException("Đơn hàng không thuộc chuyến đi này");
        }

        return ticket;
    }

    private CargoItem mapToCargoItem(CargoTicket ticket) {
        String pickupStopName = coachStopRepository.findById(ticket.getPickupStopId())
                .map(CoachStop::getStopPointName)
                .orElse("N/A");

        String dropoffStopName = coachStopRepository.findById(ticket.getDropoffStopId())
                .map(CoachStop::getStopPointName)
                .orElse("N/A");

        List<CargoTicketDetail> details = cargoTicketDetailRepository.findByCargoTicket_CargoTicketId(ticket.getCargoTicketId());
        List<CargoDetailItem> detailItems = details.stream()
                .map(this::mapToDetailItem)
                .toList();

        return new CargoItem(
                ticket.getCargoTicketId(),
                ticket.getTicketCode(),
                ticket.getSenderName(),
                ticket.getSenderPhone(),
                ticket.getReceiverName(),
                ticket.getReceiverPhone(),
                pickupStopName,
                dropoffStopName,
                ticket.getStatus(),
                ticket.getTotalPrice(),
                ticket.getDescription(),
                ticket.getFeePayer(),
                ticket.getCodAmount(),
                detailItems);
    }

    private CargoDetailItem mapToDetailItem(CargoTicketDetail detail) {
        String unit = cargoTypePriceRepository.findById(detail.getCargoTypePriceId())
                .map(CargoTypePrice::getUnit)
                .orElse(null);

        return new CargoDetailItem(
                detail.getCargoTicketDetailId(),
                detail.getDescription(),
                detail.getQuantity(),
                detail.getWeightKg(),
                detail.getDimensionVol(),
                detail.getCalculatedPrice(),
                unit);
    }

    private Staff resolveStaff(String authorizationHeader) {
        Integer accountId = jwtService.extractAccountId(authorizationHeader);
        if (accountId == null) {
            throw new BusinessRuleException("Không thể xác định tài khoản đăng nhập");
        }

        Staff staff = staffRepository.findByAccountId(accountId)
                .orElseThrow(() -> new BusinessRuleException("Tài khoản chưa liên kết nhân viên"));

        if (!staff.isActive()) {
            throw new BusinessRuleException("Nhân viên không còn hoạt động");
        }

        return staff;
    }
}
