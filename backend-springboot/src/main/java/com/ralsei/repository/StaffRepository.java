package com.ralsei.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.dto.projection.staff.StaffListProjection;
import com.ralsei.dto.projection.staff.StaffProjection;
import com.ralsei.model.Staff;

public interface StaffRepository extends JpaRepository<Staff, Integer> {

    Optional<Staff> findByAccountId(Integer accountId);
    boolean existsByAccountId(Integer accountId);
    boolean existsByPhoneIgnoreCase(String phone);
    boolean existsByEmailIgnoreCaseAndStaffIdNot(String email, Integer staffId);
    long countByTicketAgencyId(Integer ticketAgencyId);

    @Query(value = """
        SELECT s.staffId         AS staffId,
            s.staffName          AS staffName,
            s.phone              AS phone,
            s.email              AS email,
            s.cccd               AS cccd,
            s.staffPosition      AS staffPosition,
            s.ticketAgencyId     AS ticketAgencyId,
            ta.ticketAgencyName  AS ticketAgencyName,
            a.username           AS username,
            s.isActive           AS isActive,
            s.dob                AS dob,
            s.hireDate           AS hireDate,
            s.createdAt          AS createdAt
        FROM staff s
        LEFT JOIN ticket_agency ta ON ta.ticketAgencyId = s.ticketAgencyId
        LEFT JOIN account a ON a.accountId = s.accountId
        WHERE (:search IS NULL
            OR LOWER(s.staffName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(ISNULL(s.phone, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(ISNULL(s.email, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(ISNULL(s.cccd, '')) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:isActive IS NULL OR s.isActive = :isActive)
          AND (:staffPosition IS NULL OR s.staffPosition = :staffPosition)
          AND (:ticketAgencyId IS NULL OR s.ticketAgencyId = :ticketAgencyId)
        ORDER BY s.staffId DESC
    """, nativeQuery = true)
    List<StaffListProjection> filterStaff(
        @Param("search") String search,
        @Param("isActive") Boolean isActive,
        @Param("staffPosition") String staffPosition,
        @Param("ticketAgencyId") Integer ticketAgencyId
    );

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
