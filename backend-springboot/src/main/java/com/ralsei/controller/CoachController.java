package com.ralsei.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.dto.request.coach.CoachCreateRequest;
import com.ralsei.dto.request.coach.CoachFilterRequest;
import com.ralsei.dto.request.coach.CoachUpdateInfoRequest;
import com.ralsei.dto.response.coach.CoachEditFormResponse;
import com.ralsei.dto.response.coach.CoachResponse;
import com.ralsei.dto.response.coach.CoachViewDetailResponse;
import com.ralsei.service.CoachService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/coaches")
@RequiredArgsConstructor
@Validated
public class CoachController {
    private final CoachService coachService;

    @GetMapping(path = {"", "/"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<CoachResponse>> filterCoaches(
        @Valid @ModelAttribute CoachFilterRequest filterRequest,
        Pageable pageable
    ) {
        return ResponseEntity.ok(coachService.filterCoaches(filterRequest, pageable));
    }

    @PostMapping(path = {"", "/"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Integer> createCoach(@Valid @RequestBody CoachCreateRequest request) {
        Integer newId = coachService.createCoach(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newId);
    }

    @PutMapping(path = {"/{id:\\d+}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Boolean> updateCoachInfo(@PathVariable @Min(value = 1, message = "ID của Xe phải lớn hơn 0.") Integer id, @Valid @RequestBody CoachUpdateInfoRequest request) {
        return ResponseEntity.ok(coachService.updateCoachInfo(id, request));
    }

    @GetMapping(path = {"/{id:\\d+}/detail/view"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CoachViewDetailResponse> getCoachDetailForView(@PathVariable @Min(value = 1, message = "ID của Xe phải lớn hơn 0.") Integer id) {
        return ResponseEntity.ok(coachService.getCoachDetailForView(id));
    }

    @GetMapping(path = {"/{id:\\d+}/detail/edit"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CoachEditFormResponse> getCoachDetailForEdit(@PathVariable @Min(value = 1, message = "ID của Xe phải lớn hơn 0.") Integer id) {
        return ResponseEntity.ok(coachService.getCoachDetailForEdit(id));
    }
}
