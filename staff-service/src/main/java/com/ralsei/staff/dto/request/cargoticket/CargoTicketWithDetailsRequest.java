package com.ralsei.staff.dto.request.cargoticket;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ralsei.staff.dto.request.cargoticketdetail.CargoTicketDetailRequest;

/**
 * Represents the request payload for cargo ticket with details operations.
 */
@Data
@EqualsAndHashCode(callSuper = true)

public class CargoTicketWithDetailsRequest extends CargoTicketRequest {
    @Valid
    @NotEmpty(message = "Details list cannot be empty")
    private List<CargoTicketDetailRequest> details;
}
