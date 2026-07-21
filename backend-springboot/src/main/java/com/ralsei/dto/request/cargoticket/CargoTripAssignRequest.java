package com.ralsei.dto.request.cargoticket;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Batch request that assigns waiting cargo orders to one scheduled trip.
 */
public class CargoTripAssignRequest {
    @NotEmpty(message = "Phải chọn ít nhất một đơn gửi hàng.")
    @Size(max = 100, message = "Không thể gán quá 100 đơn trong một lần.")
    private List<@NotNull Integer> cargoTicketIds;
}
