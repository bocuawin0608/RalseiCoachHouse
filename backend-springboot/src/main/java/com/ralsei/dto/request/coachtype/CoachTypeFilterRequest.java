package com.ralsei.dto.request.coachtype;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CoachTypeFilterRequest(
    @Size(max = 100, message = "Tên sơ đồ tìm kiếm không được vượt quá 100 ký tự.")
    String coachTypeName,

    @PositiveOrZero(message = "Giá thấp nhất phải lớn hơn hoặc bằng 0.")
    @DecimalMax(value = "100000000", message = "Giá thấp nhất không được vượt quá 100.000.000 đ.")
    BigDecimal minPrice,

    @PositiveOrZero(message = "Giá cao nhất phải lớn hơn hoặc bằng 0.")
    @DecimalMax(value = "100000000", message = "Giá cao nhất không được vượt quá 100.000.000 đ.")
    BigDecimal maxPrice,

    @PositiveOrZero(message = "Số ghế tối thiểu không được nhỏ hơn 0.")
    @Max(value = 200, message = "Số ghế tối thiểu không được vượt quá 200.")
    Integer minSeats,

    @PositiveOrZero(message = "Số ghế tối đa không được nhỏ hơn 0.")
    @Max(value = 200, message = "Số ghế tối đa không được vượt quá 200.")
    Integer maxSeats,

    Boolean isActive
) {}
