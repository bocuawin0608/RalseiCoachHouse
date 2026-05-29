package com.ralsei.controller;

import com.ralsei.dto.projection.TripDetailProjection;
import com.ralsei.dto.request.TripSearchRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping("/api/v1/trips") 
@RequiredArgsConstructor 
@CrossOrigin(origins = "http://localhost:5173" ) 
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