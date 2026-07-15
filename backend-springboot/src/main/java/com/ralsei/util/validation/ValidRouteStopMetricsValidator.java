package com.ralsei.util.validation;

import com.ralsei.dto.request.CoachAndRouteStop.RouteStopRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

/**
 * Provides the valid route stop metrics validator component for the application.
 */
public class ValidRouteStopMetricsValidator implements ConstraintValidator<ValidRouteStopMetrics, RouteStopRequest> {

    @Override
    /**
     * Returns whether the valid is active.
     *
     * @param request the value supplied for this operation
     * @param context the value supplied for this operation
     *
     * @return {@code true} if the valid is active; otherwise {@code false}
     */
    public boolean isValid(RouteStopRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        int minutes = request.getMinutesFromStart();
        BigDecimal kilometers = request.getKilometersFromStart();

        boolean isBothZero = (minutes == 0 && kilometers != null && kilometers.compareTo(BigDecimal.ZERO) == 0);
        boolean isBothPositive = (minutes > 0 && kilometers != null && kilometers.compareTo(BigDecimal.ZERO) > 0);

        if (!isBothZero && !isBothPositive) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                   .addPropertyNode("kilometersFromStart")
                   .addConstraintViolation();
            return false;
        }

        return true;
    }
}
