package com.ralsei.dto.request.coach;

import java.util.List;

import com.ralsei.model.enums.CoachStatus;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Represents the request payload for coach filter operations.
 */
public record CoachFilterRequest(
    @Size(max = 20, message = "Biển số xe tìm kiếm không được vượt quá 20 ký tự.")
    String licensePlate,
    
    @Size(max = 10, message = "Số lượng trạng thái lọc tối đa 10.")
    List<CoachStatus> statuses,

    @Positive(message = "ID loại xe phải là số dương.")
    Integer coachTypeId,

    @Size(max = 255, message = "Tên tuyến đường tìm kiếm không được vượt quá 255 ký tự.")
    String routeName
) {}
