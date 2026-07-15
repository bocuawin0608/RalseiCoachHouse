package com.ralsei.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.CustomerListProjection;
import com.ralsei.dto.request.customer.CreateCustomerRequest;
import com.ralsei.dto.request.customer.CustomerFilterRequest;
import com.ralsei.dto.request.customer.UpdateCustomerRequest;
import com.ralsei.dto.response.customer.CustomerDetailResponse;
import com.ralsei.dto.response.customer.CustomerListResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Account;
import com.ralsei.model.AccountRole;
import com.ralsei.model.Customer;
import com.ralsei.model.PassengerTicket;
import com.ralsei.model.Role;
import com.ralsei.repository.AccountRepository;
import com.ralsei.repository.AccountRoleRepository;
import com.ralsei.repository.CustomerRepository;
import com.ralsei.repository.PassengerTicketRepository;
import com.ralsei.repository.RoleRepository;
import com.ralsei.service.CustomerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/**
 * Provides the customer service impl component for the application.
 */
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepo;
    private final AccountRepository accountRepo;
    private final AccountRoleRepository accountRoleRepo;
    private final RoleRepository roleRepo;
    private final PassengerTicketRepository passengerTicketRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    /**
     * Filters the customers records.
     *
     * @param filterRequest the value supplied for this operation
     * @param pageable the value supplied for this operation
     *
     * @return the filtered results
     */
    public Page<CustomerListResponse> filterCustomers(CustomerFilterRequest filterRequest, Pageable pageable) {
        String search = filterRequest != null && filterRequest.search() != null && !filterRequest.search().isBlank()
            ? filterRequest.search().trim() : null;
        Boolean isActive = filterRequest != null ? filterRequest.isActive() : null;
        String accountType = filterRequest != null && filterRequest.accountType() != null && !filterRequest.accountType().isBlank()
            ? filterRequest.accountType().trim() : null;
        String activity = filterRequest != null && filterRequest.activity() != null && !filterRequest.activity().isBlank()
            ? filterRequest.activity().trim() : null;

        List<CustomerListProjection> projections = customerRepo.filterCustomers(search, isActive, accountType, activity);

        List<CustomerListResponse> responses = projections.stream()
            .map(this::mapToListResponse)
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<CustomerListResponse> pageContent = start < responses.size() ? responses.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * Returns the customer detail.
     *
     * @param customerId the value supplied for this operation
     *
     * @return the customer detail
     */
    public CustomerDetailResponse getCustomerDetail(Integer customerId) {
        Customer customer = customerRepo.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại!"));

        List<PassengerTicket> tickets = passengerTicketRepo.findByCustomerIdOrderByCreatedAtDesc(customerId);

        List<CustomerDetailResponse.CustomerBookingHistory> bookings = tickets.stream()
            .map(t -> new CustomerDetailResponse.CustomerBookingHistory(
                (long) t.getPassengerTicketId(),
                t.getTicketCode(),
                t.getCreatedAt(),
                t.getTotalPrice(),
                null,
                t.getStatus() != null ? t.getStatus().name() : null
            ))
            .collect(Collectors.toList());

        long totalTrips = bookings.size();
        java.math.BigDecimal totalSpent = tickets.stream()
            .map(PassengerTicket::getTotalPrice)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        LocalDateTime lastBooking = tickets.isEmpty() ? null : tickets.get(0).getCreatedAt();

        return new CustomerDetailResponse(
            customer.getCustomerId(),
            customer.getCustomerName(),
            customer.getPhone(),
            customer.getEmail(),
            customer.getDob(),
            customer.isActive(),
            customer.getCreatedAt(),
            customer.getCreatedBy(),
            customer.getUpdatedAt(),
            customer.getUpdatedBy(),
            customer.getAccountId(),
            totalTrips,
            totalSpent,
            lastBooking,
            bookings
        );
    }

    @Override
    @Transactional
    /**
     * Creates the customer.
     *
     * @param request the value supplied for this operation
     *
     * @return the created customer
     */
    public Integer createCustomer(CreateCustomerRequest request) {
        if (request.phone() != null && !request.phone().isBlank()
            && customerRepo.existsByPhone(request.phone().trim())) {
            throw new BusinessRuleException("Số điện thoại này đã tồn tại trong hệ thống!");
        }

        // Auto-create an account so accountId is never null (DB unique constraint)
        String username = request.phone() != null ? request.phone().trim() : "kh_" + System.currentTimeMillis();
        if (accountRepo.existsByUsername(username)) {
            username = username + "_" + System.currentTimeMillis();
        }
        Account account = Account.builder()
            .username(username)
            .passwordHash(passwordEncoder.encode("123456"))
            .authProvider("local")
            .isActive(true)
            .build();
        account = accountRepo.save(account);

        Role customerRole = roleRepo.findByRoleName("CUSTOMER")
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role CUSTOMER trong hệ thống!"));
        AccountRole ar = AccountRole.builder()
            .accountId(account.getAccountId())
            .roleId(customerRole.getRoleId())
            .build();
        accountRoleRepo.save(ar);

        Customer customer = Customer.builder()
            .accountId(account.getAccountId())
            .customerName(request.customerName().trim())
            .phone(request.phone() != null ? request.phone().trim() : null)
            .email(request.email() != null ? request.email().trim() : null)
            .dob(request.dob())
            .isActive(true)
            .build();

        return customerRepo.save(customer).getCustomerId();
    }

    @Override
    @Transactional
    /**
     * Updates the customer.
     *
     * @param customerId the value supplied for this operation
     * @param request the value supplied for this operation
     */
    public void updateCustomer(Integer customerId, UpdateCustomerRequest request) {
        Customer customer = customerRepo.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại!"));

        String phone = request.phone() != null ? request.phone().trim() : null;
        if (phone != null && !phone.isBlank()
            && customerRepo.existsByPhoneAndCustomerIdNot(phone, customerId)) {
            throw new BusinessRuleException("Số điện thoại này đã tồn tại trong hệ thống!");
        }

        customer.setCustomerName(request.customerName().trim());
        customer.setPhone(phone);
        customer.setEmail(request.email() != null ? request.email().trim() : null);
        customer.setDob(request.dob());
        if (request.isActive() != null) {
            customer.setActive(request.isActive());
        }
        customerRepo.save(customer);
    }

    @Override
    @Transactional
    /**
     * Executes the toggle active operation.
     *
     * @param customerId the value supplied for this operation
     */
    public void toggleActive(Integer customerId) {
        Customer customer = customerRepo.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại!"));
        customer.setActive(!customer.isActive());
        customerRepo.save(customer);

        if (customer.getAccountId() != null) {
            accountRepo.findById(customer.getAccountId()).ifPresent(acc -> {
                acc.setActive(customer.isActive());
                accountRepo.save(acc);
            });
        }
    }

    @Override
    @Transactional
    /**
     * Deletes the customer.
     *
     * @param customerId the value supplied for this operation
     */
    public void deleteCustomer(Integer customerId) {
        Customer customer = customerRepo.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại!"));

        long ticketCount = customerRepo.countTicketsByCustomerId(customerId);
        if (ticketCount > 0) {
            throw new BusinessRuleException(
                "Không thể xóa khách hàng đã có " + ticketCount + " vé. Vui lòng vô hiệu hóa thay vì xóa!");
        }

        customerRepo.delete(customer);
    }

    private CustomerListResponse mapToListResponse(CustomerListProjection proj) {
        return new CustomerListResponse(
            proj.getCustomerId(),
            proj.getCustomerName(),
            proj.getPhone(),
            proj.getEmail(),
            null,
            proj.getIsActive() != null && proj.getIsActive(),
            proj.getCreatedAt(),
            proj.getAccountId(),
            proj.getTotalTrips() != null ? proj.getTotalTrips() : 0L,
            proj.getTotalSpent() != null ? proj.getTotalSpent() : java.math.BigDecimal.ZERO,
            proj.getLastBooking()
        );
    }

    private CustomerDetailResponse mapToDetailResponse(Customer customer) {
        return new CustomerDetailResponse(
            customer.getCustomerId(),
            customer.getCustomerName(),
            customer.getPhone(),
            customer.getEmail(),
            customer.getDob(),
            customer.isActive(),
            customer.getCreatedAt(),
            customer.getCreatedBy(),
            customer.getUpdatedAt(),
            customer.getUpdatedBy(),
            customer.getAccountId(),
            0L,
            java.math.BigDecimal.ZERO,
            null,
            List.of()
        );
    }
}
