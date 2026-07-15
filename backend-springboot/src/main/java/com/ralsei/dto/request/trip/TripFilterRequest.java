package com.ralsei.dto.request.trip;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Query parameters accepted by the public customer trip-filter endpoint.
 *
 * <p>Validation belongs at this transport boundary because {@code @ModelAttribute}
 * otherwise binds arbitrary strings and floating-point values before the service
 * or repository sees them. The service repeats the important invariants so calls
 * made outside MVC cannot bypass the public contract.</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
/**
 * Represents the request payload for trip filter operations.
 */
public class TripFilterRequest extends TripSearchRequest {
    private static final int MAX_TIME_SLOTS = 4;
    private static final int MAX_LAYOUTS = 3;
    private static final Pattern TIME_RANGE = Pattern.compile(
            "(?:[01]\\d|2[0-3]):[0-5]\\d-(?:[01]\\d|2[0-3]):[0-5]\\d");
    private static final Set<String> NAMED_TIME_SLOTS = Set.of(
            "EARLY_MORNING", "MORNING", "AFTERNOON", "EVENING");

    private List<String> timeSlots;
    private List<String> layouts;

    @PositiveOrZero(message = "Minimum price must not be negative")
    private Double minPrice;

    @PositiveOrZero(message = "Maximum price must not be negative")
    private Double maxPrice;

    /**
     * Validates the flattened comma-separated time-slot representation used by
     * the React client as well as repeated query parameters used by API clients.
     *
     * @return {@code true} when at most four valid, forward time ranges are supplied
     */
    @AssertTrue(message = "Time slots must contain at most four valid HH:mm-HH:mm ranges")
    /**
     * Returns whether the time slots valid is active.
     *
     * @return {@code true} if the time slots valid is active; otherwise {@code false}
     */
    public boolean isTimeSlotsValid() {
        List<String> values = flatten(timeSlots);
        if (values.size() > MAX_TIME_SLOTS) {
            return false;
        }
        return values.stream().allMatch(this::isValidTimeSlot);
    }

    /**
     * Prevents the repository's three fixed layout parameters from silently
     * discarding a fourth customer filter.
     *
     * @return {@code true} when no more than three non-blank layouts are supplied
     */
    @AssertTrue(message = "At most three layouts may be requested")
    /**
     * Returns whether the layouts valid is active.
     *
     * @return {@code true} if the layouts valid is active; otherwise {@code false}
     */
    public boolean isLayoutsValid() {
        return flatten(layouts).size() <= MAX_LAYOUTS;
    }

    /**
     * Rejects non-finite, negative, and reversed price ranges before SQL executes.
     *
     * @return {@code true} when both optional bounds form a safe ascending range
     */
    @AssertTrue(message = "Price range must be finite, non-negative, and minPrice must not exceed maxPrice")
    /**
     * Returns whether the price range valid is active.
     *
     * @return {@code true} if the price range valid is active; otherwise {@code false}
     */
    public boolean isPriceRangeValid() {
        if ((minPrice != null && !Double.isFinite(minPrice))
                || (maxPrice != null && !Double.isFinite(maxPrice))) {
            return false;
        }
        return minPrice == null || maxPrice == null || minPrice <= maxPrice;
    }

    private boolean isValidTimeSlot(String value) {
        if (NAMED_TIME_SLOTS.contains(value)) {
            return true;
        }
        if (!TIME_RANGE.matcher(value).matches()) {
            return false;
        }
        String[] bounds = value.split("-", 2);
        return toMinuteOfDay(bounds[0]) <= toMinuteOfDay(bounds[1]);
    }

    private int toMinuteOfDay(String value) {
        String[] parts = value.split(":", 2);
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private List<String> flatten(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .flatMap(value -> List.of(value.split(",")).stream())
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
