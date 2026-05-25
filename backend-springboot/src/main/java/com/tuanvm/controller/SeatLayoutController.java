package com.tuanvm.controller;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tuanvm.dto.projection.SeatLayoutProjection;
import com.tuanvm.service.SeatLayoutService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/seat-layouts")
@RequiredArgsConstructor
@CrossOrigin("*") //giải quyết cho FE gọi đc
public class SeatLayoutController {
    
    private final SeatLayoutService seatLayoutService;

    @GetMapping("")
    public ResponseEntity<Page<SeatLayoutProjection>> filterSeatLayouts(
        @RequestParam(required = false) String seatLayoutName,
        @RequestParam(required = false) Boolean isActive,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) Integer minSeats,
        @RequestParam(required = false) Integer maxSeats,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<SeatLayoutProjection> responses = seatLayoutService.filterSeatLayouts(seatLayoutName, isActive, minPrice, maxPrice, minSeats, maxSeats, page, size);
        
        return ResponseEntity.ok(responses);
    }
    
    
}
