package com.ralsei.dto.request.cargotype;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for the combined cargo type management form.
 *
 * <p>Cargo type name belongs to {@code cargo_type}; unit and price belong to
 * {@code cargo_type_price}. They are submitted together because staff edits the
 * business record as a single surcharge configuration.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoTypeManagementRequest {

    @NotBlank(message = "Tên loại hàng là bắt buộc")
    private String cargoTypeName;

    @NotBlank(message = "Đơn vị tính là bắt buộc")
    private String unit;

    @NotNull(message = "Đơn giá là bắt buộc")
    @DecimalMin(value = "0.0", inclusive = true, message = "Đơn giá không được âm")
    @DecimalMax(value = "9999999999999.99", message = "Đơn giá không được vượt quá 9.999.999.999.999,99")
    @Digits(integer = 13, fraction = 2, message = "Đơn giá chỉ được tối đa 13 chữ số nguyên và 2 chữ số thập phân")
    private BigDecimal pricePerUnit;
}
