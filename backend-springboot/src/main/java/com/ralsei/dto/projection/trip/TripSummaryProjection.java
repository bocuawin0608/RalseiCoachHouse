package com.ralsei.dto.projection.trip;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Interface Projection ôm trọn 10 trường từ câu lệnh SQL Server của anh.
 * Đúng chuẩn, sạch sẽ, tự động ánh xạ ngầm.
 */
public interface TripSummaryProjection {

    Integer getTripId();

    String getTripStatus(); // Khớp với AS tripStatus

    String getManufacturer();

    String getCoachTypeName();

    String getLicensePlate();

    String getCoachStatus(); // Khớp với AS coachStatus

    LocalDate getDepartureDate(); // CAST sang DATE -> map vào LocalDate ngon lành

    LocalTime getDepartureTime(); // CAST sang TIME -> map vào LocalTime chuẩn bài

    Integer getAvailableSeats(); // Khớp với AS availableSeats

    Integer getTotalSeats(); // Khớp với AS totalSeats
}