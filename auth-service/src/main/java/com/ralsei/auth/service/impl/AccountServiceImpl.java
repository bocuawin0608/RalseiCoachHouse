package com.ralsei.auth.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.auth.dto.projection.AccountListProjection;
import com.ralsei.auth.dto.request.account.AccountFilterRequest;
import com.ralsei.auth.dto.request.account.AssignRolesRequest;
import com.ralsei.auth.dto.request.account.CreateAccountRequest;
import com.ralsei.auth.dto.request.account.ResetPasswordRequest;
import com.ralsei.auth.dto.request.account.UpdateAccountRequest;
import com.ralsei.auth.dto.response.account.AccountDetailResponse;
import com.ralsei.auth.dto.response.account.AccountListResponse;
import com.ralsei.auth.dto.response.account.RoleResponse;
import com.ralsei.auth.dto.response.account.StaffInfoResponse;
import com.ralsei.auth.exception.BusinessRuleException;
import com.ralsei.auth.exception.ResourceNotFoundException;
import com.ralsei.auth.feign.CustomerServiceClient;
import com.ralsei.auth.feign.StaffServiceClient;
import com.ralsei.auth.model.Account;
import com.ralsei.auth.model.AccountRole;
import com.ralsei.auth.model.Role;
import com.ralsei.auth.repository.AccountRepository;
import com.ralsei.auth.repository.AccountRoleRepository;
import com.ralsei.auth.repository.RoleRepository;
import com.ralsei.auth.service.AccountService;
import com.ralsei.auth.util.AccountRoleGuard;

import org.springframework.jdbc.core.JdbcTemplate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepo;
    private final AccountRoleRepository accountRoleRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    public Page<AccountListResponse> filterAccounts(AccountFilterRequest filterRequest, Pageable pageable) {
        List<AccountListProjection> projections = accountRepo.findAllAccountList();

        List<AccountListResponse> responses = projections.stream()
            .map(this::mapToListResponse)
            .filter(acc -> {
                if (filterRequest == null) return true;
                String search = filterRequest.search();
                if (search != null && !search.isBlank()) {
                    String s = search.toLowerCase();
                    boolean match = (acc.username() != null && acc.username().toLowerCase().contains(s))
                        || (acc.staffName() != null && acc.staffName().toLowerCase().contains(s))
                        || (acc.customerName() != null && acc.customerName().toLowerCase().contains(s))
                        || (acc.roles() != null && String.join(",", acc.roles()).toLowerCase().contains(s));
                    if (!match) return false;
                }
                if (filterRequest.isActive() != null && acc.isActive() != filterRequest.isActive()) return false;
                if (filterRequest.staffPosition() != null && !filterRequest.staffPosition().isBlank()
                    && !filterRequest.staffPosition().equalsIgnoreCase(acc.staffPosition())) return false;
                if (filterRequest.authProvider() != null && !filterRequest.authProvider().isBlank()
                    && !filterRequest.authProvider().equalsIgnoreCase(acc.authProvider())) return false;
                if (filterRequest.role() != null && !filterRequest.role().isBlank()) {
                    if (acc.roles() == null || acc.roles().stream().noneMatch(r -> r.equalsIgnoreCase(filterRequest.role()))) return false;
                }
                return true;
            })
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<AccountListResponse> pageContent = start < end ? responses.subList(start, end) : List.of();

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

        jdbcTemplate.update("""
            INSERT INTO staff (accountId, staffName, phone, email, cccd, dob, staffPosition, ticketAgencyId, hireDate, isActive, createdAt, updatedAt)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, SYSDATETIME(), SYSDATETIME())
        """,
            account.getAccountId(),
            request.staffName(),
            request.phone(),
            request.email(),
            request.cccd(),
            request.dob(),
            request.staffPosition(),
            request.ticketAgencyId(),
            request.hireDate() != null ? request.hireDate() : java.time.LocalDate.now()
        );

        if (request.roleIds() != null) {
            List<Role> roles = request.roleIds().stream()
                .map(roleId -> roleRepo.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role không tồn tại với ID: " + roleId)))
                .collect(Collectors.toList());

            AccountRoleGuard.validateStaffOnlyRoles(
                roles.stream().map(Role::getRoleName).collect(Collectors.toList())
            );

            for (Role role : roles) {
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

        Integer existingStaffCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM staff WHERE accountId = ?", Integer.class, accountId
        );
        boolean activeVal = request.isActive() != null ? request.isActive() : account.isActive();

        if (existingStaffCount == null || existingStaffCount == 0) {
            jdbcTemplate.update("""
                INSERT INTO staff (accountId, staffName, phone, email, cccd, dob, staffPosition, ticketAgencyId, hireDate, isActive, createdAt, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME(), SYSDATETIME())
            """,
                accountId,
                request.staffName(),
                request.phone(),
                request.email(),
                request.cccd(),
                request.dob(),
                request.staffPosition(),
                request.ticketAgencyId(),
                request.hireDate() != null ? request.hireDate() : java.time.LocalDate.now(),
                activeVal
            );
        } else {
            jdbcTemplate.update("""
                UPDATE staff
                SET staffName = ?, phone = ?, email = ?, cccd = ?, dob = ?, staffPosition = ?, ticketAgencyId = ?,
                    hireDate = COALESCE(?, hireDate), isActive = ?, updatedAt = SYSDATETIME()
                WHERE accountId = ?
            """,
                request.staffName(),
                request.phone(),
                request.email(),
                request.cccd(),
                request.dob(),
                request.staffPosition(),
                request.ticketAgencyId(),
                request.hireDate(),
                activeVal,
                accountId
            );
        }

        if (request.isActive() != null) {
            account.setActive(request.isActive());
        }

        accountRepo.save(account);
    }

    @Override
    @Transactional
    public void assignRoles(Integer accountId, AssignRolesRequest request) {
        accountRepo.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại!"));

        List<AccountRole> currentAccountRoles = accountRoleRepo.findByAccountId(accountId);
        List<String> currentRoleNames = currentAccountRoles.stream()
            .map(ar -> roleRepo.findById(ar.getRoleId()).orElse(null))
            .filter(r -> r != null)
            .map(Role::getRoleName)
            .collect(Collectors.toList());

        List<Role> newRoles = request.roleIds().stream()
            .map(roleId -> roleRepo.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role không tồn tại với ID: " + roleId)))
            .collect(Collectors.toList());

        List<String> newRoleNames = newRoles.stream()
            .map(Role::getRoleName)
            .collect(Collectors.toList());

        Integer custCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customer WHERE accountId = ?", Integer.class, accountId);
        Integer staffCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM staff WHERE accountId = ?", Integer.class, accountId);
        boolean linkedToCustomer = custCount != null && custCount > 0;
        boolean linkedToStaff = staffCount != null && staffCount > 0;

        AccountRoleGuard.validateRoleAssignment(
            linkedToCustomer,
            linkedToStaff,
            currentRoleNames,
            newRoleNames
        );

        accountRoleRepo.deleteByAccountId(accountId);
        accountRoleRepo.flush();

        for (Role role : newRoles) {
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

        boolean newActiveStatus = !account.isActive();
        account.setActive(newActiveStatus);
        accountRepo.save(account);

        jdbcTemplate.update("UPDATE staff SET isActive = ?, updatedAt = SYSDATETIME() WHERE accountId = ?", newActiveStatus, accountId);
        jdbcTemplate.update("UPDATE customer SET isActive = ?, updatedAt = SYSDATETIME() WHERE accountId = ?", newActiveStatus, accountId);
    }

    @Override
    @Transactional
    public void deleteAccount(Integer accountId) {
        Account account = accountRepo.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại!"));

        jdbcTemplate.update("UPDATE staff SET accountId = NULL, isActive = 0, updatedAt = SYSDATETIME() WHERE accountId = ?", accountId);

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
            ? Arrays.stream(proj.getRoleNames().split(",")).map(String::trim).collect(Collectors.toList())
            : List.of();

        LocalDateTime lastLogin = null;
        if (proj.getLastLogin() != null && !proj.getLastLogin().isBlank()) {
            try { lastLogin = LocalDateTime.parse(proj.getLastLogin().replace(" ", "T"), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")); } catch (Exception ignored) {}
        }
        LocalDateTime createdAt = null;
        if (proj.getCreatedAt() != null && !proj.getCreatedAt().isBlank()) {
            try { createdAt = LocalDateTime.parse(proj.getCreatedAt().replace(" ", "T"), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")); } catch (Exception ignored) {}
        }

        return new AccountListResponse(
            proj.getAccountId(),
            proj.getUsername(),
            proj.getAuthProvider(),
            proj.getIsActive() != null && proj.getIsActive(),
            lastLogin,
            roleNames,
            proj.getStaffId(),
            proj.getStaffName(),
            proj.getStaffPosition(),
            proj.getPhone(),
            proj.getEmail(),
            createdAt,
            proj.getCustomerName()
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

        List<StaffInfoResponse> staffList = jdbcTemplate.query("""
            SELECT staffId, staffName, phone, email, cccd, dob, staffPosition, ticketAgencyId, hireDate, isActive
            FROM staff WHERE accountId = ?
        """, (rs, rowNum) -> new StaffInfoResponse(
            rs.getInt("staffId"),
            rs.getString("staffName"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getString("cccd"),
            rs.getDate("dob") != null ? rs.getDate("dob").toLocalDate() : null,
            rs.getString("staffPosition"),
            rs.getObject("ticketAgencyId", Integer.class),
            rs.getDate("hireDate") != null ? rs.getDate("hireDate").toLocalDate() : null,
            rs.getBoolean("isActive")
        ), account.getAccountId());

        StaffInfoResponse staffInfo = staffList.isEmpty() ? null : staffList.get(0);

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
