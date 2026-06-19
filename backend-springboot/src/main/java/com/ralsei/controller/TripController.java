package com.ralsei.controller;

import com.ralsei.dto.request.trip.TripSearchRequest;
import com.ralsei.dto.request.trip.TripUpdateRequest;
import com.ralsei.dto.projection.trip.TripDetailProjection;
import com.ralsei.dto.projection.trip.TripFilterProjection;
import com.ralsei.dto.projection.trip.TripSummaryProjection;
import com.ralsei.dto.request.trip.TripFilterRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.model.Trip;
import com.ralsei.service.TripService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

@RestController
// NIKO: Rút ngắn gốc URL xuống đây để làm trục điều phối cho cả URL công khai và nội bộ
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TripController {
    private final TripService tripService;

    /***
     * FUNCTION 1: TÌM KIẾM CƠ BẢN TRANG CHỦ
     * URL thực tế giữ nguyên: GET /api/v1/trips/home
     */
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

    /***
     * FUNCTION 2: TÌM KIẾM NÂNG CAO TRANG CHỦ
     * URL thực tế giữ nguyên: GET /api/v1/trips/home?advanced=true
     */
    @GetMapping(value = "/trips/home", params = "advanced=true") // NIKO: Bổ sung thêm /trips vào đây
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
     * FUNCTION 3: ADMIN/MANAGER TẠO CHUYẾN XE MỚI THỦ CÔNG
     * URL ĐÃ ĐỒNG BỘ: POST /api/v1/manager/trips/create
     */
    @PostMapping("/manager/trips/create") // NIKO: Đổi từ /admin/create thành /manager/trips/create
    public ResponseEntity<String> createTrip(@RequestBody Trip trip) {
        String result = tripService.createTrip(trip);

        if (result.contains("Không thể") || result.contains("thất bại")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * FUNCTION 4: ADMIN/MANAGER CẬP NHẬT CHUYẾN XE
     * URL ĐÃ ĐỒNG BỘ: PUT /api/v1/manager/trips/update/{tripId}
     */
    @PutMapping("/manager/trips/update/{tripId}") // NIKO: Đổi thành cấu trúc thống nhất
    public ResponseEntity<String> updateTrip(
            @PathVariable Integer tripId,
            @RequestBody TripUpdateRequest updateRequest) {

        String result = tripService.updateTrip(tripId, updateRequest);

        // NIKO: Chỗ này logic cũ của anh đang bị bẫy: chứa chữ "hoàn thành" (thành công) lại trả về badRequest?
        // Em giữ nguyên để anh tự check lại business logic, chỉ đổi URL thôi nhé!
        if (result.contains("hoàn thành") || result.contains("thất bại")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * FUNCTION 5: ADMIN/MANAGER XÓA MỀM CHUYẾN XE (HỦY CHUYẾN)
     * URL ĐÃ ĐỒNG BỘ: DELETE /api/v1/manager/trips/delete/{tripId}
     */
    @DeleteMapping("/manager/trips/delete/{tripId}") // NIKO: Đổi thành cấu trúc thống nhất
    public ResponseEntity<String> deleteTrip(@PathVariable Integer tripId) {
        String result = tripService.deleteTrip(tripId);

        if (result.contains("không thể") || result.contains("thất bại")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * FUNCTION 6: ADMIN/MANAGER LẤY TOÀN BỘ DANH SÁCH CHUYẾN XE RÚT GỌN THEO NGÀY
     * URL ĐÃ ĐỒNG BỘ: GET /api/v1/manager/trips/summaries
     */
    @GetMapping("/manager/trips/summaries") // NIKO: Đây chính là endpoint cứu rỗi lỗi 404/NoResourceFound ban nãy!
    public ResponseEntity<PagedResponse<TripSummaryProjection>> getAllTripSummaries(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<TripSummaryProjection> response = tripService.getAllTripSummaries(date, page, size);
        return ResponseEntity.ok(response);
    }
}