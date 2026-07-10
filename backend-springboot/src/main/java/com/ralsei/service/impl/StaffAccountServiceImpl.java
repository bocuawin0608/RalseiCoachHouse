package com.ralsei.service.impl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.AccountProjection;
import com.ralsei.dto.request.staff.StaffPasswordChangeRequest;
import com.ralsei.dto.request.staff.StaffProfileUpdateRequest;
import com.ralsei.dto.response.staff.StaffAccountActionResponse;
import com.ralsei.dto.response.staff.StaffProfileResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Account;
import com.ralsei.model.Staff;
import com.ralsei.repository.AccountRepository;
import com.ralsei.repository.RefreshTokenRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.service.StaffAccountService;

import lombok.RequiredArgsConstructor;

/**
 * Default internal staff account self-service implementation.
 * All lookups are based on the authenticated JWT principal so staff pages
 * cannot request or mutate another staff member's profile by guessing IDs.
 */
@Service
@RequiredArgsConstructor
public class StaffAccountServiceImpl implements StaffAccountService {

    private static final int MIN_STAFF_AGE_YEARS = 20;
    private static final List<String> STAFF_ROLES = List.of("ADMIN", "MANAGER", "TICKET_STAFF", "TRIP_STAFF");

    private final AccountRepository accountRepository;
    private final StaffRepository staffRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Returns the current staff member's profile from Account and Staff records.
     */
    @Override
    @Transactional(readOnly = true)
    public StaffProfileResponse getCurrentProfile() {
        AccountProjection account = findCurrentStaffAccountProjection();
        Staff staff = findActiveStaff(account.getAccountId());
        return toProfileResponse(account, staff);
    }

    /**
     * Updates name, email, and birth date. Operational fields remain read-only
     * because they define assignments and permissions across the internal site.
     */
    @Override
    @Transactional
    public StaffProfileResponse updateCurrentProfile(StaffProfileUpdateRequest request) {
        AccountProjection account = findCurrentStaffAccountProjection();
        Staff staff = findActiveStaff(account.getAccountId());
        String normalizedEmail = request.email();

        if (normalizedEmail != null
                && staffRepository.existsByEmailIgnoreCaseAndStaffIdNot(normalizedEmail, staff.getStaffId())) {
            throw new BusinessRuleException("Email đã được nhân viên khác sử dụng.");
        }
        if (request.dob() != null && request.dob().isAfter(LocalDate.now().minusYears(MIN_STAFF_AGE_YEARS))) {
            throw new BusinessRuleException("Nhân viên phải từ 20 tuổi trở lên.");
        }

        staff.setStaffName(request.staffName());
        staff.setEmail(normalizedEmail);
        staff.setDob(request.dob());
        staffRepository.save(staff);

        return getCurrentProfile();
    }

    /**
     * Changes the local staff password after verifying the current password.
     */
    @Override
    @Transactional
    public StaffAccountActionResponse changeCurrentPassword(StaffPasswordChangeRequest request) {
        AccountProjection projection = findCurrentStaffAccountProjection();
        Account account = accountRepository.findById(projection.getAccountId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản nhân viên."));

        if (!"local".equalsIgnoreCase(account.getAuthProvider())) {
            throw new BusinessRuleException("Tài khoản này không dùng mật khẩu nội bộ.");
        }
        if (account.getPasswordHash() == null
                || !passwordEncoder.matches(request.currentPassword(), account.getPasswordHash())) {
            throw new BusinessRuleException("Mật khẩu hiện tại không đúng.");
        }
        if (passwordEncoder.matches(request.newPassword(), account.getPasswordHash())) {
            throw new BusinessRuleException("Mật khẩu mới không được trùng mật khẩu hiện tại.");
        }

        account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        accountRepository.save(account);
        refreshTokenRepository.revokeAllByAccount(account);

        return new StaffAccountActionResponse(true, "Mật khẩu đã được cập nhật. Vui lòng đăng nhập lại.");
    }

    /**
     * Loads the active staff account projection attached to the JWT username.
     */
    private AccountProjection findCurrentStaffAccountProjection() {
        AccountProjection account = accountRepository.findByUsernameWithRoles(currentUsername())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản đang đăng nhập."));
        if (!Boolean.TRUE.equals(account.getIsActive())) {
            throw new BusinessRuleException("Tài khoản đã bị khóa.");
        }
        List<String> roles = splitRoles(account.getRoleNames());
        if (roles.stream().noneMatch(STAFF_ROLES::contains)) {
            throw new BusinessRuleException("Tài khoản hiện tại không phải nhân viên.");
        }
        return account;
    }

    /**
     * Loads the active staff entity for the trusted account identifier.
     */
    private Staff findActiveStaff(Integer accountId) {
        Staff staff = staffRepository.findByAccountId(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhân viên."));
        if (!staff.isActive()) {
            throw new BusinessRuleException("Hồ sơ nhân viên đã bị khóa.");
        }
        return staff;
    }

    /**
     * Reads the username placed into the SecurityContext by JWT authentication.
     */
    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new BusinessRuleException("Phiên đăng nhập không hợp lệ.");
        }
        return authentication.getName();
    }

    /**
     * Splits the comma-separated role projection into normalized role names.
     */
    private List<String> splitRoles(String roleNames) {
        if (roleNames == null || roleNames.isBlank()) {
            return List.of();
        }
        return Arrays.stream(roleNames.split(","))
            .map(String::trim)
            .filter(role -> !role.isBlank())
            .toList();
    }

    /**
     * Converts persistence objects into the profile response used by staff UI.
     */
    private StaffProfileResponse toProfileResponse(AccountProjection account, Staff staff) {
        return new StaffProfileResponse(
            staff.getStaffId(),
            account.getAccountId(),
            account.getUsername(),
            staff.getStaffName(),
            staff.getPhone(),
            staff.getEmail(),
            staff.getDob(),
            staff.getStaffPosition(),
            staff.getHireDate(),
            staff.getTicketAgencyId(),
            splitRoles(account.getRoleNames())
        );
    }
}
