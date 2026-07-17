package com.ralsei.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.model.CargoTicket;
import com.ralsei.dto.projection.cargoticket.CargoResponsibilityProjection;

/**
 * Provides persistence access for cargo ticket data.
 */
public interface CargoTicketRepository extends JpaRepository<CargoTicket, Integer> {
    @Query(value = "SELECT TOP 10 phone, name FROM (SELECT senderPhone as phone, senderName as name FROM cargo_ticket WHERE senderPhone LIKE %:phone% UNION SELECT receiverPhone as phone, receiverName as name FROM cargo_ticket WHERE receiverPhone LIKE %:phone%) AS Contacts", nativeQuery = true)
    List<Object[]> findContactsByPhoneNative(@Param("phone") String phone);

    boolean existsByTicketCodeIgnoreCase(String ticketCode);

    boolean existsByTicketCodeIgnoreCaseAndCargoTicketIdNot(String ticketCode, int cargoTicketId);

    org.springframework.data.domain.Page<CargoTicket> findByStatusNot(String status, Pageable pageable);

    org.springframework.data.domain.Page<CargoTicket> findByStatus(String status, Pageable pageable);

    /** Returns only cargo for which the supplied agency is operationally responsible. */
    @Query(value = """
            SELECT cargo.*
            FROM cargo_ticket cargo
            JOIN staff seller ON seller.staffId = cargo.soldBy
            LEFT JOIN ticket_agency destination
                   ON destination.stopPointId = cargo.dropoffStopId
                  AND destination.isActive = 1
            WHERE cargo.[status] <> 'ABANDONED'
              AND ((cargo.[status] IN ('ARRIVED', 'DELIVERED')
                    AND destination.ticketAgencyId = :ticketAgencyId)
                OR (cargo.[status] NOT IN ('ARRIVED', 'DELIVERED')
                    AND seller.ticketAgencyId = :ticketAgencyId))
            """, countQuery = """
            SELECT COUNT(cargo.cargoTicketId)
            FROM cargo_ticket cargo
            JOIN staff seller ON seller.staffId = cargo.soldBy
            LEFT JOIN ticket_agency destination
                   ON destination.stopPointId = cargo.dropoffStopId
                  AND destination.isActive = 1
            WHERE cargo.[status] <> 'ABANDONED'
              AND ((cargo.[status] IN ('ARRIVED', 'DELIVERED')
                    AND destination.ticketAgencyId = :ticketAgencyId)
                OR (cargo.[status] NOT IN ('ARRIVED', 'DELIVERED')
                    AND seller.ticketAgencyId = :ticketAgencyId))
            """, nativeQuery = true)
    org.springframework.data.domain.Page<CargoTicket> findAllForAgency(
            @Param("ticketAgencyId") int ticketAgencyId,
            Pageable pageable);

    /**
     * Routes a staff queue to the responsible office.
     *
     * <p>The receiving queue also includes legacy/provisional DELIVERED rows
     * that have not been acknowledged by a TICKET_STAFF member. Trip staff can
     * complete their hand-off before destination staff confirms receipt;
     * requiring only ARRIVED made those orders disappear between workflows.</p>
     */
    @Query(value = """
            SELECT cargo.*
            FROM cargo_ticket cargo
            JOIN staff seller ON seller.staffId = cargo.soldBy
            INNER JOIN ticket_agency destination
                   ON destination.stopPointId = cargo.dropoffStopId
                  AND destination.isActive = 1
            WHERE ((:status = 'ARRIVED'
                    AND (cargo.[status] = 'ARRIVED'
                         OR (cargo.[status] = 'DELIVERED' AND NOT EXISTS (
                             SELECT 1
                             FROM staff receiptStaff
                             WHERE receiptStaff.staffId = cargo.deliveredBy
                               AND receiptStaff.staffPosition = 'TICKET_STAFF'
                         ))))
                OR (:status = 'DELIVERED'
                    AND cargo.[status] = 'DELIVERED'
                    AND EXISTS (
                        SELECT 1
                        FROM staff receiptStaff
                        WHERE receiptStaff.staffId = cargo.deliveredBy
                          AND receiptStaff.staffPosition = 'TICKET_STAFF'
                    ))
                OR (:status NOT IN ('ARRIVED', 'DELIVERED') AND cargo.[status] = :status))
              AND (:tripId IS NULL OR cargo.tripId = :tripId)
              AND ((:status IN ('ARRIVED', 'DELIVERED') AND destination.ticketAgencyId = :ticketAgencyId)
                OR (:status NOT IN ('ARRIVED', 'DELIVERED') AND seller.ticketAgencyId = :ticketAgencyId))
            """, countQuery = """
            SELECT COUNT(cargo.cargoTicketId)
            FROM cargo_ticket cargo
            JOIN staff seller ON seller.staffId = cargo.soldBy
            INNER JOIN ticket_agency destination
                   ON destination.stopPointId = cargo.dropoffStopId
                  AND destination.isActive = 1
            WHERE ((:status = 'ARRIVED'
                    AND (cargo.[status] = 'ARRIVED'
                         OR (cargo.[status] = 'DELIVERED' AND NOT EXISTS (
                             SELECT 1
                             FROM staff receiptStaff
                             WHERE receiptStaff.staffId = cargo.deliveredBy
                               AND receiptStaff.staffPosition = 'TICKET_STAFF'
                         ))))
                OR (:status = 'DELIVERED'
                    AND cargo.[status] = 'DELIVERED'
                    AND EXISTS (
                        SELECT 1
                        FROM staff receiptStaff
                        WHERE receiptStaff.staffId = cargo.deliveredBy
                          AND receiptStaff.staffPosition = 'TICKET_STAFF'
                    ))
                OR (:status NOT IN ('ARRIVED', 'DELIVERED') AND cargo.[status] = :status))
              AND (:tripId IS NULL OR cargo.tripId = :tripId)
              AND ((:status IN ('ARRIVED', 'DELIVERED') AND destination.ticketAgencyId = :ticketAgencyId)
                OR (:status NOT IN ('ARRIVED', 'DELIVERED') AND seller.ticketAgencyId = :ticketAgencyId))
            """, nativeQuery = true)
    org.springframework.data.domain.Page<CargoTicket> findStaffQueueByStatusAndAgency(
            @Param("status") String status,
            @Param("ticketAgencyId") int ticketAgencyId,
            @Param("tripId") Integer tripId,
            Pageable pageable);

    /**
     * Preserves the existing unfiltered queue contract for callers that do not
     * select a coach first.
     */
    default org.springframework.data.domain.Page<CargoTicket> findStaffQueueByStatusAndAgency(
            String status, int ticketAgencyId, Pageable pageable) {
        return findStaffQueueByStatusAndAgency(status, ticketAgencyId, null, pageable);
    }

    Optional<CargoTicket> findByCargoTicketIdAndStatusNot(int cargoTicketId, String status);

    // của duc
    Optional<CargoTicket> findByTicketCode(String ticketCode);

    // của duc
    List<CargoTicket> findByTripId(int tripId);

    // của duc
    @Query("SELECT ct FROM CargoTicket ct WHERE ct.tripId = :tripId AND ct.status IN :statuses ORDER BY ct.cargoTicketId ASC")
    List<CargoTicket> findByTripIdAndStatusIn(@Param("tripId") int tripId, @Param("statuses") List<String> statuses);

    /**
     * Resolves the complete responsibility chain for a cargo ticket without
     * serializing lazy JPA relationships into the API response.
     */
    @Query(value = """
            SELECT r.routeName AS routeName, c.licensePlate AS licensePlate,
                   destination.ticketAgencyName AS destinationAgencyName,
                   driver.staffName AS driverName, driver.phone AS driverPhone,
                   driver.cccd AS driverCccd, attendant.staffName AS attendantName,
                   attendant.phone AS attendantPhone, attendant.cccd AS attendantCccd
            FROM cargo_ticket cargo
            LEFT JOIN trip t ON t.tripId = cargo.tripId
            LEFT JOIN route r ON r.routeId = t.routeId
            LEFT JOIN coach c ON c.coachId = t.coachId
            LEFT JOIN staff driver ON driver.staffId = t.driverId
            LEFT JOIN staff attendant ON attendant.staffId = t.attendantId
            LEFT JOIN ticket_agency destination ON destination.stopPointId = cargo.dropoffStopId
            WHERE cargo.cargoTicketId = :cargoTicketId
            """, nativeQuery = true)
    Optional<CargoResponsibilityProjection> findResponsibilityByCargoTicketId(
            @Param("cargoTicketId") int cargoTicketId);
}
