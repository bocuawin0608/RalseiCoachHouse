package com.ralsei.util.validation;

import com.ralsei.dto.request.CoachAndRouteStop.RouteStopRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class ValidRouteStopMetricsValidator implements ConstraintValidator<ValidRouteStopMetrics, RouteStopRequest> {

    @Override
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
