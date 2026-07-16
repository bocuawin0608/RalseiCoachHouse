-- ============================================================================
-- Cleanup Detection Query: find cargo tickets with no active ticket_agency
-- at their dropoff stop
-- ============================================================================
SELECT ct.cargoTicketId, ct.ticketCode, ct.dropoffStopId, ct.status,
       cs.stopPointName AS dropoffStopName
FROM cargo_ticket ct
LEFT JOIN coach_stop cs ON cs.stopPointId = ct.dropoffStopId
WHERE NOT EXISTS (
    SELECT 1 FROM ticket_agency ta 
    WHERE ta.stopPointId = ct.dropoffStopId AND ta.isActive = 1
)
ORDER BY ct.cargoTicketId;

-- ============================================================================
-- Edge Case Detection: cargo tickets where the dropoff stop HAS a ticket_agency 
-- but it is INACTIVE (isActive = 0)
-- ============================================================================
SELECT ct.cargoTicketId, ct.ticketCode, ct.dropoffStopId, ct.status,
       ta.ticketAgencyId, ta.ticketAgencyName, ta.isActive
FROM cargo_ticket ct
JOIN ticket_agency ta ON ta.stopPointId = ct.dropoffStopId
LEFT JOIN ticket_agency ta_active ON ta_active.stopPointId = ct.dropoffStopId 
      AND ta_active.isActive = 1
WHERE ta_active.ticketAgencyId IS NULL
ORDER BY ct.cargoTicketId;
