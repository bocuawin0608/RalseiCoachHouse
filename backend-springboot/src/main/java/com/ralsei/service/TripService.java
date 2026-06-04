package com.ralsei.service;

import com.ralsei.dto.projection.TripDetailProjection;
import com.ralsei.dto.projection.TripFilterProjection;
import com.ralsei.dto.response.PagedResponse;
import java.time.LocalDateTime;
import java.util.List;

//TODO: địt mẹ thằng interface cũ này nguy cơ ăn cặc rất cao, vì database mới có thể sẽ khác nhiều so với database cũ, nên có thể sẽ phải sửa lại rất nhiều thứ để phù hợp với database mới, nên tốt nhất là nên phân tích lại cái interface này nhé, vì có thể sẽ phải sửa lại để phù hợp với database mới
public interface TripService {
    PagedResponse<TripDetailProjection> getTripDetails(
            LocalDateTime start,
            LocalDateTime end,
            String route,
            int page,
            int size

    );
    PagedResponse<TripFilterProjection> getFilteredTripDetails(
            LocalDateTime start,
            LocalDateTime end,
            String route,
            List<String> timeSlots,
            List<String> layouts,
            Double minPrice,
            Double maxPrice,
            int page,
            int size
    );

    // PagedResponse<TripDetailProjection> getFilteredTripDetails(
    //     LocalDateTime start,
    //     LocalDateTime end,
    //     String route,
    //     List<String> timeSlots,
    //     List<String> layouts,
    //     Double minPrice,
    //     Double maxPrice,
    //     int page,
    //     int size
    // );
}
