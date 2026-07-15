package com.ralsei.util;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.experimental.UtilityClass;

@UtilityClass
/**
 * Provides utility helpers for time fore cast uti processing.
 */
public class TimeForeCastUtility {
    private static final long CUSTOMER_TRIP_FORECAST_MINUTES = 432L;

    /**
     * Forecasts the customer-route arrival time by adding the fixed business
     * travel window of 7 hours and 12 minutes to the departure time.
     *
     * @param departureTime departure timestamp returned by the trip schedule
     * @return arrival timestamp, or {@code null} when the trip has no departure time
     */
    /**
     * Executes the forecast arrival time operation.
     *
     * @param departureTime the value supplied for this operation
     *
     * @return the operation result
     */
    public static LocalDateTime forecastArrivalTime(LocalDateTime departureTime) {
        if (departureTime == null) {
            return null;
        }
        return departureTime.plusMinutes(CUSTOMER_TRIP_FORECAST_MINUTES);
    }

    /**
     * Returns the human-readable trip duration used by the customer search UI.
     *
     * @return formatted Vietnamese duration label for the forecast window
     */
    /**
     * Executes the forecast duration label operation.
     *
     * @return the operation result
     */
    public static String forecastDurationLabel() {
        Duration duration = Duration.ofMinutes(CUSTOMER_TRIP_FORECAST_MINUTES);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%d giờ %d phút", hours, minutes);
    }
}
