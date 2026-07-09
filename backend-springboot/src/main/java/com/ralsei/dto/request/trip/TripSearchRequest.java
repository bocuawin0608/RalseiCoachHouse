package com.ralsei.dto.request.trip;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
public class TripSearchRequest {

    public static final int MAX_PAGE_SIZE = 100;

    @NotNull(message = "Date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    private String route;

    @Min(value = 0, message = "Page index must not be less than zero")
    private int page = 0;

    @Min(value = 1, message = "Page size must not be less than one")
    @Max(value = MAX_PAGE_SIZE, message = "Page size must not exceed 100")
    private int size = 10;
}
