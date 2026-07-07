-- ============================================================
-- TEST DATA: Trip Staff feature after merging main
-- Run AFTER fakedata.sql + seed_tripstaff33.sql
-- Tests: cargo load/unload, trip start/end, status filter
-- ============================================================

USE VeXeDB;
GO

SET NOCOUNT ON;

-- ============================================================
-- Helper: find a driver + attendant + coach
-- ============================================================
DECLARE @DriverId INT;
DECLARE @AttendantId INT;
DECLARE @CoachId1 INT, @CoachId2 INT;

SELECT TOP 1 @DriverId = staffId FROM [staff] WHERE staffPosition = 'DRIVER' ORDER BY staffId;
SELECT TOP 1 @AttendantId = staffId FROM [staff] WHERE staffPosition = 'ATTENDANT' ORDER BY staffId;
SELECT TOP 1 @CoachId1 = coachId FROM [coach] WHERE [status] = 'ACTIVE' ORDER BY coachId;
SELECT TOP 1 @CoachId2 = coachId FROM [coach] WHERE [status] = 'ACTIVE' AND coachId > @CoachId1 ORDER BY coachId;

PRINT N'Driver: ' + CAST(@DriverId AS VARCHAR);
PRINT N'Attendant: ' + CAST(@AttendantId AS VARCHAR);
PRINT N'Coach1: ' + CAST(@CoachId1 AS VARCHAR);
PRINT N'Coach2: ' + CAST(@CoachId2 AS VARCHAR);

-- ============================================================
-- Trip status filter test: set varied statuses on existing trips
-- ============================================================
-- Find trips from seed file assigned to this driver
UPDATE TOP (1) [trip] SET [status] = 'IN_PROGRESS'
WHERE driverId = @DriverId AND [status] = 'SCHEDULED'
  AND departureTime < DATEADD(HOUR, 2, GETDATE());

UPDATE TOP (1) [trip] SET [status] = 'COMPLETED'
WHERE driverId = @DriverId AND [status] = 'SCHEDULED'
  AND departureTime < GETDATE();

PRINT N'→ Updated trip statuses for filter testing';

-- ============================================================
-- Trip start/end test: create a trip starting soon
-- ============================================================
DECLARE @StartTripId INT;
INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
VALUES (1, @CoachId2, DATEADD(MINUTE, 15, GETDATE()), 'SCHEDULED', @DriverId, @AttendantId);
SET @StartTripId = SCOPE_IDENTITY();
PRINT N'→ Created trip ' + CAST(@StartTripId AS VARCHAR) + ' starting in 15 min (for start/end test)';

-- Trip seats for this trip
INSERT INTO [trip_seat] (tripId, seatId, price, [status])
SELECT @StartTripId, s.seatId, ctp.seatPrice, 'AVAILABLE'
FROM [seat] s
JOIN [coach] c ON c.coachId = s.coachId AND c.coachId = @CoachId2
JOIN [coach_type_price] ctp ON ctp.coachTypeId = c.coachTypeId
WHERE GETDATE() BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate;
PRINT N'→ Created trip_seats for start/end test';

-- ============================================================
-- CARGO test: 4 cargo tickets for the start trip + 2 for old trips
-- ============================================================
DECLARE @CargoCustomerId INT;
SELECT TOP 1 @CargoCustomerId = customerId FROM [customer] ORDER BY customerId;

-- Cargo ticket 1: RECEIVED (ready to load)
INSERT INTO [cargo_ticket] (
    tripId, customerId, senderName, senderPhone,
    receiverName, receiverPhone, ticketCode, totalPrice,
    feePayer, codAmount, pickupStopId, dropoffStopId, [status], soldBy
)
VALUES (
    @StartTripId, @CargoCustomerId, N'Công ty ABC', '0912345678',
    N'Cửa hàng XYZ', '0978765432', 'CG_TEST_LOAD_001', 200000.00,
    'SENDER', 0.00, 1, 4, 'RECEIVED', @DriverId
);
DECLARE @CargoId1 INT = SCOPE_IDENTITY();

-- Cargo ticket 2: RECEIVED (another one)
INSERT INTO [cargo_ticket] (
    tripId, customerId, senderName, senderPhone,
    receiverName, receiverPhone, ticketCode, totalPrice,
    feePayer, codAmount, pickupStopId, dropoffStopId, [status], soldBy
)
VALUES (
    @StartTripId, @CargoCustomerId + 1, N'Nguyễn Văn Gửi', '0909123456',
    N'Trần Thị Nhận', '0909987654', 'CG_TEST_LOAD_002', 350000.00,
    'RECEIVER', 50000.00, 1, 4, 'RECEIVED', @DriverId
);
DECLARE @CargoId2 INT = SCOPE_IDENTITY();

-- Cargo ticket 3: LOADED (test unload flow)
INSERT INTO [cargo_ticket] (
    tripId, customerId, senderName, senderPhone,
    receiverName, receiverPhone, ticketCode, totalPrice,
    feePayer, codAmount, pickupStopId, dropoffStopId, [status], soldBy, loadedBy
)
VALUES (
    @StartTripId, @CargoCustomerId + 2, N'Bưu điện Hoàn Kiếm', '0241234567',
    N'Bưu điện Đồng Hới', '0521234567', 'CG_TEST_UNLOAD_001', 150000.00,
    'SENDER', 0.00, 1, 4, 'LOADED', @DriverId, @DriverId
);
DECLARE @CargoId3 INT = SCOPE_IDENTITY();

-- Cargo ticket 4: ARRIVED (test deliver flow)
INSERT INTO [cargo_ticket] (
    tripId, customerId, senderName, senderPhone,
    receiverName, receiverPhone, ticketCode, totalPrice,
    feePayer, codAmount, pickupStopId, dropoffStopId, [status], soldBy, loadedBy, unloadedBy
)
VALUES (
    @StartTripId, @CargoCustomerId + 3, N'Dược phẩm An Khang', '0289876543',
    N'Nhà thuốc Quảng Bình', '0529876543', 'CG_TEST_DELIVER_001', 500000.00,
    'RECEIVER', 200000.00, 1, 4, 'ARRIVED', @DriverId, @DriverId, @AttendantId
);
DECLARE @CargoId4 INT = SCOPE_IDENTITY();

-- Cargo ticket details
INSERT INTO [cargo_ticket_detail] (cargoTicketId, cargoTypePriceId, [description], quantity, weightKg, dimensionVol, calculatedPrice)
VALUES
(@CargoId1, 3, N'Thùng hàng điện tử', 2, 15.0, 0.5, 200000.00),
(@CargoId2, 2, N'Máy móc công nghiệp', 1, 50.0, 2.0, 350000.00),
(@CargoId3, 1, N'Bưu kiện nhỏ', 5, 8.0, 0.3, 150000.00),
(@CargoId4, 2, N'Thuốc men', 10, 25.0, 1.0, 500000.00);

PRINT N'→ Created 4 cargo tickets for load/unload/deliver testing';

-- ============================================================
-- Add cargo to an existing trip from seed (if any loaded trip exists)
-- ============================================================
DECLARE @OldTripId INT;
SELECT TOP 1 @OldTripId = tripId FROM [trip]
WHERE (driverId = @DriverId OR attendantId = @AttendantId)
  AND [status] = 'COMPLETED'
ORDER BY tripId;

IF @OldTripId IS NOT NULL
BEGIN
    INSERT INTO [cargo_ticket] (
        tripId, customerId, senderName, senderPhone,
        receiverName, receiverPhone, ticketCode, totalPrice,
        feePayer, codAmount, pickupStopId, dropoffStopId, [status], soldBy, loadedBy, unloadedBy, deliveredBy
    )
    VALUES (
        @OldTripId, @CargoCustomerId + 4, N'Đã giao hàng', '0911111111',
        N'Đã nhận hàng', '0922222222', 'CG_TEST_DELIVERED_001', 120000.00,
        'SENDER', 0.00, 1, 4, 'DELIVERED', @DriverId, @DriverId, @AttendantId, @DriverId
    );
    DECLARE @OldCargoId INT = SCOPE_IDENTITY();
    INSERT INTO [cargo_ticket_detail] (cargoTicketId, cargoTypePriceId, [description], quantity, weightKg, dimensionVol, calculatedPrice)
    VALUES (@OldCargoId, 1, N'Đã giao thành công', 3, 10.0, 0.4, 120000.00);
    PRINT N'→ Added delivered cargo to completed trip ' + CAST(@OldTripId AS VARCHAR);
END;

-- ============================================================
-- Search/filter test: add a distinct route name trip
-- ============================================================
DECLARE @Route2Id INT = 2; -- QB→HN route
INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
VALUES (@Route2Id, @CoachId1, DATEADD(HOUR, 3, GETDATE()), 'SCHEDULED', @DriverId, @AttendantId);
DECLARE @EveningTripId INT = SCOPE_IDENTITY();

INSERT INTO [trip_seat] (tripId, seatId, price, [status])
SELECT @EveningTripId, s.seatId, ctp.seatPrice, 'AVAILABLE'
FROM [seat] s
JOIN [coach] c ON c.coachId = s.coachId AND c.coachId = @CoachId1
JOIN [coach_type_price] ctp ON ctp.coachTypeId = c.coachTypeId
WHERE GETDATE() BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate;
PRINT N'→ Created evening trip ' + CAST(@EveningTripId AS VARCHAR) + ' on route 2 (QB→HN) for filter test';

-- ============================================================
-- NO-SHOW test: add CONFIRMED passengers to the start trip
-- ============================================================
DECLARE @NoShowCustomerId INT;
SELECT TOP 1 @NoShowCustomerId = customerId FROM [customer] ORDER BY customerId;

DECLARE @SeatRows TABLE (rn INT IDENTITY(1,1), tripSeatId INT, seatCode VARCHAR(10));
INSERT INTO @SeatRows (tripSeatId, seatCode)
SELECT TOP 3 ts.tripSeatId, s.seatCode
FROM [trip_seat] ts
JOIN [seat] s ON s.seatId = ts.seatId
WHERE ts.tripId = @StartTripId AND ts.[status] = 'AVAILABLE'
ORDER BY ts.tripSeatId;

DECLARE @SeatPrices DECIMAL(15,2);
SELECT TOP 1 @SeatPrices = ts.price FROM [trip_seat] ts WHERE ts.tripId = @StartTripId;

-- Passenger 1: will check-in normally
DECLARE @NsRow INT = 1;
UPDATE [trip_seat] SET [status] = 'SOLD' WHERE tripSeatId = (SELECT tripSeatId FROM @SeatRows WHERE rn = 1);
INSERT INTO [passenger_ticket] (customerId, tripId, soldBy, ticketCode, totalPrice, pickupStopId, dropoffStopId, pickupStopName, dropoffStopName, [status])
VALUES (@NoShowCustomerId, @StartTripId, NULL, 'NS_CHECKIN_01', @SeatPrices, 1, 4, N'Hà Nội', N'Quảng Bình', 'CONFIRMED');
DECLARE @NsTicket1 INT = SCOPE_IDENTITY();
INSERT INTO [passenger_ticket_detail] (passengerTicketId, tripSeatId, seatCodeSnapshot, qrcode, fullName, phone, price, [status])
SELECT @NsTicket1, tripSeatId, seatCode, LOWER(REPLACE(CAST(NEWID() AS VARCHAR(36)), '-', '')), N'Nguyễn Văn Checkin', '0911111111', @SeatPrices, 'CONFIRMED' FROM @SeatRows WHERE rn = 1;

-- Passenger 2: will be marked no-show
SET @NsRow = 2;
UPDATE [trip_seat] SET [status] = 'SOLD' WHERE tripSeatId = (SELECT tripSeatId FROM @SeatRows WHERE rn = 2);
INSERT INTO [passenger_ticket] (customerId, tripId, soldBy, ticketCode, totalPrice, pickupStopId, dropoffStopId, pickupStopName, dropoffStopName, [status])
VALUES (@NoShowCustomerId + 1, @StartTripId, NULL, 'NS_NOSHOW_02', @SeatPrices, 1, 4, N'Hà Nội', N'Quảng Bình', 'CONFIRMED');
DECLARE @NsTicket2 INT = SCOPE_IDENTITY();
INSERT INTO [passenger_ticket_detail] (passengerTicketId, tripSeatId, seatCodeSnapshot, qrcode, fullName, phone, price, [status])
SELECT @NsTicket2, tripSeatId, seatCode, LOWER(REPLACE(CAST(NEWID() AS VARCHAR(36)), '-', '')), N'Trần Văn Vắng', '0922222222', @SeatPrices, 'CONFIRMED' FROM @SeatRows WHERE rn = 2;

-- Payment for both
INSERT INTO [payment] (passengerTicketId, amount, paymentMethod, transactionId, [status], paymentTime)
VALUES (@NsTicket1, @SeatPrices, 'CASH', 'TXN_NS_01', 'COMPLETED', GETDATE()),
       (@NsTicket2, @SeatPrices, 'CASH', 'TXN_NS_02', 'COMPLETED', GETDATE());

PRINT N'→ Added 2 passengers to trip ' + CAST(@StartTripId AS VARCHAR) + ' for no-show test';
PRINT N'  - Nguyễn Văn Checkin → click Check-in';
PRINT N'  - Trần Văn Vắng → click "Vắng mặt"';

-- ============================================================
-- Summary
-- ============================================================
PRINT N'';
PRINT N'=== TRIP STAFF TEST DATA READY ===';
PRINT N'What to test:';
PRINT N'  1. Trip list → date toggle shows trips with varied statuses';
PRINT N'  2. Status filter → filter by SCHEDULED / IN_PROGRESS / COMPLETED';
PRINT N'  3. Search → type route name or license plate';
PRINT N'  4. Cargo tab → shows 4 cargo tickets in different states:';
PRINT N'     - CG_TEST_LOAD_001:   RECEIVED  → click "Xác nhận lên xe"';
PRINT N'     - CG_TEST_LOAD_002:   RECEIVED  → click "Xác nhận lên xe"';
PRINT N'     - CG_TEST_UNLOAD_001: LOADED    → click "Dỡ hàng"';
PRINT N'     - CG_TEST_DELIVER_001: ARRIVED  → click "Xác nhận đã giao"';
PRINT N'  5. Start/end trip → trip starting in 15 min → start then end';
PRINT N'  6. No-show button → click "Vắng mặt" on Trần Văn Vắng';
PRINT N'     - CONFIRMED → CANCELLED, card badge turns red';
PRINT N'     - Check-in button hides, no-show button hides';
PRINT N'';
PRINT N'Login: driver phone from fakedata (e.g. 0932000001 / 123456)';
PRINT N'tripId for cargo:  ' + CAST(@StartTripId AS VARCHAR);
PRINT N'tripId for evening: ' + CAST(@EveningTripId AS VARCHAR);
