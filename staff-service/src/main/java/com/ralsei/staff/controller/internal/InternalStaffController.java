package com.ralsei.staff.controller.internal;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.staff.model.Staff;
import com.ralsei.staff.repository.StaffRepository;

@RestController
@RequestMapping("/api/internal/staff")
public class InternalStaffController {

    private final StaffRepository staffRepository;

    public InternalStaffController(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @GetMapping("/{staffId}")
    public ResponseEntity<Staff> getStaffById(@PathVariable Integer staffId) {
        return staffRepository.findById(staffId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-account/{accountId}")
    public ResponseEntity<Staff> getStaffByAccountId(@PathVariable Integer accountId) {
        return staffRepository.findByAccountId(accountId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Staff>> getStaffBatch(@RequestBody List<Integer> staffIds) {
        if (staffIds == null || staffIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(staffRepository.findAllById(staffIds));
    }
}
