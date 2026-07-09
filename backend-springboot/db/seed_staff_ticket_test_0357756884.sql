/*
    Seed dữ liệu test Ticket Staff — account username: 0357756884
    Chạy trên VeXeDB đã có master data (route/coach/staff/...).

    Tạo 3 chuyến SCHEDULED:
      - +1 giờ  (trong cửa sổ <3h  -> không đổi/hủy)
      - +4 giờ  (3–5h               -> tier hoàn 50%)
      - +10 giờ (>5h                -> tier hoàn 100%)

    Mỗi chuyến 1 vé CONFIRMED (1 ghế) + payment COMPLETED (bỏ qua webhook).

    Chạy lại file này an toàn: xóa seed cũ theo prefix ticket SEED-STAFF- trước khi insert.
*/

USE VeXeDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRANSACTION;

DECLARE @Username VARCHAR(50) = '0357756884';
DECLARE @AccountId INT;
DECLARE @CustomerId INT;
DECLARE @CustomerName NVARCHAR(100);
DECLARE @CustomerPhone VARCHAR(20) = '0357756884';

DECLARE @RouteId INT;
DECLARE @CoachId INT;
DECLARE @DriverId INT;
DECLARE @AttendantId INT;
DECLARE @TicketStaffId INT;
DECLARE @SeatPrice DECIMAL(15, 2);

DECLARE @PickupStopId INT = 1;
DECLARE @DropoffStopId INT = 4;
DECLARE @PickupStopName NVARCHAR(255);
DECLARE @DropoffStopName NVARCHAR(255);

DECLARE @Departure1H DATETIME = DATEADD(HOUR, 1, GETDATE());
DECLARE @Departure4H DATETIME = DATEADD(HOUR, 4, GETDATE());
DECLARE @Departure10H DATETIME = DATEADD(HOUR, 10, GETDATE());

DECLARE @TripId1H INT;
DECLARE @TripId4H INT;
DECLARE @TripId10H INT;

DECLARE @TripSeat1H INT;
DECLARE @TripSeat4H INT;
DECLARE @TripSeat10H INT;

DECLARE @SeatCode1H VARCHAR(10);
DECLARE @SeatCode4H VARCHAR(10);
DECLARE @SeatCode10H VARCHAR(10);

DECLARE @TicketId1H INT;
DECLARE @TicketId4H INT;
DECLARE @TicketId10H INT;

SELECT @AccountId = accountId
FROM [account]
WHERE username = @Username;

IF @AccountId IS NULL
BEGIN
    RAISERROR(N'Không tìm thấy account username = %s. Tạo account trước rồi chạy lại script.', 16, 1, @Username);
    ROLLBACK TRANSACTION;
    RETURN;
END;

SELECT @CustomerId = customerId,
       @CustomerName = customerName,
       @CustomerPhone = COALESCE(NULLIF(LTRIM(RTRIM(phone)), ''), @CustomerPhone)
FROM [customer]
WHERE accountId = @AccountId;

IF @CustomerId IS NULL
BEGIN
    SET @CustomerName = COALESCE(@CustomerName, N'Khách test Ticket Staff');

    INSERT INTO [customer] (accountId, customerName, phone, email, dob)
    VALUES (@AccountId, @CustomerName, @CustomerPhone, NULL, '1995-01-01');

    SET @CustomerId = SCOPE_IDENTITY();
    PRINT N'-> Đã tạo customer mới cho account ' + @Username;
END
ELSE
BEGIN
    PRINT N'-> Dùng customerId = ' + CAST(@CustomerId AS VARCHAR(20)) + N' (account ' + @Username + N')';
END;

SELECT @PickupStopName = stopPointName FROM [coach_stop] WHERE stopPointId = @PickupStopId;
SELECT @DropoffStopName = stopPointName FROM [coach_stop] WHERE stopPointId = @DropoffStopId;

IF @PickupStopName IS NULL OR @DropoffStopName IS NULL
BEGIN
    RAISERROR(N'Thiếu coach_stop id 1 hoặc 4. Chạy fakedata/ddl trước.', 16, 1);
    ROLLBACK TRANSACTION;
    RETURN;
END;

SELECT TOP 1
    @RouteId = r.routeId
FROM [route] r
WHERE r.routeName = N'Hà Nội - Quảng Bình'
ORDER BY r.routeId;

IF @RouteId IS NULL
    SELECT TOP 1 @RouteId = routeId FROM [route] ORDER BY routeId;

SELECT TOP 1
    @CoachId = c.coachId,
    @SeatPrice = ctp.seatPrice
FROM [coach] c
JOIN [coach_type_price] ctp ON ctp.coachTypeId = c.coachTypeId
WHERE c.[status] = 'ACTIVE'
  AND GETDATE() BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate
ORDER BY c.coachId;

IF @CoachId IS NULL
BEGIN
    RAISERROR(N'Không tìm thấy coach ACTIVE có coach_type_price hiệu lực.', 16, 1);
    ROLLBACK TRANSACTION;
    RETURN;
END;

SELECT TOP 1 @DriverId = staffId FROM [staff] WHERE staffPosition = 'DRIVER' ORDER BY staffId;
SELECT TOP 1 @AttendantId = staffId FROM [staff] WHERE staffPosition = 'ATTENDANT' ORDER BY staffId;
SELECT TOP 1 @TicketStaffId = staffId FROM [staff] WHERE staffPosition = 'TICKET_STAFF' ORDER BY staffId;

IF @DriverId IS NULL OR @AttendantId IS NULL
BEGIN
    RAISERROR(N'Thiếu staff DRIVER/ATTENDANT trong DB.', 16, 1);
    ROLLBACK TRANSACTION;
    RETURN;
END;

IF @SeatPrice IS NULL OR @SeatPrice <= 0
    SET @SeatPrice = 350000.00;

/* --- Cleanup seed cũ (idempotent) --- */
DECLARE @OldSeedTrips TABLE (tripId INT PRIMARY KEY);

INSERT INTO @OldSeedTrips (tripId)
SELECT DISTINCT pt.tripId
FROM [passenger_ticket] pt
WHERE pt.ticketCode LIKE 'SEED-STAFF-%';

DELETE r
FROM [refund] r
JOIN [payment] p ON p.paymentId = r.paymentId
JOIN [passenger_ticket] pt ON pt.passengerTicketId = p.passengerTicketId
WHERE pt.ticketCode LIKE 'SEED-STAFF-%';

DELETE p
FROM [payment] p
JOIN [passenger_ticket] pt ON pt.passengerTicketId = p.passengerTicketId
WHERE pt.ticketCode LIKE 'SEED-STAFF-%';

DELETE ptd
FROM [passenger_ticket_detail] ptd
JOIN [passenger_ticket] pt ON pt.passengerTicketId = ptd.passengerTicketId
WHERE pt.ticketCode LIKE 'SEED-STAFF-%';

DELETE pt
FROM [passenger_ticket] pt
WHERE pt.ticketCode LIKE 'SEED-STAFF-%';

DELETE ts
FROM [trip_seat] ts
JOIN @OldSeedTrips ot ON ot.tripId = ts.tripId;

DELETE t
FROM [trip] t
JOIN @OldSeedTrips ot ON ot.tripId = t.tripId;

PRINT N'-> Đã dọn seed cũ (nếu có).';

/* --- Trip +1h --- */
INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
VALUES (@RouteId, @CoachId, @Departure1H, 'SCHEDULED', @DriverId, @AttendantId);
SET @TripId1H = SCOPE_IDENTITY();

INSERT INTO [trip_seat] (tripId, seatId, price, [status])
SELECT @TripId1H, s.seatId, @SeatPrice, 'AVAILABLE'
FROM [seat] s
WHERE s.coachId = @CoachId
  AND s.isActive = 1;

SELECT TOP 1
    @TripSeat1H = ts.tripSeatId,
    @SeatCode1H = s.seatCode
FROM [trip_seat] ts
JOIN [seat] s ON s.seatId = ts.seatId
WHERE ts.tripId = @TripId1H
  AND ts.[status] = 'AVAILABLE'
ORDER BY ts.tripSeatId;

UPDATE [trip_seat] SET [status] = 'SOLD' WHERE tripSeatId = @TripSeat1H;

INSERT INTO [passenger_ticket] (
    customerId, tripId, voucherId, soldBy, ticketCode, totalPrice,
    pickupStopId, dropoffStopId, pickupStopName, dropoffStopName,
    voucherCodeSnapshot, refundPolicyDepartureTime, majorChangeType, [status]
)
VALUES (
    @CustomerId, @TripId1H, NULL, @TicketStaffId, 'SEED-STAFF-1H', @SeatPrice,
    @PickupStopId, @DropoffStopId, @PickupStopName, @DropoffStopName,
    NULL, @Departure1H, NULL, 'CONFIRMED'
);
SET @TicketId1H = SCOPE_IDENTITY();

INSERT INTO [passenger_ticket_detail] (
    passengerTicketId, tripSeatId, seatCodeSnapshot, qrcode,
    fullName, phone, email, price, [status]
)
VALUES (
    @TicketId1H, @TripSeat1H, @SeatCode1H,
    'SEED-QR-1H-' + CAST(NEWID() AS VARCHAR(36)),
    @CustomerName, @CustomerPhone, NULL, @SeatPrice, 'CONFIRMED'
);

INSERT INTO [payment] (
    passengerTicketId, cargoTicketId, amount, paymentMethod,
    transactionId, [status], refundAmount, paymentTime, cancelToken
)
VALUES (
    @TicketId1H, NULL, @SeatPrice, 'SEPAY',
    'PAY1H001', 'COMPLETED', 0.00, GETDATE(), CAST(NEWID() AS VARCHAR(36))
);

/* --- Trip +4h --- */
INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
VALUES (@RouteId, @CoachId, @Departure4H, 'SCHEDULED', @DriverId, @AttendantId);
SET @TripId4H = SCOPE_IDENTITY();

INSERT INTO [trip_seat] (tripId, seatId, price, [status])
SELECT @TripId4H, s.seatId, @SeatPrice, 'AVAILABLE'
FROM [seat] s
WHERE s.coachId = @CoachId
  AND s.isActive = 1;

SELECT TOP 1
    @TripSeat4H = ts.tripSeatId,
    @SeatCode4H = s.seatCode
FROM [trip_seat] ts
JOIN [seat] s ON s.seatId = ts.seatId
WHERE ts.tripId = @TripId4H
  AND ts.[status] = 'AVAILABLE'
ORDER BY ts.tripSeatId;

UPDATE [trip_seat] SET [status] = 'SOLD' WHERE tripSeatId = @TripSeat4H;

INSERT INTO [passenger_ticket] (
    customerId, tripId, voucherId, soldBy, ticketCode, totalPrice,
    pickupStopId, dropoffStopId, pickupStopName, dropoffStopName,
    voucherCodeSnapshot, refundPolicyDepartureTime, majorChangeType, [status]
)
VALUES (
    @CustomerId, @TripId4H, NULL, @TicketStaffId, 'SEED-STAFF-4H', @SeatPrice,
    @PickupStopId, @DropoffStopId, @PickupStopName, @DropoffStopName,
    NULL, @Departure4H, NULL, 'CONFIRMED'
);
SET @TicketId4H = SCOPE_IDENTITY();

INSERT INTO [passenger_ticket_detail] (
    passengerTicketId, tripSeatId, seatCodeSnapshot, qrcode,
    fullName, phone, email, price, [status]
)
VALUES (
    @TicketId4H, @TripSeat4H, @SeatCode4H,
    'SEED-QR-4H-' + CAST(NEWID() AS VARCHAR(36)),
    @CustomerName, @CustomerPhone, NULL, @SeatPrice, 'CONFIRMED'
);

INSERT INTO [payment] (
    passengerTicketId, cargoTicketId, amount, paymentMethod,
    transactionId, [status], refundAmount, paymentTime, cancelToken
)
VALUES (
    @TicketId4H, NULL, @SeatPrice, 'SEPAY',
    'PAY4H001', 'COMPLETED', 0.00, GETDATE(), CAST(NEWID() AS VARCHAR(36))
);

/* --- Trip +10h (2 ghế — tiện test hủy một phần sau này) --- */
INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
VALUES (@RouteId, @CoachId, @Departure10H, 'SCHEDULED', @DriverId, @AttendantId);
SET @TripId10H = SCOPE_IDENTITY();

INSERT INTO [trip_seat] (tripId, seatId, price, [status])
SELECT @TripId10H, s.seatId, @SeatPrice, 'AVAILABLE'
FROM [seat] s
WHERE s.coachId = @CoachId
  AND s.isActive = 1;

DECLARE @TripSeat10H_2 INT;
DECLARE @SeatCode10H_2 VARCHAR(10);

SELECT TOP 1
    @TripSeat10H = ts.tripSeatId,
    @SeatCode10H = s.seatCode
FROM [trip_seat] ts
JOIN [seat] s ON s.seatId = ts.seatId
WHERE ts.tripId = @TripId10H
  AND ts.[status] = 'AVAILABLE'
ORDER BY ts.tripSeatId;

SELECT TOP 1
    @TripSeat10H_2 = ts.tripSeatId,
    @SeatCode10H_2 = s.seatCode
FROM [trip_seat] ts
JOIN [seat] s ON s.seatId = ts.seatId
WHERE ts.tripId = @TripId10H
  AND ts.[status] = 'AVAILABLE'
  AND ts.tripSeatId <> @TripSeat10H
ORDER BY ts.tripSeatId;

UPDATE [trip_seat]
SET [status] = 'SOLD'
WHERE tripSeatId IN (@TripSeat10H, @TripSeat10H_2);

DECLARE @TotalPrice10H DECIMAL(15, 2) = @SeatPrice * 2;

INSERT INTO [passenger_ticket] (
    customerId, tripId, voucherId, soldBy, ticketCode, totalPrice,
    pickupStopId, dropoffStopId, pickupStopName, dropoffStopName,
    voucherCodeSnapshot, refundPolicyDepartureTime, majorChangeType, [status]
)
VALUES (
    @CustomerId, @TripId10H, NULL, @TicketStaffId, 'SEED-STAFF-10H', @TotalPrice10H,
    @PickupStopId, @DropoffStopId, @PickupStopName, @DropoffStopName,
    NULL, @Departure10H, NULL, 'CONFIRMED'
);
SET @TicketId10H = SCOPE_IDENTITY();

INSERT INTO [passenger_ticket_detail] (
    passengerTicketId, tripSeatId, seatCodeSnapshot, qrcode,
    fullName, phone, email, price, [status]
)
VALUES
(
    @TicketId10H, @TripSeat10H, @SeatCode10H,
    'SEED-QR-10H-A-' + CAST(NEWID() AS VARCHAR(36)),
    @CustomerName, @CustomerPhone, NULL, @SeatPrice, 'CONFIRMED'
),
(
    @TicketId10H, @TripSeat10H_2, @SeatCode10H_2,
    'SEED-QR-10H-B-' + CAST(NEWID() AS VARCHAR(36)),
    N'Người đi cùng', @CustomerPhone, NULL, @SeatPrice, 'CONFIRMED'
);

INSERT INTO [payment] (
    passengerTicketId, cargoTicketId, amount, paymentMethod,
    transactionId, [status], refundAmount, paymentTime, cancelToken
)
VALUES (
    @TicketId10H, NULL, @TotalPrice10H, 'SEPAY',
    'PAY10H01', 'COMPLETED', 0.00, GETDATE(), CAST(NEWID() AS VARCHAR(36))
);

COMMIT TRANSACTION;

PRINT N'=== SEED HOÀN TẤT ===';
PRINT N'Account     : ' + @Username + N' (accountId=' + CAST(@AccountId AS VARCHAR(20)) + N', customerId=' + CAST(@CustomerId AS VARCHAR(20)) + N')';
PRINT N'Ticket 1h   : SEED-STAFF-1H  | tripId=' + CAST(@TripId1H AS VARCHAR(20)) + N' | departure=' + CONVERT(VARCHAR(19), @Departure1H, 120);
PRINT N'Ticket 4h   : SEED-STAFF-4H  | tripId=' + CAST(@TripId4H AS VARCHAR(20)) + N' | departure=' + CONVERT(VARCHAR(19), @Departure4H, 120);
PRINT N'Ticket 10h  : SEED-STAFF-10H | tripId=' + CAST(@TripId10H AS VARCHAR(20)) + N' | departure=' + CONVERT(VARCHAR(19), @Departure10H, 120) + N' | 2 ghế';
PRINT N'Tìm trên Ticket Staff: SĐT ' + @CustomerPhone + N' hoặc mã vé SEED-STAFF-*';
GO
