package com.ralsei.controller;

import com.ralsei.dto.request.trip.TripSearchRequest;
import com.ralsei.dto.projection.trip.TripDetailProjection;
import com.ralsei.dto.projection.trip.TripFilterProjection;
import com.ralsei.dto.request.trip.TripFilterRequest; // Import con Request mở rộng vào đây
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.service.TripService;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TripController {
    private final TripService tripService;
/***
 * FUNCTION 1: TÌM KIẾM CƠ BẢN TRANG CHỦ (KÍCH HOẠT KHI URL KHÔNG CÓ THAM SỐ "advanced=true")
 * Sử dụng con Request cơ bản TripSearchRequest để nhận các tham số tìm kiếm cơ bản như ngày đi, tuyến đường, phân trang
 * Các tham số này sẽ được truyền xuống service và repository để thực hiện truy vấn cơ bản mà không có bộ lọc nâng cao
 * @param request - Đối tượng request chứa các tham số tìm kiếm cơ bản, đã được Spring tự động ánh xạ từ query parameters của URL
 * @return ResponseEntity chứa PagedResponse với TripDetailProjection, trả về kết quả tìm kiếm cơ bản không có bộ lọc nâng cao
 * Lưu ý: Cần đảm bảo rằng các tham số trong TripSearchRequest phải khớp chính xác với những gì repository và service đang mong đợi để tránh lỗi khi 
 * thực hiện
 */
    @GetMapping(value = "/home", params = "!advanced")
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
 * FUNCTION 2: TÌM KIẾM NÂNG CAO TRANG CHỦ (KÍCH HOẠT KHI URL CÓ THAM SỐ "advanced=true")
 * Sử dụng con Request mở rộng TripFilterRequest để nhận thêm các tham số lọc nâng cao
 * Các tham số này sẽ được truyền xuống service và repository để thực hiện truy vấn với bộ
 * @param request - Đối tượng request chứa tất cả tham số lọc nâng cao, đã được Spring tự động ánh xạ từ query parameters của URL
 * @return  ResponseEntity chứa PagedResponse với TripFilterProjection, trả về kết quả tìm kiếm đã được lọc nâng cao
 * Lưu ý: Cần đảm bảo rằng các tham số trong TripFilterRequest phải khớp chính xác với những gì repository và service đang mong đợi để tránh lỗi khi thực hiện 
 * truy vấn 
 */
    @GetMapping(value = "/home", params = "advanced=true")
    public ResponseEntity<PagedResponse<TripFilterProjection>> filterTrips(
            @ModelAttribute TripFilterRequest request) { // Đổi sang con Request mở rộng ở đây
        
        LocalDateTime start = request.getDate().atStartOfDay();
        LocalDateTime end = request.getDate().atTime(23, 59, 59, 999_000_000);
        
        // Cần gì có nấy, các method getter đã xuất hiện hoàn chỉnh!
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
}