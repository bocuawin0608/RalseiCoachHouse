package com.ralsei.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.service.CargoTicketService;
import com.ralsei.service.impl.CargoTicketServiceImpl;

/**
 * Integration tests for the cargo handoff from trip staff to ticket staff.
 * <p>
 * Requires a running SQL Server database with the test data below.
 * Run with: mvn test -Dtest=CargoHandoffIntegrationTest -Dspring.profiles.active=dev
 * </p>
 *
 * <h3>Test data setup (run in SQL Server before these tests):</h3>
 * <pre>
 * -- Agency A (pickup) at stop 1000
 * INSERT INTO ticket_agency (stopPointId, ticketAgencyName, isActive) VALUES (1000, 'Test Agency Origin', 1);
 * DECLARE @agencyOriginId INT = SCOPE_IDENTITY();
 *
 * -- Agency B (destination) at stop 2000
 * INSERT INTO ticket_agency (stopPointId, ticketAgencyName, isActive) VALUES (2000, 'Test Agency Destination', 1);
 * DECLARE @agencyDestId INT = SCOPE_IDENTITY();
 *
 * -- Staff at origin agency (TICKET_STAFF)
 * INSERT INTO staff (accountId, ticketAgencyId, staffName, phone, staffPosition, hireDate, isActive)
 * VALUES (NULL, @agencyOriginId, 'Test Seller', '0900000001', 'TICKET_STAFF', GETDATE(), 1);
 * DECLARE @sellerId INT = SCOPE_IDENTITY();
 *
 * -- Staff at destination agency (TICKET_STAFF)
 * INSERT INTO staff (accountId, ticketAgencyId, staffName, phone, staffPosition, hireDate, isActive)
 * VALUES (NULL, @agencyDestId, 'Test Receiver', '0900000002', 'TICKET_STAFF', GETDATE(), 1);
 * DECLARE @receiverId INT = SCOPE_IDENTITY();
 *
 * -- Staff at unrelated agency (wrong agency for isolation test)
 * INSERT INTO ticket_agency (stopPointId, ticketAgencyName, isActive) VALUES (3000, 'Test Agency Unrelated', 1);
 * DECLARE @agencyUnrelatedId INT = SCOPE_IDENTITY();
 * INSERT INTO staff (accountId, ticketAgencyId, staffName, phone, staffPosition, hireDate, isActive)
 * VALUES (NULL, @agencyUnrelatedId, 'Test Other', '0900000003', 'TICKET_STAFF', GETDATE(), 1);
 *
 * -- Stops (must exist before ticket_agency references them)
 * INSERT INTO coach_stop (stopPointId, stopPointName, address, city, isActive, latitude, longitude)
 * VALUES (1000, 'Origin Stop', '123 Origin St', 'Hanoi', 1, 21.0, 105.0);
 * INSERT INTO coach_stop (stopPointId, stopPointName, address, city, isActive, latitude, longitude)
 * VALUES (2000, 'Dest Stop', '456 Dest St', 'Saigon', 1, 10.0, 106.0);
 * INSERT INTO coach_stop (stopPointId, stopPointName, address, city, isActive, latitude, longitude)
 * VALUES (3000, 'Other Stop', '789 Other St', 'Danang', 1, 16.0, 108.0);
 * -- Stop WITHOUT any ticket_agency (for negative test)
 * INSERT INTO coach_stop (stopPointId, stopPointName, address, city, isActive, latitude, longitude)
 * VALUES (9999, 'NoAgency Stop', '999 Void St', 'Nowhere', 1, 0.0, 0.0);
 * -- Inactive ticket_agency at a stop (for edge case test)
 * INSERT INTO ticket_agency (stopPointId, ticketAgencyName, isActive) VALUES (4000, 'Test Agency Inactive', 0);
 * INSERT INTO coach_stop (stopPointId, stopPointName, address, city, isActive, latitude, longitude)
 * VALUES (4000, 'Inactive Stop', '111 Dead St', 'Ghost Town', 1, 50.0, 50.0);
 * </pre>
 */
@SpringBootTest
@Transactional
class CargoHandoffIntegrationTest {

    @Autowired
    private CargoTicketRepository cargoTicketRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Pageable DEFAULT_PAGE = PageRequest.of(0, 20, Sort.by("cargoTicketId").descending());

    // ========================================================================
    // TEST 1: Regression — dropoffStopId with no active ticket_agency
    // ========================================================================

    @Test
    @DisplayName("TEST 1 (Regression): findStaffQueueByStatusAndAgency returns 0 rows for cargo at agency-less stop")
    void test1_cargoAtAgencylessStop_isInvisible() {
        // GIVEN: A cargo ticket whose dropoffStopId (9999) has no active ticket_agency
        jdbcTemplate.update("""
            INSERT INTO cargo_ticket (senderName, senderPhone, receiverName, receiverPhone,
                ticketCode, totalPrice, feePayer, codAmount, pickupStopId, dropoffStopId,
                status, soldBy)
            VALUES ('TestSender', '0999999999', 'TestReceiver', '0888888888',
                'CG-TEST1-NOAGENCY', 100000, 'SENDER', 0, 1000, 9999, 'ARRIVED', 1)
            """);

        // WHEN: Ticket staff at the destination agency queries ARRIVED cargo
        int destinationAgencyId = jdbcTemplate.queryForObject(
                "SELECT ticketAgencyId FROM ticket_agency WHERE stopPointId = 2000 AND isActive = 1", Integer.class);
        var result = cargoTicketRepository.findStaffQueueByStatusAndAgency(
                "ARRIVED", destinationAgencyId, DEFAULT_PAGE);

        // THEN: Empty result — INNER JOIN excludes agency-less stops
        assertThat(result.getContent()).as("Cargo at agency-less stop must be invisible").isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    // ========================================================================
    // TEST 2: Happy path — valid dropoff with agency, arrives in destination queue
    // ========================================================================

    @Test
    @DisplayName("TEST 2 (Happy Path): ARRIVED cargo at valid destination stop appears in queue")
    void test2_validDropoff_appearsInDestinationQueue() {
        // GIVEN: Cargo with dropoffStopId=2000 (has active Test Agency Destination)
        jdbcTemplate.update("""
            INSERT INTO cargo_ticket (senderName, senderPhone, receiverName, receiverPhone,
                ticketCode, totalPrice, feePayer, codAmount, pickupStopId, dropoffStopId,
                status, soldBy)
            VALUES ('TestSender', '0999999999', 'TestReceiver', '0888888888',
                'CG-TEST2-ARRIVED', 100000, 'SENDER', 0, 1000, 2000, 'ARRIVED', 1)
            """);

        // WHEN: Destination ticket staff queries ARRIVED cargo
        int destinationAgencyId = jdbcTemplate.queryForObject(
                "SELECT ticketAgencyId FROM ticket_agency WHERE stopPointId = 2000 AND isActive = 1", Integer.class);
        var result = cargoTicketRepository.findStaffQueueByStatusAndAgency(
                "ARRIVED", destinationAgencyId, DEFAULT_PAGE);

        // THEN: Cargo IS visible at the destination agency
        assertThat(result.getContent()).as("ARRIVED cargo at valid stop must appear").isNotEmpty();
        assertThat(result.getContent()).anyMatch(
                t -> "CG-TEST2-ARRIVED".equals(t.getTicketCode()));
    }

    // ========================================================================
    // TEST 3: Wrong agency isolation — ARRIVED cargo NOT visible to other agency
    // ========================================================================

    @Test
    @DisplayName("TEST 3 (Isolation): ARRIVED cargo must NOT appear for a different agency")
    void test3_wrongAgency_cannotSeeArrivedCargo() {
        // GIVEN: Cargo destined for agency at stop 2000
        jdbcTemplate.update("""
            INSERT INTO cargo_ticket (senderName, senderPhone, receiverName, receiverPhone,
                ticketCode, totalPrice, feePayer, codAmount, pickupStopId, dropoffStopId,
                status, soldBy)
            VALUES ('TestSender', '0999999999', 'TestReceiver', '0888888888',
                'CG-TEST3-ISOLATION', 100000, 'SENDER', 0, 1000, 2000, 'ARRIVED', 1)
            """);

        // WHEN: A DIFFERENT agency (unrelated, stop 3000) queries ARRIVED cargo
        int unrelatedAgencyId = jdbcTemplate.queryForObject(
                "SELECT ticketAgencyId FROM ticket_agency WHERE stopPointId = 3000 AND isActive = 1", Integer.class);
        var result = cargoTicketRepository.findStaffQueueByStatusAndAgency(
                "ARRIVED", unrelatedAgencyId, DEFAULT_PAGE);

        // THEN: Cargo NOT visible — it belongs to a different destination agency
        assertThat(result.getContent()).as("ARRIVED cargo must NOT leak to other agencies").isEmpty();
    }

    // ========================================================================
    // TEST 4: DELIVERED routing fix — DELIVERED cargo visible at destination
    // ========================================================================

    @Test
    @DisplayName("TEST 4 (DELIVERED fix): DELIVERED cargo appears under destination agency, NOT seller")
    void test4_deliveredCargo_routesToDestinationNotSeller() {
        // GIVEN: Cargo at stop 2000, marked DELIVERED
        jdbcTemplate.update("""
            INSERT INTO cargo_ticket (senderName, senderPhone, receiverName, receiverPhone,
                ticketCode, totalPrice, feePayer, codAmount, pickupStopId, dropoffStopId,
                status, soldBy)
            VALUES ('TestSender', '0999999999', 'TestReceiver', '0888888888',
                'CG-TEST4-DELIVERED', 100000, 'SENDER', 0, 1000, 2000, 'DELIVERED', 1)
            """);

        int destinationAgencyId = jdbcTemplate.queryForObject(
                "SELECT ticketAgencyId FROM ticket_agency WHERE stopPointId = 2000 AND isActive = 1", Integer.class);
        int originAgencyId = jdbcTemplate.queryForObject(
                "SELECT ticketAgencyId FROM ticket_agency WHERE stopPointId = 1000 AND isActive = 1", Integer.class);

        // WHEN: Destination agency queries DELIVERED cargo
        var destResult = cargoTicketRepository.findStaffQueueByStatusAndAgency(
                "DELIVERED", destinationAgencyId, DEFAULT_PAGE);

        // THEN: DELIVERED cargo IS visible to destination agency
        assertThat(destResult.getContent()).as("DELIVERED cargo must be visible to destination agency").isNotEmpty();
        assertThat(destResult.getContent()).anyMatch(
                t -> "CG-TEST4-DELIVERED".equals(t.getTicketCode()));

        // WHEN: Origin (seller) agency queries DELIVERED cargo
        var sellerResult = cargoTicketRepository.findStaffQueueByStatusAndAgency(
                "DELIVERED", originAgencyId, DEFAULT_PAGE);

        // THEN: DELIVERED cargo is NOT visible to the seller's agency
        assertThat(sellerResult.getContent()).as("DELIVERED cargo must NOT leak to seller's agency").isEmpty();
    }

    // ========================================================================
    // TEST 5: Cleanup detection — count rows affected (run this SQL manually)
    // ========================================================================

    @Test
    @DisplayName("TEST 5 (Cleanup): Manual SQL to find orphaned cargo tickets")
    void test5_cleanupQuery_countsOrphanedRows() {
        // This test runs the cleanup query and prints the count.
        // MANUAL STEP: Run the SQL in cargo_agency_cleanup.sql for a full result set.
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM cargo_ticket ct
            WHERE NOT EXISTS (
                SELECT 1 FROM ticket_agency ta
                WHERE ta.stopPointId = ct.dropoffStopId AND ta.isActive = 1
            )
            """, Integer.class);

        System.out.println("=== TEST 5: Orphaned cargo tickets count: " + count + " ===");

        // If count > 0, review and clean up manually:
        if (count != null && count > 0) {
            var rows = jdbcTemplate.queryForList("""
                SELECT ct.cargoTicketId, ct.ticketCode, ct.dropoffStopId, ct.status
                FROM cargo_ticket ct
                WHERE NOT EXISTS (
                    SELECT 1 FROM ticket_agency ta
                    WHERE ta.stopPointId = ct.dropoffStopId AND ta.isActive = 1
                )
                ORDER BY ct.cargoTicketId
                """);
            rows.forEach(row -> System.out.println(
                    "  ORPHANED: id=" + row.get("cargoTicketId")
                    + " code=" + row.get("ticketCode")
                    + " dropoffStopId=" + row.get("dropoffStopId")
                    + " status=" + row.get("status")));
        }

        assertThat(count).isNotNull();
        System.out.println("=== TEST 5: Cleanup check complete ===");
    }

    // ========================================================================
    // TEST 6: Edge case — ticket_agency exists but isActive = 0
    // ========================================================================

    @Test
    @DisplayName("TEST 6 (Edge case): Cargo at inactive-agency stop is correctly excluded")
    void test6_inactiveAgency_cargoIsExcluded() {
        // GIVEN: Cargo at stop 4000 where ticket_agency has isActive = 0
        jdbcTemplate.update("""
            INSERT INTO cargo_ticket (senderName, senderPhone, receiverName, receiverPhone,
                ticketCode, totalPrice, feePayer, codAmount, pickupStopId, dropoffStopId,
                status, soldBy)
            VALUES ('TestSender', '0999999999', 'TestReceiver', '0888888888',
                'CG-TEST6-INACTIVE', 100000, 'SENDER', 0, 1000, 4000, 'ARRIVED', 1)
            """);

        // Confirm the ticket_agency at stop 4000 is indeed inactive
        Boolean isActive = jdbcTemplate.queryForObject(
                "SELECT isActive FROM ticket_agency WHERE stopPointId = 4000", Boolean.class);
        assertThat(isActive).as("Test data: agency at stop 4000 must be inactive").isFalse();

        // WHEN: Any agency queries ARRIVED cargo
        int someAgencyId = jdbcTemplate.queryForObject(
                "SELECT TOP 1 ticketAgencyId FROM ticket_agency WHERE isActive = 1", Integer.class);
        var result = cargoTicketRepository.findStaffQueueByStatusAndAgency(
                "ARRIVED", someAgencyId, DEFAULT_PAGE);

        // THEN: Cargo is excluded — INNER JOIN condition requires isActive = 1
        assertThat(result.getContent()).as("Cargo at inactive-agency stop must be excluded").isEmpty();
    }
}
