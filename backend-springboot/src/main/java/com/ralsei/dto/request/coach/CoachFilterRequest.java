package com.ralsei.dto.request.coach;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CoachFilterRequest(
    @Size(max = 20, message = "Biển số xe tìm kiếm không được vượt quá 20 ký tự.")
    String licensePlate,
    
    @Size(max = 10, message = "Số lượng trạng thái lọc tối đa 10.")
    List<String> statuses,

    @Positive(message = "ID loại xe phải là số dương.")
    Integer coachTypeId
) {}
