package com.tuanvm.controller;

import com.tuanvm.dto.projection.TripDetailProjection;
import com.tuanvm.dto.request.TripSearchRequest;
import com.tuanvm.dto.response.PagedResponse;
import com.tuanvm.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 1. Khai báo đây là API Controller trả về JSON
@RequestMapping("/api/v1/trips") // 2. Định nghĩa gốc URL cho cụm Trip
@RequiredArgsConstructor // 3. Tự inject TripService vào qua constructor
public class TripController {

    private final TripService tripService;
    /***
     * API tìm kiếm chuyến xe theo ngày, tuyến đường, phân trang
     * @param request - chứa start, end, route, page, size
     * @return - PagedResponse<TripDetailProjection> với thông tin chuyến xe chi tiết
     */
    @GetMapping("/home")
    public ResponseEntity<PagedResponse<TripDetailProjection>> searchTrips(
            @ModelAttribute TripSearchRequest request 
    ) {
        PagedResponse<TripDetailProjection> response = tripService.getTripDetails(
                request.getStart(),
                request.getEnd(),
                request.getRoute(),
                request.getPage(),
                request.getSize()
        );
        return ResponseEntity.ok(response);
    }
}