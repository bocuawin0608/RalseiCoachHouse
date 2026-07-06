package com.ralsei.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import com.ralsei.dto.projection.staff.StaffProjection;
import com.ralsei.model.Staff;

public interface StaffRepository extends JpaRepository<Staff, Integer> {

    Optional<Staff> findByAccountId(Integer accountId);
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
