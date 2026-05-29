package com.ralsei.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.dto.request.seatlayout.SeatLayoutCreateRequest;
import com.ralsei.dto.request.seatlayout.SeatLayoutFilterRequest;
import com.ralsei.dto.response.seatlayout.SeatLayoutDetailResponse;
import com.ralsei.dto.response.seatlayout.SeatLayoutResponse;
import com.ralsei.service.SeatLayoutService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/seat-layouts")
@RequiredArgsConstructor
@CrossOrigin("*") //giải quyết cho FE gọi đc, cấu hình tạm ở đây
@Validated
public class SeatLayoutController {
    
    private final SeatLayoutService seatLayoutService;

    @GetMapping(path = {"", "/"})
    public ResponseEntity<Page<SeatLayoutResponse>> filterSeatLayouts(
        @Valid @ModelAttribute SeatLayoutFilterRequest filterRequest, Pageable pageable) {
        
        Page<SeatLayoutResponse> responses = seatLayoutService.filterSeatLayouts(filterRequest, pageable);
        return ResponseEntity.ok(responses);
    }
    
    @PostMapping("")
    public ResponseEntity<SeatLayoutResponse> createSeatLayout(@Valid @RequestBody SeatLayoutCreateRequest createRequest) {
        
        SeatLayoutResponse response = seatLayoutService.createSeatLayout(createRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatLayoutDetailResponse> viewSeatLayoutDetail(@PathVariable @Min(value = 1, message = "ID của Sơ đồ ghế phải lớn hơn 0.") Integer id) {
        SeatLayoutDetailResponse response = seatLayoutService.getSeatLayoutDetail(id);
        return ResponseEntity.ok(response);
    }
    
}
