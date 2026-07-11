package com.ralsei.dto.request.trip;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * Verifies the public trip-search DTO rejects unsafe pagination, time, and
 * price parameters before the customer repository is invoked.
 */
class TripSearchRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void rejectsPageSizeAbovePublicSearchLimit() {
        TripFilterRequest request = validRequest();
        request.setSize(TripSearchRequest.MAX_PAGE_SIZE + 1);

        Set<ConstraintViolation<TripFilterRequest>> violations = validator.validate(request);

        assertThat(violations)
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("size");
                    assertThat(violation.getMessage()).isEqualTo("Page size must not exceed 100");
                });
    }

    @Test
    void acceptsMaximumPageSize() {
        TripFilterRequest request = validRequest();
        request.setSize(TripSearchRequest.MAX_PAGE_SIZE);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsNegativePrice() {
        TripFilterRequest request = validRequest();
        request.setMinPrice(-1D);

        assertThat(validator.validate(request))
                .anySatisfy(violation -> assertThat(violation.getPropertyPath().toString()).isEqualTo("minPrice"));
    }

    @Test
    void rejectsReversedPriceRange() {
        TripFilterRequest request = validRequest();
        request.setMinPrice(500_000D);
        request.setMaxPrice(100_000D);

        assertThat(validator.validate(request))
                .anySatisfy(violation -> assertThat(violation.getPropertyPath().toString())
                        .isEqualTo("priceRangeValid"));
    }

    @Test
    void rejectsMalformedTimeRange() {
        TripFilterRequest request = validRequest();
        request.setTimeSlots(List.of("25:00-26:00"));

        assertThat(validator.validate(request))
                .anySatisfy(violation -> assertThat(violation.getPropertyPath().toString())
                        .isEqualTo("timeSlotsValid"));
    }

    @Test
    void acceptsValidCustomerFilters() {
        TripFilterRequest request = validRequest();
        request.setTimeSlots(List.of("06:00-12:00"));
        request.setLayouts(List.of("luxury"));
        request.setMinPrice(100_000D);
        request.setMaxPrice(500_000D);

        assertThat(validator.validate(request)).isEmpty();
    }

    private TripFilterRequest validRequest() {
        TripFilterRequest request = new TripFilterRequest();
        request.setDate(LocalDate.now().plusDays(1));
        request.setRoute("Hà Nội - Quảng Bình");
        request.setPage(0);
        return request;
    }
}
