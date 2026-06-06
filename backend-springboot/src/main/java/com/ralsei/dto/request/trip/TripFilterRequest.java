package com.ralsei.dto.request.trip;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripFilterRequest extends TripSearchRequest {
    private List<String> timeSlots;
    private List<String> layouts;
    private Double minPrice;
    private Double maxPrice;
}
