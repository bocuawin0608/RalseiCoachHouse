package com.ralsei.dto.request.seatlayout;

import java.math.BigDecimal;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SeatLayoutFilterRequest(
    @Size(max = 100, message = "Tên sơ đồ tìm kiếm không được vượt quá 100 ký tự.")
    String seatLayoutName,

    @PositiveOrZero(message = "Giá thấp nhất phải lớn hơn hoặc bằng 0.")
    BigDecimal minPrice,

    @PositiveOrZero(message = "Giá cao nhất phải lớn hơn hoặc bằng 0.")
    BigDecimal maxPrice,

    @PositiveOrZero(message = "Số ghế tối thiểu không được nhỏ hơn 0.")
    Integer minSeats,

    @PositiveOrZero(message = "Số ghế tối đa không được nhỏ hơn 0.")
    Integer maxSeats,

    Boolean isActive
){}
