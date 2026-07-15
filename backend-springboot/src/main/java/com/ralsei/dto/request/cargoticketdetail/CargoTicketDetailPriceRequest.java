package com.ralsei.dto.request.cargoticketdetail;

import java.math.BigDecimal;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
/**
 * Represents the request payload for cargo ticket detail price operations.
 */
public class CargoTicketDetailPriceRequest {
    @NotNull(message = "Loại hàng hóa không được để trống")
    private Integer cargoTypePriceId;

    @NotNull(message = "Thể tích không được để trống")
    @Min(value = 0, message = "Thể tích phải lớn hơn hoặc bằng 0")
    private BigDecimal dimensionVol;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
    private Integer quantity;
}
