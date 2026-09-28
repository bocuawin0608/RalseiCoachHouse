package com.ralsei.staff.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ralsei.staff.dto.request.staff.OnboardStaffRequest;
import com.ralsei.staff.dto.request.staff.StaffFilterRequest;
import com.ralsei.staff.dto.request.staff.UpdateStaffRequest;
import com.ralsei.staff.dto.response.staff.OnboardStaffResponse;
import com.ralsei.staff.dto.response.staff.StaffDetailResponse;
import com.ralsei.staff.dto.response.staff.StaffListResponse;

/**
 * Service interface for staff management operations.
 */

/**
 * Provides the business service contract for staff.
 */
public interface StaffService {
    Page<StaffListResponse> filterStaff(StaffFilterRequest filterRequest, Pageable pageable);
    StaffDetailResponse getStaffDetail(Integer staffId);
    void updateStaff(Integer staffId, UpdateStaffRequest request);
    void toggleActive(Integer staffId);
    void deleteStaff(Integer staffId);
    OnboardStaffResponse onboardStaff(OnboardStaffRequest request);
}
