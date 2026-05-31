package com.ralsei.controller;
import com.ralsei.dto.projection.TripDetailProjection;
import com.ralsei.dto.request.TripSearchRequest;
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
@RestController // 1. Khai báo đây là API Controller trả về JSON
@RequestMapping("/api/v1/trips") // 2. Định nghĩa gốc URL cho cụm Trip
@RequiredArgsConstructor // 3. Tự inject TripService vào qua constructor
@CrossOrigin(origins = "http://localhost:5173" ) // Hoặc cổng port React của bạn
public class TripController {
private final TripService tripService;
    /***
     * API tìm kiếm chuyến xe theo ngày, tuyến đường, phân trang
     * @param request - chứa date, route, page, size
     * @return - PagedResponse<TripDetailProjection> với thông tin chuyến xe chi tiết
     */
@GetMapping("/home")
public ResponseEntity<PagedResponse<TripDetailProjection>> searchTrips(
        @ModelAttribute TripSearchRequest request
) {
LocalDateTime start = request.getDate().atStartOfDay();
LocalDateTime end   = request.getDate().atTime(23, 59, 59, 999_000_000);
PagedResponse<TripDetailProjection> response = tripService.getTripDetails(
start,
end,
request.getRoute(),
request.getPage(),
request.getSize()
    );
return ResponseEntity.ok(response);

}
}