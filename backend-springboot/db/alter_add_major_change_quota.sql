-- Phase 4b: quota + refund policy snapshot for passenger_ticket
-- Run once on existing databases before deploying the backend changes.

IF COL_LENGTH('passenger_ticket', 'majorChangeType') IS NULL
BEGIN
    ALTER TABLE [passenger_ticket] ADD [majorChangeType] VARCHAR(20) NULL;
END;

IF COL_LENGTH('passenger_ticket', 'refundPolicyDepartureTime') IS NULL
BEGIN
    ALTER TABLE [passenger_ticket] ADD [refundPolicyDepartureTime] DATETIME NULL;
END;

IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE name = 'CK_PassengerTicket_MajorChangeType'
)
BEGIN
    ALTER TABLE [passenger_ticket] ADD CONSTRAINT CK_PassengerTicket_MajorChangeType
        CHECK ([majorChangeType] IS NULL OR [majorChangeType] IN ('TRANSFER_TRIP', 'CANCEL_PARTIAL'));
END;

UPDATE pt
SET pt.refundPolicyDepartureTime = t.departureTime
FROM passenger_ticket pt
INNER JOIN trip t ON t.tripId = pt.tripId
WHERE pt.refundPolicyDepartureTime IS NULL;
