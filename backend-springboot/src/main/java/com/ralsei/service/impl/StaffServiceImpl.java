package com.ralsei.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.staff.StaffListProjection;
import com.ralsei.dto.request.staff.StaffFilterRequest;
import com.ralsei.dto.request.staff.UpdateStaffRequest;
import com.ralsei.dto.response.staff.StaffDetailResponse;
import com.ralsei.dto.response.staff.StaffListResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.CoachStop;
import com.ralsei.model.Staff;
import com.ralsei.model.TicketAgency;
import com.ralsei.repository.AccountRepository;
import com.ralsei.repository.CoachStopRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.repository.TicketAgencyRepository;
import com.ralsei.service.StaffService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepo;
    private final TicketAgencyRepository ticketAgencyRepo;
    private final CoachStopRepository coachStopRepo;
    private final AccountRepository accountRepo;

    @Override
    @Transactional(readOnly = true)
    public Page<StaffListResponse> filterStaff(StaffFilterRequest filterRequest, Pageable pageable) {
        String search = filterRequest != null && filterRequest.search() != null && !filterRequest.search().isBlank()
            ? filterRequest.search().trim() : null;
        Boolean isActive = filterRequest != null ? filterRequest.isActive() : null;
        String staffPosition = filterRequest != null && filterRequest.staffPosition() != null && !filterRequest.staffPosition().isBlank()
            ? filterRequest.staffPosition().trim() : null;
        Integer ticketAgencyId = filterRequest != null ? filterRequest.ticketAgencyId() : null;

        List<StaffListProjection> projections = staffRepo.filterStaff(search, isActive, staffPosition, ticketAgencyId);

        List<StaffListResponse> responses = projections.stream()
            .map(this::mapToListResponse)
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<StaffListResponse> pageContent = start < responses.size() ? responses.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    @Override
    @Transactional(readOnly = true)
    public StaffDetailResponse getStaffDetail(Integer staffId) {
        Staff staff = staffRepo.findById(staffId)
            .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại!"));
        return mapToDetailResponse(staff);
    }

    @Override
    @Transactional
    public void updateStaff(Integer staffId, UpdateStaffRequest request) {
        Staff staff = staffRepo.findById(staffId)
            .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại!"));

        staff.setStaffName(request.staffName().trim());
        staff.setPhone(request.phone() != null ? request.phone().trim() : null);
        staff.setEmail(request.email() != null ? request.email().trim() : null);
        staff.setDob(request.dob());
        staff.setCccd(request.cccd() != null ? request.cccd().trim() : null);
        staff.setStaffPosition(request.staffPosition().trim());
        staff.setHireDate(request.hireDate());
        staff.setTicketAgencyId(request.ticketAgencyId());
        if (request.isActive() != null) {
            staff.setActive(request.isActive());
        }
        staffRepo.save(staff);
    }

    @Override
    @Transactional
    public void toggleActive(Integer staffId) {
        Staff staff = staffRepo.findById(staffId)
            .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại!"));
        staff.setActive(!staff.isActive());
        staffRepo.save(staff);
    }

    @Override
    @Transactional
    public void deleteStaff(Integer staffId) {
        Staff staff = staffRepo.findById(staffId)
            .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại!"));

        if (staff.getAccountId() != null) {
            throw new BusinessRuleException(
                "Không thể xóa nhân viên đã có tài khoản. Vui lòng vô hiệu hóa thay vì xóa!");
        }

        staffRepo.delete(staff);
    }

    private StaffListResponse mapToListResponse(StaffListProjection proj) {
        return new StaffListResponse(
            proj.getStaffId(),
            proj.getStaffName(),
            proj.getPhone(),
            proj.getEmail(),
            proj.getCccd(),
            proj.getStaffPosition(),
            proj.getTicketAgencyId(),
            proj.getTicketAgencyName(),
            proj.getUsername(),
            proj.getIsActive() != null && proj.getIsActive(),
            proj.getCreatedAt()
        );
    }

    private StaffDetailResponse mapToDetailResponse(Staff staff) {
        String ticketAgencyName = null;
        if (staff.getTicketAgencyId() != null) {
            ticketAgencyName = ticketAgencyRepo.findById(staff.getTicketAgencyId())
                .map(TicketAgency::getTicketAgencyName)
                .orElse(null);
        }

        Boolean accountActive = null;
        String username = null;
        if (staff.getAccountId() != null) {
            var accountOpt = accountRepo.findById(staff.getAccountId());
            if (accountOpt.isPresent()) {
                var account = accountOpt.get();
                accountActive = account.isActive();
                username = account.getUsername();
            }
        }

        return new StaffDetailResponse(
            staff.getStaffId(),
            staff.getAccountId(),
            staff.getTicketAgencyId(),
            ticketAgencyName,
            staff.getStaffName(),
            staff.getPhone(),
            staff.getEmail(),
            staff.getDob(),
            staff.getCccd(),
            staff.getStaffPosition(),
            staff.getHireDate(),
            staff.isActive(),
            accountActive,
            username,
            staff.getCreatedAt(),
            staff.getCreatedBy(),
            staff.getUpdatedAt(),
            staff.getUpdatedBy()
        );
    }
}
