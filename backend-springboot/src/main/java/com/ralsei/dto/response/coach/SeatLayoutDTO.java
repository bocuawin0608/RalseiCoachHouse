package com.ralsei.dto.response.coach;

import java.util.List;

/**
 * Represents the data transfer object for seat layout.
 */
public record SeatLayoutDTO(
    int totalFloors,
    int rows,
    int cols,
    List<List<List<String>>> floors
) {}
