package com.ralsei.dto.response.coach;

import java.util.List;

public record SeatLayoutDTO(
    int totalFloors,
    int rows,
    int cols,
    List<List<List<String>>> floors
) {}
