package com.ralsei.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.dto.request.coachtype.CoachTypeCreateRequest;
import com.ralsei.dto.request.coachtype.CoachTypeFilterRequest;
import com.ralsei.dto.request.coachtype.CoachTypeUpdateInfoRequest;
import com.ralsei.dto.request.coachtype.CoachTypeUpdatePriceRequest;
import com.ralsei.dto.request.coachtype.CoachTypeUpdateSeatmapRequest;
import com.ralsei.dto.response.coachtype.CoachTypeDetailResponse;
import com.ralsei.dto.response.coachtype.CoachTypeResponse;
import com.ralsei.service.CoachTypeService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/coach-types")
@RequiredArgsConstructor
@Validated
public class CoachTypeController {
    private final CoachTypeService coachTypeService;

    @GetMapping(path = {"", "/"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<CoachTypeResponse>> filterCoachTypes(
        @Valid @ModelAttribute CoachTypeFilterRequest filterRequest, 
        Pageable pageable
    ) {    
        return ResponseEntity.ok(coachTypeService.filterCoachTypes(filterRequest, pageable));
    } 

    @PostMapping(path = {"", "/"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Integer> createCoachType(@Valid @RequestBody CoachTypeCreateRequest createRequest) {
        Integer newId = coachTypeService.createCoachType(createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(newId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CoachTypeDetailResponse> viewCoachTypeDetail(@PathVariable @Min(value = 1, message = "ID của Loại xe phải lớn hơn 0.") Integer id) {
        return ResponseEntity.ok(coachTypeService.getCoachTypeDetail(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> updateGeneralInfo(
        @PathVariable @Min(value = 1, message = "ID của Loại xe phải lớn hơn 0.") Integer id,
        @Valid @RequestBody CoachTypeUpdateInfoRequest updateRequest
    ) {
        coachTypeService.updateCoachTypeInfo(id, updateRequest);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/price")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> configurePrice(
        @PathVariable @Min(value = 1, message = "ID của Loại xe phải lớn hơn 0.") Integer id,
        @Valid @RequestBody CoachTypeUpdatePriceRequest updateRequest
    ) {
        coachTypeService.updateCoachTypePrice(id, updateRequest);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/seat-layout")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> updateSeatLayout(
        @PathVariable @Min(value = 1, message = "ID của Loại xe phải lớn hơn 0.") Integer id,
        @Valid @RequestBody CoachTypeUpdateSeatmapRequest updateRequest
    ) {
        coachTypeService.updateCoachTypeSeatmap(id, updateRequest);
        return ResponseEntity.noContent().build();
    }
}
