package com.ralsei.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ralsei.repository.RouteRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.repository.TripRepository;

@ExtendWith(MockitoExtension.class)
class TripServiceStaffTripInfoTest {

    @Mock private TripRepository tripRepository;
    @Mock private RouteRepository routeRepository;
    @Mock private StaffRepository staffRepository;

    @InjectMocks
    private TripServiceImpl tripService;

    @Test
    void selectedDepartureDateDefinesExactCalendarDayRange() {
        LocalDate selectedDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(2);
        when(tripRepository.findStaffTripInfos(
                any(LocalDate.class),
                any(LocalDate.class),
                any(),
                any(),
                any(),
                any(),
                anyInt(),
                anyInt(),
                anyInt(),
                anyInt(),
                anyInt(),
                anyList(),
                any(),
                any(Pageable.class)))
                .thenReturn(Page.empty());

        tripService.getStaffTripInfos(
                selectedDate, null, null, null, null, null, null, null, 0, 10);

        verify(tripRepository).findStaffTripInfos(
                eq(selectedDate),
                eq(selectedDate.plusDays(1)),
                any(),
                any(),
                any(),
                any(),
                anyInt(),
                anyInt(),
                anyInt(),
                anyInt(),
                anyInt(),
                anyList(),
                any(),
                any(Pageable.class));
    }
}
