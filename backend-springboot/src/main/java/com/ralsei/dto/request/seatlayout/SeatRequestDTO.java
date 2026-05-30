package com.ralsei.dto.request.seatlayout;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SeatRequestDTO(
    Integer seatId,

    @NotBlank(message = "Mã ghế không được để trống.")
    @Size(max = 10, message = "Mã ghế không được vượt quá 10 ký tự.")
    String seatCode,

    @NotNull(message = "Số hàng của ghế không được để trống.")
    @Min(value = 1, message = "Số hàng của ghế phải lớn hơn hoặc bằng 1.")
    @Max(value = 20, message = "Số hàng của ghế không được vượt quá 20.")
    Integer rowIndex,

    @NotNull(message = "Số cột của ghế không được để trống.")
    @Min(value = 1, message = "Số cột của ghế phải lớn hơn hoặc bằng 1.")
    @Max(value = 10, message = "Số cột của ghế không được vượt quá 10.")
    Integer colIndex,

    @NotNull(message = "Trạng thái hoạt động của ghế không được để trống.")
    Boolean isActive
) {}