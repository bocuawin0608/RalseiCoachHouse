package com.ralsei.staff.controller.internal;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/staff")
@RequiredArgsConstructor
public class InternalStaffController {
    // GET /{staffId} -> returns StaffInfoDTO
    // GET /by-account/{accountId} -> returns StaffInfoDTO
    // POST /batch -> body: List<Integer> staffIds -> returns List<StaffInfoDTO>
}
