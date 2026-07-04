package com.ralsei.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ralsei.dto.request.staff.StaffFilterRequest;
import com.ralsei.dto.request.staff.UpdateStaffRequest;
import com.ralsei.dto.response.staff.StaffDetailResponse;
import com.ralsei.dto.response.staff.StaffListResponse;

public interface StaffService {
    Page<StaffListResponse> filterStaff(StaffFilterRequest filterRequest, Pageable pageable);
    StaffDetailResponse getStaffDetail(Integer staffId);
    void updateStaff(Integer staffId, UpdateStaffRequest request);
    void toggleActive(Integer staffId);
    void deleteStaff(Integer staffId);
}
