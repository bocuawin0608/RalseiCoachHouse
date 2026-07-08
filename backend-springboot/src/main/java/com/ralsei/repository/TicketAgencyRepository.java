package com.ralsei.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.dto.projection.TicketAgencyListProjection;
import com.ralsei.model.TicketAgency;

/**
 * Repository interface for {@link com.ralsei.model.TicketAgency} entity.
 */

public interface TicketAgencyRepository extends JpaRepository<TicketAgency, Integer> {

    boolean existsByTicketAgencyNameIgnoreCase(String ticketAgencyName);

    @Query(value = """
        SELECT ta.ticketAgencyId   AS ticketAgencyId,
            ta.ticketAgencyName AS ticketAgencyName,
            ta.stopPointId      AS stopPointId,
            cs.stopPointName    AS stopPointName,
            cs.city             AS city,
            ta.isActive         AS isActive,
            (SELECT COUNT(*) FROM staff s WHERE s.ticketAgencyId = ta.ticketAgencyId) AS staffCount
        FROM ticket_agency ta
        LEFT JOIN coach_stop cs ON cs.stopPointId = ta.stopPointId
        WHERE (:search IS NULL OR LOWER(ta.ticketAgencyName) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:isActive IS NULL OR ta.isActive = :isActive)
        ORDER BY ta.ticketAgencyId DESC
    """, nativeQuery = true)
    List<TicketAgencyListProjection> filterTicketAgencies(@Param("search") String search, @Param("isActive") Boolean isActive);
}
