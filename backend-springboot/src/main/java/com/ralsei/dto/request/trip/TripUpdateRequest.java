package com.ralsei.dto.request.trip;

import java.time.LocalDateTime;

/**
 * Complete mutable trip schedule submitted by the staff editor.
 *
 * <p>A record gives Jackson canonical constructor binding for every field. This
 * avoids fluent Lombok setters being mistaken for non-bean methods and silently
 * leaving values such as {@code departureTime} null.</p>
 *
 * @param routeId route assigned to the trip
 * @param driverId available driver identifier
 * @param coachId available coach identifier
 * @param attendantId available attendant identifier
 * @param departureTime local departure date and 24-hour time
 * @param status database trip status
 */
/**
 * Represents the request payload for trip update operations.
 */
public record TripUpdateRequest(
        Integer routeId,
        Integer driverId,
        Integer coachId,
        Integer attendantId,
        LocalDateTime departureTime,
        String status) {
}
