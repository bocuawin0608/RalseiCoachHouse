package com.ralsei.dto.response.CoachAndRouteStop;

/**
 * Represents the data transfer object for route dropdown.
 */
public record RouteDropdownDTO(
    Integer routeId,
    String routeName
) {}