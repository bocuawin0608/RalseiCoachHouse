package com.ralsei.staff.dto.response.CoachAndRouteStop;

/**
 * Represents the data transfer object for route dropdown.
 */
public record RouteDropdownDTO(
    Integer routeId,
    String routeName
) {}