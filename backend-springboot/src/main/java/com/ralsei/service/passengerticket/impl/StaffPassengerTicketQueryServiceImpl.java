package com.ralsei.service.passengerticket.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.staffpassengerticket.StaffPassengerTicketRowProjection;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerTicketDetailResponse;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerTicketDetailResponse.RefundItem;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerTicketDetailResponse.SeatItem;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerTicketListItemResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Payment;
import com.ralsei.model.Refund;
import com.ralsei.repository.PassengerTicketDetailRepository;
import com.ralsei.repository.PassengerTicketRepository;
import com.ralsei.repository.PaymentRepository;
import com.ralsei.repository.RefundRepository;
import com.ralsei.service.passengerticket.PassengerTicketStaffPolicy;
import com.ralsei.service.passengerticket.StaffPassengerTicketQueryService;
import com.ralsei.util.QRCreateUitility;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffPassengerTicketQueryServiceImpl implements StaffPassengerTicketQueryService {

    private final PassengerTicketRepository ticketRepository;
    private final PassengerTicketDetailRepository ticketDetailRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PassengerTicketStaffPolicy policy;
    private final QRCreateUitility qrCreateUitility;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<StaffPassengerTicketListItemResponse> search(
        String phone,
        String ticketCode,
        String status,
        Integer routeId,
        Integer tripId,
        LocalDate departureDate,
        int page,
        int size
    ) {
        SearchParams params = normalizeSearchParams(phone, ticketCode, status, routeId, tripId, departureDate);
        validateCoreFilter(params);

        long totalElements = ticketRepository.countStaffPassengerTickets(
            params.phone(), params.ticketCode(), params.status(),
            params.routeId(), params.tripId(), params.departureDate()
        );

        if (totalElements == 0) {
            return emptyPage(page, size);
        }

        int totalPages = (int) Math.ceil((double) totalElements / size);
        List<Integer> ticketIds = ticketRepository.findStaffPassengerTicketIds(
            params.phone(), params.ticketCode(), params.status(),
            params.routeId(), params.tripId(), params.departureDate(),
            page * size, size
        );

        if (ticketIds.isEmpty()) {
            return emptyPage(page, size);
        }

        List<StaffPassengerTicketRowProjection> rows =
            ticketDetailRepository.findStaffTicketRowsByPassengerTicketIds(ticketIds);

        List<StaffPassengerTicketListItemResponse> content = assembleListItems(rows);
        return new PagedResponse<>(
            content,
            page,
            size,
            totalElements,
            totalPages,
            page >= totalPages - 1
        );
    }

    @Override
    @Transactional(readOnly = true)
    public StaffPassengerTicketDetailResponse getDetail(String ticketCode) {
        if (ticketCode == null || ticketCode.isBlank()) {
            throw new ResourceNotFoundException("Mã vé không hợp lệ.");
        }

        List<StaffPassengerTicketRowProjection> rows =
            ticketDetailRepository.findStaffTicketRowsByTicketCode(ticketCode.trim());

        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy vé.");
        }

        return assembleDetail(rows, loadRefunds(rows.get(0).getPassengerTicketId()));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getSeatQrImage(String ticketCode, Integer ticketDetailId) {
        String token = ticketDetailRepository.findStaffQrToken(ticketCode, ticketDetailId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã QR của ghế."));
        return qrCreateUitility.createPng(token);
    }

    private List<RefundItem> loadRefunds(Integer passengerTicketId) {
        return paymentRepository.findByPassengerTicketId(passengerTicketId)
            .map(Payment::getPaymentId)
            .map(refundRepository::findByPaymentIdOrderByCreatedAtDesc)
            .orElse(List.of())
            .stream()
            .map(this::mapRefund)
            .toList();
    }

    private RefundItem mapRefund(Refund refund) {
        return new RefundItem(
            refund.getRefundId(),
            refund.getAmount(),
            refund.getStatus(),
            refund.getReason(),
            refund.getRefundTime()
        );
    }

    private SearchParams normalizeSearchParams(
        String phone,
        String ticketCode,
        String status,
        Integer routeId,
        Integer tripId,
        LocalDate departureDate
    ) {
        return new SearchParams(
            trimToNull(phone),
            trimToNull(ticketCode),
            trimToNull(status),
            routeId != null && routeId > 0 ? routeId : null,
            tripId != null && tripId > 0 ? tripId : null,
            departureDate
        );
    }

    private void validateCoreFilter(SearchParams params) {
        boolean hasCoreFilter = params.phone() != null
            || params.ticketCode() != null
            || params.departureDate() != null
            || params.tripId() != null;

        if (!hasCoreFilter) {
            throw new BusinessRuleException(
                "Vui lòng nhập SĐT, mã vé, chọn ngày khởi hành hoặc lọc theo chuyến xe."
            );
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private PagedResponse<StaffPassengerTicketListItemResponse> emptyPage(int page, int size) {
        return new PagedResponse<>(List.of(), page, size, 0, 0, true);
    }

    private List<StaffPassengerTicketListItemResponse> assembleListItems(List<StaffPassengerTicketRowProjection> rows) {
        Map<Integer, List<StaffPassengerTicketRowProjection>> rowsByTicket = new LinkedHashMap<>();
        rows.forEach(row -> rowsByTicket
            .computeIfAbsent(row.getPassengerTicketId(), ignored -> new ArrayList<>())
            .add(row));

        return rowsByTicket.values().stream().map(ticketRows -> {
            StaffPassengerTicketRowProjection first = ticketRows.get(0);
            List<String> seatCodes = ticketRows.stream()
                .map(StaffPassengerTicketRowProjection::getSeatCode)
                .collect(Collectors.toList());

            return new StaffPassengerTicketListItemResponse(
                first.getPassengerTicketId(),
                first.getTicketCode(),
                first.getTicketStatus(),
                first.getFullName(),
                first.getPhone(),
                first.getRouteName(),
                first.getDepartureTime(),
                first.getLicensePlate(),
                seatCodes,
                seatCodes.size(),
                first.getTotalPrice()
            );
        }).toList();
    }

    private StaffPassengerTicketDetailResponse assembleDetail(
        List<StaffPassengerTicketRowProjection> rows,
        List<RefundItem> refunds
    ) {
        StaffPassengerTicketRowProjection first = rows.get(0);
        long hoursLeft = policy.hoursUntilDeparture(first.getDepartureTime());

        List<SeatItem> seats = rows.stream()
            .map(row -> new SeatItem(
                row.getTicketDetailId(),
                row.getSeatCode(),
                row.getDetailStatus(),
                row.getSeatPrice(),
                row.getFullName(),
                row.getPhone(),
                row.getEmail(),
                row.getChildFullname(),
                row.getChildBirthYear()
            ))
            .toList();

        return new StaffPassengerTicketDetailResponse(
            first.getPassengerTicketId(),
            first.getTicketCode(),
            first.getTicketStatus(),
            first.getTripId(),
            first.getRouteId(),
            first.getRouteName(),
            first.getDepartureTime(),
            first.getLicensePlate(),
            first.getCoachTypeName(),
            first.getPickupStopName(),
            first.getDropoffStopName(),
            first.getTotalPrice(),
            first.getVoucherCodeSnapshot(),
            first.getSoldByStaffName(),
            first.getBookedAt(),
            first.getPaymentMethod(),
            first.getPaymentStatus(),
            first.getPaymentAmount(),
            first.getPaymentRefundAmount() != null ? first.getPaymentRefundAmount() : BigDecimal.ZERO,
            refunds,
            seats,
            policy.resolveAllowedActions(
                first.getTicketStatus(), rows, first.getDepartureTime(), first.getPaymentStatus()
            ),
            hoursLeft,
            policy.resolveRefundTierLabel(hoursLeft)
        );
    }

    private record SearchParams(
        String phone,
        String ticketCode,
        String status,
        Integer routeId,
        Integer tripId,
        LocalDate departureDate
    ) {}
}
