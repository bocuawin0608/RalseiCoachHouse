package com.ralsei.dto.projection.route;

/**
 * Customer-search view of one active city served by an active route.
 * This projection is intentionally separate from {@code RouteDropdownDTO},
 * whose established two-field contract is shared by existing consumers.
 */
public interface RouteLocationDropdownProjection {

    /** Identifies the route serving this location. */
    Integer getRouteId();

    /** Returns the existing route display name without changing its DTO contract. */
    String getRouteName();

    /** Returns the normalized source city stored on the active coach stop. */
    String getLocationName();
}
