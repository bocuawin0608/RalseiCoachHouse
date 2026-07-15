package com.ralsei.dto.request.cargoticket;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ralsei.dto.request.cargoticketdetail.CargoTicketDetailRequest;

@Data
@EqualsAndHashCode(callSuper = true)
/**
 * Represents the request payload for cargo ticket with details operations.
 */
public class CargoTicketWithDetailsRequest extends CargoTicketRequest {
    @Valid
    @NotEmpty(message = "Details list cannot be empty")
    private List<CargoTicketDetailRequest> details;
}
