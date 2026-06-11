package com.ralsei.dto.request.CoachAndRouteStop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachStopRequest {
    @NotBlank(message = "Stop point name is required")
    private String stopPointName;

    @NotBlank(message = "Address is required")
    @Pattern(regexp = "^[^,]+,\\s*[^,]+$", message = "Địa chỉ không hợp lệ. Vui lòng nhập theo định dạng: Số nhà tên đường phố, huyện, tỉnh.")
    private String address;

    @NotBlank(message = "City is required")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Thành phố không hợp lệ. Vui lòng nhập theo định dạng: Chỉ chứa chữ cái và khoảng trắng.")
    private String city;
}
