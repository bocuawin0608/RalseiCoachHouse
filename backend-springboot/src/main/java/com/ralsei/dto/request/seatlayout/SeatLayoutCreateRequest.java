package com.ralsei.dto.request.seatlayout;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SeatLayoutCreateRequest(

    @NotBlank(message = "Tên sơ đồ không được để trống.")
    @Size(max = 100, message = "Tên sơ đồ không được vượt quá 100 ký tự.")
    String seatLayoutName,

    @NotNull(message = "Số hàng ghế không được để trống.")
    @Min(value = 1, message = "Số hàng ghế phải lớn hơn hoặc bằng 1.")
    @Max(value = 20, message = "Số hàng ghế của xe không được vượt quá 20 hàng.")
    Integer totalRows,
    
    @NotNull(message = "Số cột ghế không được để trống.")
    @Min(value = 1, message = "Số cột ghế phải lớn hơn hoặc bằng 1.")
    @Max(value = 10, message = "Số cột ghế của xe không được vượt quá 10 cột.")
    Integer totalCols,
    
    @NotNull(message = "Giá tiền không được để trống.")
    @PositiveOrZero(message = "Giá tiền không được nhỏ hơn 0.")
    BigDecimal seatPrice
){}
