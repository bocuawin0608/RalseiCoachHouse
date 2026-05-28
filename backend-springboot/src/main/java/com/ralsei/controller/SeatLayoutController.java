package com.ralsei.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.dto.projection.SeatLayoutProjection;
import com.ralsei.dto.request.seatlayout.SeatLayoutFilterRequest;
import com.ralsei.service.SeatLayoutService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/seat-layouts")
@RequiredArgsConstructor
@CrossOrigin("*") //giải quyết cho FE gọi đc, cấu hình tạm ở đây
public class SeatLayoutController {
    
    private final SeatLayoutService seatLayoutService;

    @GetMapping("")
    public ResponseEntity<Page<SeatLayoutProjection>> filterSeatLayouts(
        @Valid @ModelAttribute SeatLayoutFilterRequest filterRequest, Pageable pageable) {
        
        Page<SeatLayoutProjection> responses = seatLayoutService.filterSeatLayouts(filterRequest, pageable);
        
        return ResponseEntity.ok(responses);
    }
    
    
}
