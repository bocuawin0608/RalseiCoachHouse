package com.ralsei.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ralsei.dto.request.customer.CreateCustomerRequest;
import com.ralsei.dto.request.customer.CustomerFilterRequest;
import com.ralsei.dto.request.customer.UpdateCustomerRequest;
import com.ralsei.dto.response.customer.CustomerDetailResponse;
import com.ralsei.dto.response.customer.CustomerListResponse;

/**
 * Service interface for customer management operations.
 */

/**
 * Provides the business service contract for customer.
 */
public interface CustomerService {
    Page<CustomerListResponse> filterCustomers(CustomerFilterRequest filterRequest, Pageable pageable);
    CustomerDetailResponse getCustomerDetail(Integer customerId);
    Integer createCustomer(CreateCustomerRequest request);
    void updateCustomer(Integer customerId, UpdateCustomerRequest request);
    void toggleActive(Integer customerId);
    void deleteCustomer(Integer customerId);
}
