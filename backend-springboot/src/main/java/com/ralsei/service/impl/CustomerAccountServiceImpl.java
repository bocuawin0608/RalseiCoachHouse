package com.ralsei.service.impl;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.AccountProjection;
import com.ralsei.dto.request.customer.CustomerProfileUpdateRequest;
import com.ralsei.dto.response.customer.CustomerAccountActionResponse;
import com.ralsei.dto.response.customer.CustomerProfileResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Account;
import com.ralsei.model.Customer;
import com.ralsei.repository.AccountRepository;
import com.ralsei.repository.CustomerRepository;
import com.ralsei.repository.RefreshTokenRepository;
import com.ralsei.service.CustomerAccountService;
import com.ralsei.util.PhoneNumberUtility;

import lombok.RequiredArgsConstructor;

/**
 * Default customer account self-service implementation.
 * All lookups are based on the authenticated JWT principal so customer pages
 * cannot request or mutate another customer's profile by guessing IDs.
 */
@Service
@RequiredArgsConstructor
public class CustomerAccountServiceImpl implements CustomerAccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Returns the current customer's profile from Account and Customer records.
     */
    @Override
    @Transactional(readOnly = true)
    public CustomerProfileResponse getCurrentProfile() {
        AccountProjection account = findCurrentAccountProjection();
        Customer customer = findActiveCustomer(account.getAccountId());
        return toProfileResponse(account, customer);
    }

    /**
     * Updates name, email, and birth date. Phone remains read-only
     * because it is also a Firebase login identifier in this system.
     */
    @Override
    @Transactional
    public CustomerProfileResponse updateCurrentProfile(CustomerProfileUpdateRequest request) {
        AccountProjection account = findCurrentAccountProjection();
        Customer customer = findActiveCustomer(account.getAccountId());
        String normalizedEmail = request.email();

        if (normalizedEmail != null
                && customerRepository.existsByEmailIgnoreCaseAndCustomerIdNot(normalizedEmail, customer.getCustomerId())) {
            throw new BusinessRuleException("Email đã được tài khoản khác sử dụng.");
        }

        customer.setCustomerName(request.customerName());
        customer.setEmail(normalizedEmail);
        customer.setDob(request.dob());
        customerRepository.save(customer);

        return getCurrentProfile();
    }

    /**
     * Soft-deactivates the current customer account so historical tickets remain
     * intact while future login and refresh-token use are blocked.
     */
    @Override
    @Transactional
    public CustomerAccountActionResponse deactivateCurrentAccount() {
        AccountProjection projection = findCurrentAccountProjection();
        Customer customer = findActiveCustomer(projection.getAccountId());
        Account account = accountRepository.findById(projection.getAccountId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản khách hàng."));

        customer.setActive(false);
        account.setActive(false);
        customerRepository.save(customer);
        accountRepository.save(account);
        refreshTokenRepository.revokeAllByAccount(account);

        return new CustomerAccountActionResponse(true, "Tài khoản đã được vô hiệu hóa.");
    }

    /**
     * Loads the active account projection attached to the current JWT username.
     */
    private AccountProjection findCurrentAccountProjection() {
        AccountProjection account = accountRepository.findByUsernameWithRoles(currentUsername())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản đang đăng nhập."));
        if (!Boolean.TRUE.equals(account.getIsActive())) {
            throw new BusinessRuleException("Tài khoản đã bị khóa.");
        }
        if (account.getRoleNames() == null || !Arrays.asList(account.getRoleNames().split(",")).contains("CUSTOMER")) {
            throw new BusinessRuleException("Tài khoản hiện tại không phải khách hàng.");
        }
        return account;
    }

    /**
     * Loads the active customer entity for the trusted account identifier.
     */
    private Customer findActiveCustomer(Integer accountId) {
        Customer customer = customerRepository.findByAccountId(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ khách hàng."));
        if (!customer.isActive()) {
            throw new BusinessRuleException("Hồ sơ khách hàng đã bị khóa.");
        }
        return customer;
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
     * Converts persistence objects into the profile response used by the page.
     */
    private CustomerProfileResponse toProfileResponse(AccountProjection account, Customer customer) {
        List<String> roles = account.getRoleNames() == null || account.getRoleNames().isBlank()
            ? List.of()
            : Arrays.stream(account.getRoleNames().split(","))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .toList();
        return new CustomerProfileResponse(
            customer.getCustomerId(),
            account.getAccountId(),
            account.getUsername(),
            customer.getCustomerName(),
            customer.getPhone() == null ? null : PhoneNumberUtility.normalizeToLocalFormat(customer.getPhone()),
            customer.getEmail(),
            customer.getDob(),
            account.getAuthProvider(),
            roles
        );
    }
}
