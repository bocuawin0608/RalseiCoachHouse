package com.ralsei.dto.request.trip;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

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

    private TripFilterRequest validRequest() {
        TripFilterRequest request = new TripFilterRequest();
        request.setDate(LocalDate.now().plusDays(1));
        request.setRoute("Hà Nội - Quảng Bình");
        request.setPage(0);
        return request;
    }
}
