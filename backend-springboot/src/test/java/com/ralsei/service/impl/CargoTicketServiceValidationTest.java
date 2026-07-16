package com.ralsei.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.ralsei.dto.request.cargoticket.CargoTicketRequest;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.model.Staff;
import com.ralsei.repository.CoachStopRepository;
import com.ralsei.repository.CustomerRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.repository.TicketAgencyRepository;
import com.ralsei.repository.TripRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CargoTicketServiceValidationTest {

    @Mock private CoachStopRepository coachStopRepository;
    @Mock private TripRepository tripRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private TicketAgencyRepository ticketAgencyRepository;

    @InjectMocks
    private CargoTicketServiceImpl cargoTicketService;

    private CargoTicketRequest request;
    private Staff currentStaff;

    @BeforeEach
    void setUp() {
        request = new CargoTicketRequest();
        request.setPickupStopId(1);
        request.setDropoffStopId(8);
        request.setSenderName("TestSender");
        request.setSenderPhone("0900000001");
        request.setReceiverName("TestReceiver");
        request.setReceiverPhone("0900000002");
        request.setTotalPrice(BigDecimal.valueOf(100000));
        request.setFeePayer("SENDER");
        request.setCodAmount(BigDecimal.ZERO);

        Staff sellerStaff = new Staff();
        sellerStaff.setStaffId(3);
        request.setSoldBy(sellerStaff);

        currentStaff = new Staff();
        currentStaff.setStaffId(3);
        currentStaff.setStaffPosition("TICKET_STAFF");
        currentStaff.setTicketAgencyId(2);
        currentStaff.setActive(true);
    }

    @Test
    @DisplayName("TEST 1a (Validation): Rejects cargo with agency-less dropoffStopId")
    void testRejectsCargoAtAgencylessDropoff() {
        // stops exist
        when(coachStopRepository.existsById(1)).thenReturn(true);
        when(coachStopRepository.existsById(8)).thenReturn(true);
        // trip exists
        when(tripRepository.existsById(anyInt())).thenReturn(true);
        // staff checks
        when(staffRepository.existsById(anyInt())).thenReturn(true);
        when(staffRepository.existsByStaffIdAndIsActiveTrueAndStaffPosition(anyInt(), anyString()))
                .thenReturn(true);
        when(staffRepository.existsByStaffIdAndIsActiveTrueAndStaffPositionIn(anyInt(), anyList()))
                .thenReturn(true);
        // trip eligibility
        when(tripRepository.isEligibleForAgencyCargo(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(true);

        // pickup stop has agency, dropoff does NOT
        when(ticketAgencyRepository.existsByStopPointIdAndIsActiveTrue(1)).thenReturn(true);
        when(ticketAgencyRepository.existsByStopPointIdAndIsActiveTrue(8)).thenReturn(false);

        var ex = assertThrows(BusinessRuleException.class, () -> {
            cargoTicketService.newCargoTicketValidation_onlyForTest(request, currentStaff);
        });
        assertEquals("Điểm trả hàng không có văn phòng vé đang hoạt động.", ex.getMessage());
    }

    @Test
    @DisplayName("TEST 1b (Validation): Rejects cargo with agency-less pickupStopId")
    void testRejectsCargoAtAgencylessPickup() {
        when(coachStopRepository.existsById(1)).thenReturn(true);
        when(coachStopRepository.existsById(8)).thenReturn(true);
        when(tripRepository.existsById(anyInt())).thenReturn(true);
        when(staffRepository.existsById(anyInt())).thenReturn(true);
        when(staffRepository.existsByStaffIdAndIsActiveTrueAndStaffPosition(anyInt(), anyString()))
                .thenReturn(true);
        when(staffRepository.existsByStaffIdAndIsActiveTrueAndStaffPositionIn(anyInt(), anyList()))
                .thenReturn(true);
        when(tripRepository.isEligibleForAgencyCargo(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(true);

        // pickup stop has NO agency, dropoff does
        when(ticketAgencyRepository.existsByStopPointIdAndIsActiveTrue(1)).thenReturn(false);
        when(ticketAgencyRepository.existsByStopPointIdAndIsActiveTrue(8)).thenReturn(true);

        var ex = assertThrows(BusinessRuleException.class, () -> {
            cargoTicketService.newCargoTicketValidation_onlyForTest(request, currentStaff);
        });
        assertEquals("Điểm nhận hàng không có văn phòng vé đang hoạt động.", ex.getMessage());
    }

    @Test
    @DisplayName("TEST 1c (Validation): Accepts cargo with valid agencies at both stops")
    void testAcceptsCargoWithValidAgencies() {
        when(coachStopRepository.existsById(1)).thenReturn(true);
        when(coachStopRepository.existsById(8)).thenReturn(true);
        when(tripRepository.existsById(anyInt())).thenReturn(true);
        when(staffRepository.existsById(anyInt())).thenReturn(true);
        when(staffRepository.existsByStaffIdAndIsActiveTrueAndStaffPosition(anyInt(), anyString()))
                .thenReturn(true);
        when(staffRepository.existsByStaffIdAndIsActiveTrueAndStaffPositionIn(anyInt(), anyList()))
                .thenReturn(true);
        when(tripRepository.isEligibleForAgencyCargo(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(true);

        // BOTH stops have active agency
        when(ticketAgencyRepository.existsByStopPointIdAndIsActiveTrue(1)).thenReturn(true);
        when(ticketAgencyRepository.existsByStopPointIdAndIsActiveTrue(8)).thenReturn(true);

        assertDoesNotThrow(() -> {
            cargoTicketService.newCargoTicketValidation_onlyForTest(request, currentStaff);
        });
    }
}
