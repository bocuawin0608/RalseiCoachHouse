package com.tuanvm.application;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ralsei.repository.TripRepository;
import com.ralsei.application.Application;

@SpringBootTest(classes = Application.class)
class CargoTicketTripOptionsRepositoryTest {
    @Autowired
    private TripRepository tripRepository;

    @Test
    void resolvesTripsForBothCityDirectionsWithoutSqlErrors() {
        LocalDateTime now = LocalDateTime.now();
        var outbound = tripRepository.findCargoTicketTripOptions(1, 3);
        var inbound = tripRepository.findCargoTicketTripOptions(3, 1);

        assertFalse(outbound.isEmpty());
        assertFalse(inbound.isEmpty());
        assertTrue(outbound.stream().allMatch(trip ->
                "Hà Nội - Quảng Bình".equals(trip.getRouteName())
                        && trip.getPickupTime().isAfter(now)
                        && trip.getPickupStopId() == 1
                        && trip.getDropoffStopId() == 3));
        assertTrue(inbound.stream().allMatch(trip ->
                "Quảng Bình - Hà Nội".equals(trip.getRouteName())
                        && trip.getPickupTime().isAfter(now)
                        && trip.getPickupStopId() == 3
                        && trip.getDropoffStopId() == 1));
    }
}
