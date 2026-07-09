package com.ralsei.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.model.CargoTicket;
import com.ralsei.model.CargoTicketDetail;
import com.ralsei.model.Coach;
import com.ralsei.model.PassengerTicket;
import com.ralsei.model.PassengerTicketDetail;
import com.ralsei.model.Payment;
import com.ralsei.model.Seat;
import com.ralsei.model.Staff;
import com.ralsei.model.Trip;
import com.ralsei.model.TripSeat;
import com.ralsei.model.enums.PassengerTicketStatus;
import com.ralsei.model.enums.TripSeatStatus;
import com.ralsei.repository.CargoTicketDetailRepository;
import com.ralsei.repository.CargoTicketRepository;
import com.ralsei.repository.CoachRepository;
import com.ralsei.repository.CoachStopRepository;
import com.ralsei.repository.PassengerTicketDetailRepository;
import com.ralsei.repository.PassengerTicketRepository;
import com.ralsei.repository.PaymentRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.repository.TripRepository;
import com.ralsei.repository.TripSeatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripStaffTestDataInitializer implements CommandLineRunner {

    private static final String[] QR_TOKENS = {
        "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6",
        "b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7",
        "c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8",
        "d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9",
    };

    private final TripRepository tripRepository;
    private final TripSeatRepository tripSeatRepository;
    private final StaffRepository staffRepository;
    private final CoachRepository coachRepository;
    private final PassengerTicketRepository passengerTicketRepository;
    private final PassengerTicketDetailRepository passengerTicketDetailRepository;
    private final PaymentRepository paymentRepository;
    private final CargoTicketRepository cargoTicketRepository;
    private final CargoTicketDetailRepository cargoTicketDetailRepository;
    private final CoachStopRepository coachStopRepository;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            LocalDate today = LocalDate.now();

            List<Staff> drivers = staffRepository.findAll().stream()
                    .filter(s -> "DRIVER".equals(s.getStaffPosition()))
                    .toList();

            if (drivers.isEmpty()) {
                log.warn("No drivers found");
                return;
            }

            Staff driver = drivers.get(0);
            Staff attendant = staffRepository.findAll().stream()
                    .filter(s -> "ATTENDANT".equals(s.getStaffPosition()))
                    .findFirst().orElse(null);

            if (attendant == null) {
                log.warn("No attendant found");
                return;
            }

            boolean hasTodayTrips = tripRepository.findAll().stream()
                    .anyMatch(t -> t.getDepartureTime() != null
                            && t.getDepartureTime().toLocalDate().equals(today)
                            && (t.getDriverId() == driver.getStaffId()
                                    || t.getAttendantId() == driver.getStaffId()));

            if (hasTodayTrips) {
                log.info("Test data exists for today");
                return;
            }

            List<Coach> coaches = coachRepository.findAll().stream()
                    .filter(c -> "ACTIVE".equals(c.getStatus()))
                    .toList();
            if (coaches.isEmpty()) {
                log.warn("No active coaches");
                return;
            }

            Coach coach = coaches.get(0);
            List<Seat> seats = coach.getSeats();
            BigDecimal seatPrice = BigDecimal.valueOf(200000);

            LocalDateTime departure = LocalDateTime.now().plusHours(1);
            Trip trip = Trip.builder()
                    .routeId(1)
                    .coachId(coach.getCoachId())
                    .departureTime(departure)
                    .status("SCHEDULED")
                    .driverId(driver.getStaffId())
                    .attendantId(attendant.getStaffId())
                    .build();
            trip = tripRepository.save(trip);

            List<TripSeat> tripSeats = new ArrayList<>();
            for (Seat seat : seats) {
                TripSeat ts = TripSeat.builder()
                        .trip(trip)
                        .seat(seat)
                        .price(seatPrice)
                        .status(TripSeatStatus.AVAILABLE)
                        .build();
                tripSeats.add(tripSeatRepository.save(ts));
            }

            String pickupName = coachStopRepository.findById(1)
                    .map(cs -> cs.getStopPointName()).orElse("Hà Nội");
            String dropoffName = coachStopRepository.findById(4)
                    .map(cs -> cs.getStopPointName()).orElse("Quảng Bình");

            String[] names = { "Nguyễn Văn An", "Trần Thị Bình", "Lê Quốc Cường", "Phạm Thị Dung" };
            String[] phones = { "0911111111", "0922222222", "0933333333", "0944444444" };

            for (int i = 0; i < 4; i++) {
                TripSeat ts = tripSeats.get(i % tripSeats.size());

                PassengerTicket ticket = PassengerTicket.builder()
                        .tripId(trip.getTripId())
                        .ticketCode("AUTO_" + (i + 1))
                        .totalPrice(seatPrice)
                        .pickupStopId(1)
                        .dropoffStopId(4)
                        .pickupStopName(pickupName)
                        .dropoffStopName(dropoffName)
                        .status(PassengerTicketStatus.CONFIRMED)
                        .build();
                ticket = passengerTicketRepository.save(ticket);

                PassengerTicketDetail detail = PassengerTicketDetail.builder()
                        .passengerTicketId(ticket.getPassengerTicketId())
                        .tripSeatId(ts.getTripSeatId())
                        .seatCodeSnapshot(ts.getSeat().getSeatCode())
                        .qrcode(QR_TOKENS[i])
                        .fullName(names[i])
                        .phone(phones[i])
                        .price(seatPrice)
                        .status("CONFIRMED")
                        .build();
                passengerTicketDetailRepository.save(detail);

                ts.setStatus(TripSeatStatus.SOLD);
                tripSeatRepository.save(ts);

                Payment payment = Payment.builder()
                        .passengerTicketId(ticket.getPassengerTicketId())
                        .amount(seatPrice)
                        .paymentMethod("CASH")
                        .transactionId("AUTO_TXN_" + (i + 1))
                        .status("COMPLETED")
                        .paymentTime(LocalDateTime.now())
                        .build();
                paymentRepository.save(payment);
            }

            CargoTicket cargo = CargoTicket.builder()
                    .tripId(trip.getTripId())
                    .senderName("Công ty ABC")
                    .senderPhone("0912345678")
                    .receiverName("Cửa hàng XYZ")
                    .receiverPhone("0987654321")
                    .ticketCode("CG_AUTO_01")
                    .totalPrice(BigDecimal.valueOf(300000))
                    .feePayer("SENDER")
                    .codAmount(BigDecimal.ZERO)
                    .pickupStopId(1)
                    .dropoffStopId(4)
                    .status("RECEIVED")
                    .soldBy(driver.getStaffId())
                    .build();
            cargo = cargoTicketRepository.save(cargo);

            CargoTicketDetail cargoDetail = CargoTicketDetail.builder()
                    .cargoTicketId(cargo.getCargoTicketId())
                    .cargoTypePriceId(1)
                    .description("Hàng hóa test")
                    .quantity(2)
                    .weightKg(BigDecimal.valueOf(10))
                    .dimensionVol(BigDecimal.valueOf(0.5))
                    .calculatedPrice(BigDecimal.valueOf(300000))
                    .build();
            cargoTicketDetailRepository.save(cargoDetail);

            log.info("====== TRIP STAFF TEST DATA INJECTED ======");
            log.info("Trip ID: {}, departure: {}", trip.getTripId(), departure);
            log.info("");
            log.info("QR tokens for today (use any QR generator):");
            for (int i = 0; i < 4; i++) {
                log.info("  {} ({}) → {}", names[i], phones[i], QR_TOKENS[i]);
            }
            log.info("");
            log.info("Cargo: {} (RECEIVED)", cargo.getTicketCode());
            log.info("==========================================");
        } catch (Exception e) {
            log.warn("Failed to inject test data: {}", e.getMessage());
        }
    }
}
