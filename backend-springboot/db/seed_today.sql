-- ============================================================
-- SEED: Trips & test data for 2026-07-07
-- Staff login: 093200001 / 123456
-- ============================================================
USE VeXeDB;
GO

SET NOCOUNT ON;

DECLARE @StaffId33 INT;
DECLARE @AttendantId INT;
DECLARE @CoachId1 INT;
DECLARE @CoachId2 INT;
DECLARE @NewTripId1 INT;
DECLARE @NewTripId2 INT;
DECLARE @TripIds TABLE (tripId INT, coachId INT);
DECLARE @PickupName NVARCHAR(255);
DECLARE @DropoffName NVARCHAR(255);
DECLARE @SeatPrice DECIMAL(15,2);
DECLARE @CustomerId INT;

SELECT TOP 1 @StaffId33 = staffId FROM [staff] WHERE staffPosition = 'DRIVER' ORDER BY staffId;
SELECT TOP 1 @AttendantId = staffId FROM [staff] WHERE staffPosition = 'ATTENDANT' ORDER BY staffId;
SELECT @CoachId1 = MIN(coachId) FROM (SELECT TOP 2 coachId FROM [coach] WHERE [status] = 'ACTIVE' ORDER BY coachId) x;
SELECT @CoachId2 = MIN(coachId) FROM (SELECT TOP 2 coachId FROM [coach] WHERE [status] = 'ACTIVE' ORDER BY coachId) x HAVING MIN(coachId) > @CoachId1;
SELECT TOP 1 @CustomerId = customerId FROM [customer] ORDER BY customerId;
SELECT TOP 1 @SeatPrice = ctp.seatPrice FROM [coach_type_price] ctp WHERE GETDATE() BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate;

PRINT 'Staff33=' + CAST(@StaffId33 AS VARCHAR) + ' Attendant=' + CAST(@AttendantId AS VARCHAR);
PRINT 'Coach1=' + CAST(@CoachId1 AS VARCHAR) + ' Coach2=' + CAST(@CoachId2 AS VARCHAR);
PRINT 'SeatPrice=' + CAST(@SeatPrice AS VARCHAR);

-- Trip 1: 06:00 HN->QB
INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
VALUES (1, @CoachId1, '2026-07-07 06:00:00', 'SCHEDULED', @StaffId33, @AttendantId);
SET @NewTripId1 = SCOPE_IDENTITY();
INSERT INTO @TripIds VALUES (@NewTripId1, @CoachId1);

-- Trip 2: 14:00 QB->HN (staff33 as attendant)
INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
VALUES (2, @CoachId2, '2026-07-07 14:00:00', 'SCHEDULED', @AttendantId, @StaffId33);
SET @NewTripId2 = SCOPE_IDENTITY();
INSERT INTO @TripIds VALUES (@NewTripId2, @CoachId2);

PRINT 'Trip1=' + CAST(@NewTripId1 AS VARCHAR) + ' Trip2=' + CAST(@NewTripId2 AS VARCHAR);

-- Trip seats
INSERT INTO [trip_seat] (tripId, seatId, price, [status])
SELECT t.tripId, s.seatId, ctp.seatPrice, 'AVAILABLE'
FROM @TripIds t
JOIN [coach] c ON c.coachId = t.coachId
JOIN [seat] s ON s.coachId = c.coachId
JOIN [coach_type_price] ctp ON ctp.coachTypeId = c.coachTypeId
WHERE GETDATE() BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate;

PRINT 'Trip seats created';

-- Test passengers for Trip 1
SELECT @PickupName = stopPointName FROM [coach_stop] WHERE stopPointId = 1;
SELECT @DropoffName = stopPointName FROM [coach_stop] WHERE stopPointId = 4;

DECLARE @AvailableSeats TABLE (rn INT IDENTITY(1,1), tripSeatId INT, seatCode VARCHAR(10));
INSERT INTO @AvailableSeats (tripSeatId, seatCode)
SELECT TOP 7 ts.tripSeatId, s.seatCode
FROM [trip_seat] ts
JOIN [seat] s ON s.seatId = ts.seatId
WHERE ts.tripId = @NewTripId1 AND ts.[status] = 'AVAILABLE'
ORDER BY ts.tripSeatId;

DECLARE @i INT = 1;
DECLARE @CurSeatId INT;
DECLARE @CurSeatCode VARCHAR(10);
DECLARE @NewTicketId INT;
DECLARE @NewDetailId INT;
DECLARE @DetailStatus VARCHAR(20);
DECLARE @HasChild INT;

WHILE @i <= 7
BEGIN
    SELECT @CurSeatId = tripSeatId FROM @AvailableSeats WHERE rn = @i;
    SELECT @CurSeatCode = seatCode FROM @AvailableSeats WHERE rn = @i;
    SET @DetailStatus = CASE WHEN @i = 7 THEN 'CHECKED_IN' ELSE 'CONFIRMED' END;
    SET @HasChild = CASE WHEN @i = 4 THEN 1 ELSE 0 END;

    UPDATE [trip_seat] SET [status] = 'SOLD' WHERE tripSeatId = @CurSeatId;

    INSERT INTO [passenger_ticket] (customerId, tripId, soldBy, ticketCode, totalPrice, pickupStopId, dropoffStopId, pickupStopName, dropoffStopName, voucherCodeSnapshot, [status])
    VALUES (@CustomerId + ((@i - 1) % 3), @NewTripId1, NULL, 'T0707_' + RIGHT('00' + CAST(@i AS VARCHAR(2)), 2), @SeatPrice, 1, 4, @PickupName, @DropoffName, NULL, 'CONFIRMED');
    SET @NewTicketId = SCOPE_IDENTITY();

    INSERT INTO [passenger_ticket_detail] (passengerTicketId, tripSeatId, seatCodeSnapshot, qrcode, fullName, phone, price, [status])
    VALUES (@NewTicketId, @CurSeatId, @CurSeatCode, LOWER(REPLACE(CAST(NEWID() AS VARCHAR(36)), '-', '')),
        CASE @i
            WHEN 1 THEN 'Nguyen Van An'
            WHEN 2 THEN 'Tran Thi Binh'
            WHEN 3 THEN 'Le Quoc Cuong'
            WHEN 4 THEN 'Pham Thi Dung'
            WHEN 5 THEN 'Hoang Minh Duc'
            WHEN 6 THEN 'Vu Thi Hoa'
            ELSE 'Ngo Van Kien (da check-in)'
        END,
        '09' + RIGHT('0000000' + CAST(@i * 7777 AS VARCHAR(7)), 8), @SeatPrice, @DetailStatus);
    SET @NewDetailId = SCOPE_IDENTITY();

    IF @HasChild = 1
        INSERT INTO [accompanied_child] (ticketDetailId, fullname, birthYear) VALUES (@NewDetailId, 'Pham Be Nho', 2022);

    INSERT INTO [payment] (passengerTicketId, cargoTicketId, amount, paymentMethod, transactionId, [status], paymentTime)
    VALUES (@NewTicketId, NULL, @SeatPrice, 'SEPAY', 'TXN_0707_' + RIGHT('00' + CAST(@i AS VARCHAR(2)), 2), 'COMPLETED', GETDATE());

    SET @i = @i + 1;
END;

PRINT '7 passengers created for Trip ' + CAST(@NewTripId1 AS VARCHAR);

-- Cargo for Trip 1
INSERT INTO [cargo_ticket] (tripId, customerId, senderName, senderPhone, receiverName, receiverPhone, ticketCode, totalPrice, description, feePayer, codAmount, pickupStopId, dropoffStopId, status, soldBy)
VALUES (@NewTripId1, @CustomerId, 'Nguyen Van A', '0912345678', 'Tran Thi B', '0987654321', 'CRG_0707_01', 600000, '3 boxes goods', 'SENDER', 500000, 1, 4, 'RECEIVED', @StaffId33);
SET @NewTicketId = SCOPE_IDENTITY();
INSERT INTO [cargo_ticket_detail] (cargoTicketId, cargoTypePriceId, description, quantity, weightKg, dimensionVol, calculatedPrice)
VALUES (@NewTicketId, 1, 'Rice cooker', 2, 5, 0.15, 120000), (@NewTicketId, 1, 'Electric fan', 1, 3, 0.10, 60000);

INSERT INTO [cargo_ticket] (tripId, customerId, senderName, senderPhone, receiverName, receiverPhone, ticketCode, totalPrice, description, feePayer, codAmount, pickupStopId, dropoffStopId, status, soldBy)
VALUES (@NewTripId1, @CustomerId + 1, 'Le Van C', '0977112233', 'Pham Thi D', '0966554433', 'CRG_0707_02', 800000, '2 laptops', 'RECEIVER', 0, 1, 4, 'RECEIVED', @StaffId33);
SET @NewTicketId = SCOPE_IDENTITY();
INSERT INTO [cargo_ticket_detail] (cargoTicketId, cargoTypePriceId, description, quantity, weightKg, dimensionVol, calculatedPrice)
VALUES (@NewTicketId, 2, 'Dell laptop', 1, 2.5, 0.05, 400000), (@NewTicketId, 2, 'HP laptop', 1, 2.8, 0.05, 400000);

PRINT '2 cargo orders created for Trip ' + CAST(@NewTripId1 AS VARCHAR);
PRINT '';
PRINT '=== TEST DATA READY ===';
PRINT 'Login: 093200001 / 123456';
PRINT 'Trip 1 (06:00 HN->QB): ' + CAST(@NewTripId1 AS VARCHAR) + ' - 7 passengers, 2 cargo';
PRINT 'Trip 2 (14:00 QB->HN): ' + CAST(@NewTripId2 AS VARCHAR) + ' - no passengers yet';
