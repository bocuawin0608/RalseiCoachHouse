package com.ralsei.dto.request;

import jakarta.validation.constraints.NotBlank;
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
    private String address;
}
