package com.ralsei.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.dto.projection.staff.StaffProjection;
import com.ralsei.dto.projection.cargoticket.CargoTicketStaffOptionProjection;
import com.ralsei.model.Staff;

public interface StaffRepository extends JpaRepository<Staff, Integer> {
    @Query(value = "SELECT staffId, staffName FROM staff WHERE isActive = 1 AND staffPosition = 'TICKET_STAFF' ORDER BY staffName", nativeQuery = true)
    List<CargoTicketStaffOptionProjection> findCargoTicketSellerOptions();

    @Query(value = "SELECT s.staffId, s.staffName, a.username AS username FROM staff s LEFT JOIN account a ON s.accountId = a.accountId WHERE s.isActive = 1 AND s.staffPosition = 'TICKET_STAFF' ORDER BY s.staffName", nativeQuery = true)
    List<CargoTicketStaffOptionProjection> findCargoTicketSellerOptionsWithUsername();

    @Query(value = "SELECT staffId, staffName FROM staff WHERE isActive = 1 AND staffPosition IN ('ATTENDANT', 'TICKET_STAFF') ORDER BY staffName", nativeQuery = true)
    List<CargoTicketStaffOptionProjection> findCargoTicketHandlerOptions();

    @Query(value = "SELECT staffId, staffName FROM staff WHERE isActive = 1 AND staffPosition = 'DRIVER' ORDER BY staffName", nativeQuery = true)
    List<CargoTicketStaffOptionProjection> findCargoTicketDriverOptions();

    boolean existsByStaffIdAndIsActiveTrueAndStaffPosition(int staffId, String staffPosition);

    boolean existsByStaffIdAndIsActiveTrueAndStaffPositionIn(int staffId, java.util.Collection<String> positions);

    java.util.Optional<Staff> findByAccountId(Integer accountId);

    boolean existsByEmailIgnoreCaseAndStaffIdNot(String email, Integer staffId);

    @Query(value = """
                SELECT s.staffName AS staffName
                FROM staff s
                JOIN account a ON a.accountId = s.accountId
                JOIN account_role ar ON ar.accountId = a.accountId
                JOIN role r ON r.roleId = ar.roleId
                WHERE r.roleName = 'TRIP_STAFF' AND s.staffPosition = 'DRIVER'
                AND EXISTS (
                    SELECT 1
                    FROM trip t
                    WHERE t.driverId = s.staffId
                    AND CAST(t.departureTime AS DATE) = :date
                )
            """, nativeQuery = true)
    List<StaffProjection> getStaffNameDropDown(@Param("date") LocalDate date);
}
