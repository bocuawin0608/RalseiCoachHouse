package com.ralsei.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.AccountListProjection;
import com.ralsei.dto.request.account.AccountFilterRequest;
import com.ralsei.dto.request.account.AssignRolesRequest;
import com.ralsei.dto.request.account.CreateAccountRequest;
import com.ralsei.dto.request.account.ResetPasswordRequest;
import com.ralsei.dto.request.account.UpdateAccountRequest;
import com.ralsei.dto.response.account.AccountDetailResponse;
import com.ralsei.dto.response.account.AccountListResponse;
import com.ralsei.dto.response.account.RoleResponse;
import com.ralsei.dto.response.account.StaffInfoResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Account;
import com.ralsei.model.AccountRole;
import com.ralsei.model.Role;
import com.ralsei.model.Staff;
import com.ralsei.repository.AccountRepository;
import com.ralsei.repository.AccountRoleRepository;
import com.ralsei.repository.RoleRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.service.AccountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/**
 * Implementation of {@link com.ralsei.service.AccountService}.
 */

public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepo;
    private final AccountRoleRepository accountRoleRepo;
    private final RoleRepository roleRepo;
    private final StaffRepository staffRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<AccountListResponse> filterAccounts(AccountFilterRequest filterRequest, Pageable pageable) {
        List<AccountListProjection> projections = accountRepo.findAllAccountList();

        List<AccountListResponse> responses = projections.stream()
            .map(this::mapToListResponse)
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<AccountListResponse> pageContent = responses.subList(start, end);

        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDetailResponse getAccountDetail(Integer accountId) {
        Account account = accountRepo.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại!"));

        return mapToDetailResponse(account);
    }

    @Override
    @Transactional
    public Integer createAccount(CreateAccountRequest request) {
        if (accountRepo.existsByUsername(request.username())) {
            throw new BusinessRuleException("Tên đăng nhập này đã tồn tại trong hệ thống!");
        }

        Account account = Account.builder()
            .username(request.username())
            .passwordHash(passwordEncoder.encode(request.password()))
            .authProvider("local")
            .isActive(true)
            .build();

        account = accountRepo.save(account);

        Staff staff = Staff.builder()
            .accountId(account.getAccountId())
            .staffName(request.staffName())
            .phone(request.phone())
            .email(request.email())
            .cccd(request.cccd())
            .dob(request.dob())
            .staffPosition(request.staffPosition())
            .ticketAgencyId(request.ticketAgencyId())
            .hireDate(request.hireDate())
            .isActive(true)
            .build();

        staffRepo.save(staff);

        if (request.roleIds() != null) {
            for (Integer roleId : request.roleIds()) {
                Role role = roleRepo.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role không tồn tại với ID: " + roleId));

                AccountRole accountRole = AccountRole.builder()
                    .accountId(account.getAccountId())
                    .roleId(role.getRoleId())
                    .build();

                accountRoleRepo.save(accountRole);
            }
        }

        return account.getAccountId();
    }

    @Override
    @Transactional
    public void updateAccount(Integer accountId, UpdateAccountRequest request) {
        Account account = accountRepo.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại!"));

        Staff staff = staffRepo.findByAccountId(accountId).orElse(null);
        if (staff == null) {
            staff = Staff.builder()
                .accountId(accountId)
                .staffName(request.staffName())
                .phone(request.phone())
                .staffPosition(request.staffPosition())
                .hireDate(request.hireDate() != null ? request.hireDate() : java.time.LocalDate.now())
                .isActive(true)
                .build();
        } else {
            staff.setStaffName(request.staffName());
            staff.setPhone(request.phone());
            staff.setEmail(request.email());
            staff.setCccd(request.cccd());
            staff.setDob(request.dob());
            staff.setStaffPosition(request.staffPosition());
            staff.setTicketAgencyId(request.ticketAgencyId());
            if (request.hireDate() != null) staff.setHireDate(request.hireDate());
        }

        if (request.isActive() != null) {
            account.setActive(request.isActive());
            staff.setActive(request.isActive());
        }

        staffRepo.save(staff);
        accountRepo.save(account);
    }

    @Override
    @Transactional
    public void assignRoles(Integer accountId, AssignRolesRequest request) {
        accountRepo.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại!"));

        accountRoleRepo.deleteByAccountId(accountId);

        for (Integer roleId : request.roleIds()) {
            Role role = roleRepo.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role không tồn tại với ID: " + roleId));

            AccountRole accountRole = AccountRole.builder()
                .accountId(accountId)
                .roleId(role.getRoleId())
                .build();

            accountRoleRepo.save(accountRole);
        }
    }

    @Override
    @Transactional
    public void resetPassword(Integer accountId, ResetPasswordRequest request) {
        Account account = accountRepo.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại!"));

        account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        accountRepo.save(account);
    }

    @Override
    @Transactional
    public void toggleActive(Integer accountId) {
        Account account = accountRepo.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại!"));

        if (account.isActive()) {
            long adminCount = accountRepo.findAll().stream()
                .filter(a -> {
                    List<AccountRole> roles = accountRoleRepo.findByAccountId(a.getAccountId());
                    return roles.stream().anyMatch(ar -> {
                        Role r = roleRepo.findById(ar.getRoleId()).orElse(null);
                        return r != null && "ADMIN".equals(r.getRoleName());
                    });
                })
                .filter(Account::isActive)
                .count();

            if (adminCount <= 1) {
                List<AccountRole> adminRoles = accountRoleRepo.findByAccountId(accountId);
                boolean isAdmin = adminRoles.stream().anyMatch(ar -> {
                    Role r = roleRepo.findById(ar.getRoleId()).orElse(null);
                    return r != null && "ADMIN".equals(r.getRoleName());
                });
                if (isAdmin) {
                    throw new BusinessRuleException("Không thể vô hiệu hóa tài khoản Admin cuối cùng!");
                }
            }
        }

        account.setActive(!account.isActive());
        accountRepo.save(account);

        Staff staff = staffRepo.findByAccountId(accountId).orElse(null);
        if (staff != null) {
            staff.setActive(account.isActive());
            staffRepo.save(staff);
        }
    }

    @Override
    @Transactional
    public void deleteAccount(Integer accountId) {
        Account account = accountRepo.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại!"));

        Staff staff = staffRepo.findByAccountId(accountId).orElse(null);
        if (staff != null) {
            staff.setAccountId(null);
            staff.setActive(false);
            staffRepo.save(staff);
        }

        accountRoleRepo.deleteByAccountId(accountId);
        accountRepo.delete(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepo.findAll().stream()
            .filter(Role::getIsActive)
            .map(role -> new RoleResponse(role.getRoleId(), role.getRoleName()))
            .collect(Collectors.toList());
    }

    private AccountListResponse mapToListResponse(AccountListProjection proj) {
        List<String> roleNames = proj.getRoleNames() != null && !proj.getRoleNames().isBlank()
            ? Arrays.asList(proj.getRoleNames().split(","))
            : List.of();

        return new AccountListResponse(
            proj.getAccountId(),
            proj.getUsername(),
            proj.getAuthProvider(),
            proj.getIsActive() != null && proj.getIsActive(),
            null,
            roleNames,
            proj.getStaffId(),
            proj.getStaffName(),
            proj.getStaffPosition(),
            null
        );
    }

    private AccountDetailResponse mapToDetailResponse(Account account) {
        List<AccountRole> accountRoles = accountRoleRepo.findByAccountId(account.getAccountId());
        List<RoleResponse> roleResponses = accountRoles.stream()
            .map(ar -> roleRepo.findById(ar.getRoleId())
                .map(role -> new RoleResponse(role.getRoleId(), role.getRoleName()))
                .orElse(null))
            .filter(r -> r != null)
            .collect(Collectors.toList());

        Staff staff = staffRepo.findByAccountId(account.getAccountId()).orElse(null);
        StaffInfoResponse staffInfo = staff != null ? new StaffInfoResponse(
            staff.getStaffId(),
            staff.getStaffName(),
            staff.getPhone(),
            staff.getEmail(),
            staff.getCccd(),
            staff.getDob(),
            staff.getStaffPosition(),
            staff.getTicketAgencyId(),
            staff.getHireDate(),
            staff.isActive()
        ) : null;

        return new AccountDetailResponse(
            account.getAccountId(),
            account.getUsername(),
            account.getAuthProvider(),
            account.isActive(),
            account.getLastLogin(),
            roleResponses,
            staffInfo,
            account.getCreatedAt(),
            account.getCreatedBy(),
            account.getUpdatedAt(),
            account.getUpdatedBy()
        );
    }
}
