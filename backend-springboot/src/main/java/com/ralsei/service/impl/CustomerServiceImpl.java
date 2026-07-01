package com.ralsei.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import com.ralsei.model.Customer;
import com.ralsei.repository.CustomerRepository;
import com.ralsei.service.CustomerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepo;

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerListResponse> filterCustomers(CustomerFilterRequest filterRequest, Pageable pageable) {
        String search = filterRequest != null && filterRequest.search() != null && !filterRequest.search().isBlank()
            ? filterRequest.search().trim() : null;
        Boolean isActive = filterRequest != null ? filterRequest.isActive() : null;

        List<CustomerListProjection> projections = customerRepo.filterCustomers(search, isActive);

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
    public CustomerDetailResponse getCustomerDetail(Integer customerId) {
        Customer customer = customerRepo.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại!"));
        return mapToDetailResponse(customer);
    }

    @Override
    @Transactional
    public Integer createCustomer(CreateCustomerRequest request) {
        if (request.phone() != null && !request.phone().isBlank()
            && customerRepo.existsByPhone(request.phone().trim())) {
            throw new BusinessRuleException("Số điện thoại này đã tồn tại trong hệ thống!");
        }

        Customer customer = Customer.builder()
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
    public void toggleActive(Integer customerId) {
        Customer customer = customerRepo.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại!"));
        customer.setActive(!customer.isActive());
        customerRepo.save(customer);
    }

    @Override
    @Transactional
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
            proj.getCreatedAt()
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
            customer.getUpdatedBy()
        );
    }
}
