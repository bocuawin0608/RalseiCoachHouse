package com.ralsei.dto.request.seatlayout;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SeatLayoutUpdateInfoRequest(
    @NotBlank(message = "Tên sơ đồ không được để trống.")
    @Size(max = 100, message = "Tên sơ đồ không được vượt quá 100 ký tự.")    
    String seatLayoutName,
    
    @NotNull(message = "Trạng thái hoạt động của sơ đồ ghế không được để trống.")
    Boolean isActive
) {}