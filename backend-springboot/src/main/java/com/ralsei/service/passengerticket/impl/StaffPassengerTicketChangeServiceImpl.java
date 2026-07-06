package com.ralsei.service.passengerticket.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.staffpassengerticket.StaffPassengerTicketRowProjection;
import com.ralsei.dto.request.passengerbooking.AccompaniedChildDTO;
import com.ralsei.dto.request.staffpassengerticket.StaffPassengerChangePassengerRequest;
import com.ralsei.dto.response.staffpassengerticket.StaffPassengerTicketDetailResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.AccompaniedChild;
import com.ralsei.model.PassengerTicketDetail;
import com.ralsei.model.Trip;
import com.ralsei.repository.AccompaniedChildRepository;
import com.ralsei.repository.PassengerTicketDetailRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.repository.TripRepository;
import com.ralsei.service.passengerticket.PassengerTicketStaffPolicy;
import com.ralsei.service.passengerticket.StaffPassengerTicketChangeService;
import com.ralsei.service.passengerticket.StaffPassengerTicketQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffPassengerTicketChangeServiceImpl implements StaffPassengerTicketChangeService {

    private final PassengerTicketDetailRepository ticketDetailRepository;
    private final AccompaniedChildRepository accompaniedChildRepository;
    private final TripRepository tripRepository;
    private final StaffRepository staffRepository;
    private final PassengerTicketStaffPolicy policy;
    private final StaffPassengerTicketQueryService queryService;

    @Override
    @Transactional
    public StaffPassengerTicketDetailResponse changePassengerInfo(
        Integer accountId,
        String ticketCode,
        Integer ticketDetailId,
        StaffPassengerChangePassengerRequest request
    ) {
        staffRepository.findByAccountId(accountId)
            .orElseThrow(() -> new BusinessRuleException("Không tìm thấy thông tin nhân viên!"));

        String normalizedTicketCode = ticketCode == null ? "" : ticketCode.trim();
        List<StaffPassengerTicketRowProjection> rows =
            ticketDetailRepository.findStaffTicketRowsByTicketCode(normalizedTicketCode);

        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy vé.");
        }

        StaffPassengerTicketRowProjection targetRow = rows.stream()
            .filter(row -> ticketDetailId.equals(row.getTicketDetailId()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ghế trong vé này."));

        Trip trip = tripRepository.findById(targetRow.getTripId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến xe."));

        policy.assertPassengerInfoChangeAllowed(
            targetRow.getTicketStatus(),
            targetRow.getDetailStatus(),
            targetRow.getPaymentStatus(),
            targetRow.getDepartureTime(),
            trip.getStatus()
        );

        validateAccompaniedChildRequest(request);

        PassengerTicketDetail detail = ticketDetailRepository.findById(ticketDetailId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ghế trong vé này."));

        detail.setFullName(request.fullName().trim());
        detail.setPhone(request.phone().trim());
        detail.setEmail(request.email().trim());
        detail.setUpdatedBy(accountId);
        ticketDetailRepository.save(detail);

        syncAccompaniedChild(ticketDetailId, accountId, request);

        return queryService.getDetail(normalizedTicketCode);
    }

    private void validateAccompaniedChildRequest(StaffPassengerChangePassengerRequest request) {
        if (Boolean.TRUE.equals(request.removeAccompaniedChild()) && request.accompaniedChild() != null) {
            throw new BusinessRuleException("Không thể vừa xóa vừa cập nhật thông tin trẻ em đi kèm.");
        }
    }

    private void syncAccompaniedChild(
        Integer ticketDetailId,
        Integer accountId,
        StaffPassengerChangePassengerRequest request
    ) {
        Optional<AccompaniedChild> existingChild =
            accompaniedChildRepository.findByTicketDetailId(ticketDetailId);

        if (Boolean.TRUE.equals(request.removeAccompaniedChild())) {
            existingChild.ifPresent(accompaniedChildRepository::delete);
            return;
        }

        AccompaniedChildDTO childPayload = request.accompaniedChild();
        if (childPayload == null) {
            return;
        }

        if (existingChild.isPresent()) {
            AccompaniedChild child = existingChild.get();
            child.setFullname(childPayload.fullname().trim());
            child.setBirthYear(childPayload.birthYear());
            child.setUpdatedBy(accountId);
            accompaniedChildRepository.save(child);
            return;
        }

        AccompaniedChild child = AccompaniedChild.builder()
            .ticketDetailId(ticketDetailId)
            .fullname(childPayload.fullname().trim())
            .birthYear(childPayload.birthYear())
            .build();
        child.setCreatedBy(accountId);
        accompaniedChildRepository.save(child);
    }
}
