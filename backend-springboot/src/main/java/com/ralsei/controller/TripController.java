package com.ralsei.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.dto.projection.trip.TripDetailProjection;
import com.ralsei.dto.projection.trip.TripFilterProjection;
import com.ralsei.dto.projection.trip.TripSummaryProjection;
import com.ralsei.dto.projection.trip.TripStopProjection;
import com.ralsei.dto.request.trip.TripCreateRequest;
import com.ralsei.dto.request.trip.TripFilterRequest;
import com.ralsei.dto.request.trip.TripSearchRequest;
import com.ralsei.dto.request.trip.TripUpdateRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.service.TripService;
import java.util.List;

import lombok.RequiredArgsConstructor;
/***
 * 
 * TripController: nhớ comment vào nhá
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TripController {
    private final TripService tripService;

    @GetMapping(value = "/trips/home", params = "!advanced") // NIKO: Bổ sung thêm /trips vào đây
    public ResponseEntity<PagedResponse<TripDetailProjection>> searchTrips(
            @ModelAttribute TripSearchRequest request) {

        LocalDateTime start = request.getDate().atStartOfDay();
        LocalDateTime end = request.getDate().atTime(23, 59, 59, 999_000_000);

        PagedResponse<TripDetailProjection> response = tripService.getTripDetails(
                start,
                end,
                request.getRoute(),
                request.getPage(),
                request.getSize());
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/trips/home", params = "advanced=true")
    public ResponseEntity<PagedResponse<TripFilterProjection>> filterTrips(
            @ModelAttribute TripFilterRequest request) {

        LocalDateTime start = request.getDate().atStartOfDay();
        LocalDateTime end = request.getDate().atTime(23, 59, 59, 999_000_000);

        PagedResponse<TripFilterProjection> response = tripService.getFilteredTripDetails(
                start,
                end,
                request.getRoute(),
                request.getTimeSlots(),
                request.getLayouts(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getPage(),
                request.getSize());

        return ResponseEntity.ok(response);
    }

    /**
     * Exposes the ordered pickup and drop-off timeline for one public trip.
     *
     * @param tripId concrete trip selected on the customer search page
     * @return route stops derived from that trip's assigned route
     */
    @GetMapping("/trips/{tripId}/stops")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<TripStopProjection>> getTripStops(@PathVariable Integer tripId) {
        return ResponseEntity.ok(tripService.getTripStops(tripId));
    }

    @PostMapping("/manager/trips/create") // NIKO: Đổi từ /admin/create thành /manager/trips/create
    public ResponseEntity<String> insertTrip(@RequestBody TripCreateRequest tripRequest) {
        String result = tripService.insertTrip(tripRequest);

        if (result.contains("Không thể") || result.contains("thất bại")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/manager/trips/update/{tripId}")
    public ResponseEntity<String> updateTrip(
            @PathVariable Integer tripId,
            @RequestBody TripUpdateRequest updateRequest) {

        String result = tripService.updateTrip(tripId, updateRequest);
        if (result.contains("hoàn thành") || result.contains("thất bại")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/manager/trips/delete/{tripId}")
    public ResponseEntity<String> deleteTrip(@PathVariable Integer tripId) {
        String result = tripService.deleteTrip(tripId);

        if (result.contains("không thể") || result.contains("thất bại")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/manager/trips/summaries") // NIKO: Đây chính là endpoint cứu rỗi lỗi 404/NoResourceFound ban nãy!
    public ResponseEntity<PagedResponse<TripSummaryProjection>> getAllTripSummaries(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<TripSummaryProjection> response = tripService.getAllTripSummaries(date, page, size);
        return ResponseEntity.ok(response);
    }
    // --- CÁC ENDPOINT TỰ ĐỘNG LẤY TÀI NGUYÊN DỰA VÀO NGÀY KHỞI HÀNH ---
    //TODO: fix this later
    // @GetMapping("/manager/trips/available-coaches")
    // public ResponseEntity<?> getAvailableCoaches(
    //         @RequestParam Integer routeId,
    //         @RequestParam String departureTime) { // Nhận chuỗi "YYYY-MM-DDTHH:mm:00" từ React

    //     // Bóc tách lấy riêng chuỗi Ngày (YYYY-MM-DD) trước ký tự 'T'
    //     String dateStr = departureTime.split("T")[0];
    //     java.time.LocalDate localDate = java.time.LocalDate.parse(dateStr); // Chuyển thành kiểu LocalDate để truy vấn

    //     // Truyền ngày xuống Service để tìm xe rảnh trong ngày đó
    //     return ResponseEntity.ok(tripService.getAvailableCoachesByDate(routeId, localDate));
    // }
    //TODO: fix this later
    //@GetMapping("/manager/trips/available-drivers")
    // public ResponseEntity<?> getAvailableDrivers(
    //         @RequestParam String departureTime) {

    //     String dateStr = departureTime.split("T")[0];
    //     java.time.LocalDate localDate = java.time.LocalDate.parse(dateStr);

    //     // Truyền ngày xuống Service để tìm tài xế rảnh trong ngày đó
    //     return ResponseEntity.ok(tripService.getAvailableDriversByDate(localDate));
    // }
    // //TODO: fix this later
    // @GetMapping("/manager/trips/available-attendants")
    // public ResponseEntity<?> getAvailableAttendants(
    //         @RequestParam String departureTime) {

    //     String dateStr = departureTime.split("T")[0];
    //     java.time.LocalDate localDate = java.time.LocalDate.parse(dateStr);

    //     // Truyền ngày xuống Service để tìm phụ xe rảnh trong ngày đó
    //     return ResponseEntity.ok(tripService.getAvailableAttendantsByDate(localDate));
    // }
}
