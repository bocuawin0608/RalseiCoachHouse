package com.ralsei.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.dto.request.staff.StaffPasswordChangeRequest;
import com.ralsei.dto.request.staff.StaffProfileUpdateRequest;
import com.ralsei.dto.response.staff.StaffAccountActionResponse;
import com.ralsei.dto.response.staff.StaffProfileResponse;
import com.ralsei.service.StaffAccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Internal staff account self-service endpoints.
 */
@RestController
@RequestMapping("/api/v1/staff/me")
@PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','TICKET_STAFF','TRIP_STAFF')")
@RequiredArgsConstructor
public class StaffAccountController {

    private final StaffAccountService staffAccountService;

    /**
     * Returns the signed-in staff member's profile.
     */
    @GetMapping
    public ResponseEntity<StaffProfileResponse> getCurrentProfile() {
        return ResponseEntity.ok(staffAccountService.getCurrentProfile());
    }

    /**
     * Updates safe editable profile fields for the signed-in staff member.
     */
    @PatchMapping
    public ResponseEntity<StaffProfileResponse> updateCurrentProfile(
        @Valid @RequestBody StaffProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(staffAccountService.updateCurrentProfile(request));
    }

    /**
     * Changes the signed-in local staff password.
     */
    @PostMapping("/password")
    public ResponseEntity<StaffAccountActionResponse> changeCurrentPassword(
        @Valid @RequestBody StaffPasswordChangeRequest request
    ) {
        return ResponseEntity.ok(staffAccountService.changeCurrentPassword(request));
    }
}
