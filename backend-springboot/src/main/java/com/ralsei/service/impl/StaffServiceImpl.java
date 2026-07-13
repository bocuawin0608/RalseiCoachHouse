package com.ralsei.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.staff.StaffListProjection;
import com.ralsei.dto.request.staff.OnboardStaffRequest;
import com.ralsei.dto.request.staff.StaffFilterRequest;
import com.ralsei.dto.request.staff.UpdateStaffRequest;
import com.ralsei.dto.response.staff.OnboardStaffResponse;
import com.ralsei.dto.response.staff.StaffDetailResponse;
import com.ralsei.dto.response.staff.StaffListResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Account;
import com.ralsei.model.AccountRole;
import com.ralsei.model.CoachStop;
import com.ralsei.model.Role;
import com.ralsei.model.Staff;
import com.ralsei.model.TicketAgency;
import com.ralsei.repository.AccountRepository;
import com.ralsei.repository.AccountRoleRepository;
import com.ralsei.repository.CoachStopRepository;
import com.ralsei.repository.RoleRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.repository.TicketAgencyRepository;
import com.ralsei.service.StaffService;
import com.ralsei.util.AccountRoleGuard;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/**
 * Implementation of {@link com.ralsei.service.StaffService}.
 */

public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepo;
    private final TicketAgencyRepository ticketAgencyRepo;
    private final CoachStopRepository coachStopRepo;
    private final AccountRepository accountRepo;
    private final AccountRoleRepository accountRoleRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

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
        staff.setHireDate(request.hireDate() != null ? request.hireDate()
            : (staff.getHireDate() != null ? staff.getHireDate() : java.time.LocalDate.now()));
        staff.setTicketAgencyId(request.ticketAgencyId());
        if (request.isActive() != null) {
            staff.setActive(request.isActive());
        }
        staffRepo.save(staff);

        List<Integer> roleIds = request.roleIds();
        if (roleIds != null && staff.getAccountId() != null) {
            List<Role> roles = roleIds.stream()
                .filter(rid -> rid != null)
                .map(rid -> roleRepo.findById(rid)
                    .orElseThrow(() -> new ResourceNotFoundException("Role không tồn tại với ID: " + rid)))
                .collect(Collectors.toList());

            AccountRoleGuard.validateStaffOnlyRoles(
                roles.stream().map(Role::getRoleName).collect(Collectors.toList())
            );

            accountRoleRepo.deleteByAccountId(staff.getAccountId());
            List<AccountRole> newRoles = roles.stream()
                .map(role -> AccountRole.builder()
                    .accountId(staff.getAccountId())
                    .roleId(role.getRoleId())
                    .build())
                .collect(Collectors.toList());
            if (!newRoles.isEmpty()) {
                accountRoleRepo.saveAll(newRoles);
            }
        }
    }

    @Override
    @Transactional
    public void toggleActive(Integer staffId) {
        Staff staff = staffRepo.findById(staffId)
            .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại!"));
        staff.setActive(!staff.isActive());
        staffRepo.save(staff);

        if (staff.getAccountId() != null) {
            accountRepo.findById(staff.getAccountId()).ifPresent(account -> {
                account.setActive(staff.isActive());
                accountRepo.save(account);
            });
        }
    }

    @Override
    @Transactional
    public void deleteStaff(Integer staffId) {
        Staff staff = staffRepo.findById(staffId)
            .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại!"));

        Integer accountId = staff.getAccountId();
        if (accountId != null) {
            accountRoleRepo.deleteByAccountId(accountId);
            staff.setAccountId(null);
            staffRepo.save(staff);
            staffRepo.flush();
            accountRepo.deleteById(accountId);
        }

        staffRepo.delete(staff);
    }

    @Override
    @Transactional
    public OnboardStaffResponse onboardStaff(OnboardStaffRequest request) {
        if (staffRepo.existsByPhoneIgnoreCase(request.phone().trim())) {
            throw new BusinessRuleException("Số điện thoại đã tồn tại trong danh sách nhân viên!");
        }
        if (accountRepo.existsByUsername(request.phone().trim())) {
            throw new BusinessRuleException("Số điện thoại đã được dùng làm tên đăng nhập!");
        }

        // Validate roles exist and stay on staff side
        List<Role> roles = request.roleIds().stream()
            .map(id -> roleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role không tồn tại với ID: " + id)))
            .collect(Collectors.toList());
        AccountRoleGuard.validateStaffOnlyRoles(
            roles.stream().map(Role::getRoleName).collect(Collectors.toList())
        );

        // Create Account
        Account account = Account.builder()
            .username(request.phone().trim())
            .passwordHash(passwordEncoder.encode("123456"))
            .authProvider("local")
            .isActive(true)
            .build();
        account = accountRepo.save(account);

        // Create AccountRole
        for (Role role : roles) {
            AccountRole ar = AccountRole.builder()
                .accountId(account.getAccountId())
                .roleId(role.getRoleId())
                .build();
            accountRoleRepo.save(ar);
        }

        // Create Staff
        Staff staff = Staff.builder()
            .accountId(account.getAccountId())
            .staffName(request.staffName().trim())
            .phone(request.phone().trim())
            .email(request.email() != null ? request.email().trim() : null)
            .dob(request.dob())
            .cccd(request.cccd() != null ? request.cccd().trim() : null)
            .staffPosition(request.staffPosition().trim())
            .hireDate(request.hireDate())
            .ticketAgencyId(request.ticketAgencyId())
            .isActive(true)
            .build();
        staff = staffRepo.save(staff);

        return new OnboardStaffResponse(
            staff.getStaffId(),
            account.getAccountId(),
            account.getUsername()
        );
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
            proj.getDob(),
            proj.getHireDate(),
            proj.getCreatedAt(),
            proj.getRoleName()
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
