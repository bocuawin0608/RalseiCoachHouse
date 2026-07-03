-- ============================================================
-- SEED: Trips & test passengers cho Staff #33 (Tài Xế số 1)
-- Mục đích: Test màn hình Trip Staff từ 2026-07-03 đến 2026-07-06
-- Chạy file này SAU khi đã chạy fakedata.sql
-- Password tất cả accounts trong fakedata: 123456
-- Staff #33: username = 0932000001 (Tài Xế Chuyên Nghiệp 1)
-- ============================================================

USE VeXeDB;
GO

SET NOCOUNT ON;

-- ============================================================
-- BƯỚC 1: Xác định staffId của driver #33 và coach sẽ dùng
-- ============================================================
DECLARE @StaffId33 INT;
DECLARE @CoachId1  INT;
DECLARE @CoachId2  INT;
DECLARE @CoachId3  INT;

-- Staff đầu tiên có staffPosition = 'DRIVER' → chính là staffId 33 sau khi reseed
SELECT @StaffId33 = staffId
FROM [staff]
WHERE staffPosition = 'DRIVER'
ORDER BY staffId ASC
OFFSET 0 ROWS FETCH NEXT 1 ROW ONLY;

-- Lấy 3 coach ACTIVE đầu tiên
SELECT @CoachId1 = MIN(coachId) FROM (
    SELECT TOP 1 coachId FROM [coach] WHERE [status] = 'ACTIVE' ORDER BY coachId
) x;
SELECT @CoachId2 = MIN(coachId) FROM (
    SELECT TOP 1 coachId FROM [coach] WHERE [status] = 'ACTIVE' AND coachId > @CoachId1 ORDER BY coachId
) x;
SELECT @CoachId3 = MIN(coachId) FROM (
    SELECT TOP 1 coachId FROM [coach] WHERE [status] = 'ACTIVE' AND coachId > @CoachId2 ORDER BY coachId
) x;

-- Attendant kết cặp: lấy attendant đầu tiên
DECLARE @AttendantId INT;
SELECT @AttendantId = staffId
FROM [staff]
WHERE staffPosition = 'ATTENDANT'
ORDER BY staffId ASC
OFFSET 0 ROWS FETCH NEXT 1 ROW ONLY;

PRINT N'→ staffId 33 = ' + CAST(@StaffId33 AS VARCHAR(10));
PRINT N'→ CoachIds: ' + CAST(@CoachId1 AS VARCHAR(5)) + ', ' + CAST(@CoachId2 AS VARCHAR(5)) + ', ' + CAST(@CoachId3 AS VARCHAR(5));
PRINT N'→ AttendantId: ' + CAST(@AttendantId AS VARCHAR(10));

-- ============================================================
-- BƯỚC 2: Tạo trips từ 03/07 đến 06/07 (Thứ 6 → Chủ Nhật)
-- Mỗi ngày 3 chuyến: 06:00, 13:00, 20:00
-- ============================================================
DECLARE @TripIds TABLE (tripId INT, coachId INT);
DECLARE @NewTripId INT;

-- ---- 2026-07-03 (Thứ 6) ----
-- 06:00 HN→QB (driverId = staffId33)
INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
VALUES (1, @CoachId1, '2026-07-03 06:00:00', 'SCHEDULED', @StaffId33, @AttendantId);
SET @NewTripId = SCOPE_IDENTITY(); INSERT INTO @TripIds VALUES (@NewTripId, @CoachId1);

-- 13:00 HN→QB (attendantId = staffId33)
INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
VALUES (1, @CoachId2, '2026-07-03 13:00:00', 'SCHEDULED', @AttendantId, @StaffId33);
SET @NewTripId = SCOPE_IDENTITY(); INSERT INTO @TripIds VALUES (@NewTripId, @CoachId2);

-- 20:00 QB→HN (driverId = staffId33)
INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
VALUES (2, @CoachId3, '2026-07-03 20:00:00', 'SCHEDULED', @StaffId33, @AttendantId);
SET @NewTripId = SCOPE_IDENTITY(); INSERT INTO @TripIds VALUES (@NewTripId, @CoachId3);

-- ---- 2026-07-04 (Thứ 7) ----
INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
VALUES (1, @CoachId1, '2026-07-04 07:00:00', 'SCHEDULED', @StaffId33, @AttendantId);
SET @NewTripId = SCOPE_IDENTITY(); INSERT INTO @TripIds VALUES (@NewTripId, @CoachId1);

INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
VALUES (2, @CoachId2, '2026-07-04 14:00:00', 'SCHEDULED', @AttendantId, @StaffId33);
SET @NewTripId = SCOPE_IDENTITY(); INSERT INTO @TripIds VALUES (@NewTripId, @CoachId2);

-- ---- 2026-07-05 (Chủ Nhật) ----
INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
VALUES (1, @CoachId3, '2026-07-05 08:00:00', 'SCHEDULED', @StaffId33, @AttendantId);
SET @NewTripId = SCOPE_IDENTITY(); INSERT INTO @TripIds VALUES (@NewTripId, @CoachId3);

INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
VALUES (2, @CoachId1, '2026-07-05 19:00:00', 'SCHEDULED', @AttendantId, @StaffId33);
SET @NewTripId = SCOPE_IDENTITY(); INSERT INTO @TripIds VALUES (@NewTripId, @CoachId1);

-- ---- 2026-07-06 (Thứ 2) ----
INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
VALUES (1, @CoachId2, '2026-07-06 06:00:00', 'SCHEDULED', @StaffId33, @AttendantId);
SET @NewTripId = SCOPE_IDENTITY(); INSERT INTO @TripIds VALUES (@NewTripId, @CoachId2);

PRINT N'→ Đã tạo ' + CAST(@@ROWCOUNT AS VARCHAR) + ' trips cho staff #33';

-- ============================================================
-- BƯỚC 3: Tạo trip_seats cho các trip mới
-- ============================================================
INSERT INTO [trip_seat] (tripId, seatId, price, [status])
SELECT t.tripId, s.seatId, ctp.seatPrice, 'AVAILABLE'
FROM @TripIds t
JOIN [coach] c ON c.coachId = t.coachId
JOIN [seat] s ON s.coachId = c.coachId
JOIN [coach_type_price] ctp ON ctp.coachTypeId = c.coachTypeId
WHERE GETDATE() BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate;

PRINT N'→ Đã tạo trip_seats cho tất cả trips';

-- ============================================================
-- BƯỚC 4: Tạo vé test cho trip đầu tiên hôm nay (06:00)
-- 5 vé CONFIRMED (để test check-in) + 1 CHECKED_IN (đã quét rồi)
-- ============================================================
DECLARE @TestTripId INT;
SELECT TOP 1 @TestTripId = tripId FROM @TripIds ORDER BY tripId ASC;

DECLARE @TestCustomerId INT;
SELECT TOP 1 @TestCustomerId = customerId FROM [customer] ORDER BY customerId ASC;

DECLARE @PickupName  NVARCHAR(255);
DECLARE @DropoffName NVARCHAR(255);
SELECT @PickupName  = stopPointName FROM [coach_stop] WHERE stopPointId = 1;
SELECT @DropoffName = stopPointName FROM [coach_stop] WHERE stopPointId = 4;

-- Lấy seatPrice cho trip này
DECLARE @SeatPrice DECIMAL(15,2);
SELECT TOP 1 @SeatPrice = ts.price
FROM [trip_seat] ts
WHERE ts.tripId = @TestTripId;

-- Lấy 6 ghế trống đầu tiên của trip test
DECLARE @AvailableSeats TABLE (rn INT IDENTITY(1,1), tripSeatId INT, seatCode VARCHAR(10));
INSERT INTO @AvailableSeats (tripSeatId, seatCode)
SELECT TOP 6 ts.tripSeatId, s.seatCode
FROM [trip_seat] ts
JOIN [seat] s ON s.seatId = ts.seatId
WHERE ts.tripId = @TestTripId AND ts.[status] = 'AVAILABLE'
ORDER BY ts.tripSeatId;

-- Tạo từng vé theo vòng lặp
DECLARE @i INT = 1;
DECLARE @CurSeatId INT;
DECLARE @CurSeatCode VARCHAR(10);
DECLARE @NewTicketId INT;
DECLARE @DetailStatus VARCHAR(20);
DECLARE @TicketStatus VARCHAR(20);
DECLARE @HasChild INT;
DECLARE @NewDetailId INT;

WHILE @i <= 6
BEGIN
    SELECT @CurSeatId   = tripSeatId FROM @AvailableSeats WHERE rn = @i;
    SELECT @CurSeatCode = seatCode    FROM @AvailableSeats WHERE rn = @i;

    -- Vé 6: CHECKED_IN (đã quét - để test lỗi "quét lại")
    SET @DetailStatus = CASE WHEN @i = 6 THEN 'CHECKED_IN' ELSE 'CONFIRMED' END;
    SET @TicketStatus = 'CONFIRMED';
    SET @HasChild     = CASE WHEN @i = 3 THEN 1 ELSE 0 END; -- Vé 3 có trẻ em

    -- Đánh dấu ghế SOLD
    UPDATE [trip_seat] SET [status] = 'SOLD' WHERE tripSeatId = @CurSeatId;

    -- Master ticket
    INSERT INTO [passenger_ticket] (
        customerId, tripId, soldBy, ticketCode, totalPrice,
        pickupStopId, dropoffStopId, pickupStopName, dropoffStopName,
        voucherCodeSnapshot, [status]
    )
    VALUES (
        @TestCustomerId + (@i - 1), @TestTripId, NULL,
        'TSTEST_' + RIGHT('00' + CAST(@i AS VARCHAR(2)), 2),
        @SeatPrice,
        1, 4, @PickupName, @DropoffName,
        NULL, @TicketStatus
    );
    SET @NewTicketId = SCOPE_IDENTITY();

    -- Detail với QR code (32-char hex không dấu gạch — giống BoardingQrTokenGenerator)
    INSERT INTO [passenger_ticket_detail] (
        passengerTicketId, tripSeatId, seatCodeSnapshot,
        qrcode, fullName, phone, price, [status]
    )
    VALUES (
        @NewTicketId, @CurSeatId, @CurSeatCode,
        LOWER(REPLACE(CAST(NEWID() AS VARCHAR(36)), '-', '')),
        CASE @i
            WHEN 1 THEN N'Nguyễn Văn An'
            WHEN 2 THEN N'Trần Thị Bình'
            WHEN 3 THEN N'Lê Quốc Cường'
            WHEN 4 THEN N'Phạm Thị Dung'
            WHEN 5 THEN N'Hoàng Minh Đức'
            ELSE        N'Vũ Thị Hoa (đã check-in)'
        END,
        '09' + RIGHT('0000000' + CAST(@i * 7777 AS VARCHAR(7)), 8),
        @SeatPrice,
        @DetailStatus
    );
    SET @NewDetailId = SCOPE_IDENTITY();

    -- Accompanied child cho vé số 3
    IF @HasChild = 1
    BEGIN
        INSERT INTO [accompanied_child] (ticketDetailId, fullname, birthYear)
        VALUES (@NewDetailId, N'Lê Bé Gấu', 2021);
    END;

    -- Payment record
    INSERT INTO [payment] (
        passengerTicketId, cargoTicketId, amount, paymentMethod,
        transactionId, [status], paymentTime
    )
    VALUES (
        @NewTicketId, NULL, @SeatPrice, 'SEPAY',
        'TXN_TS33_' + RIGHT('00' + CAST(@i AS VARCHAR(2)), 2),
        'COMPLETED', GETDATE()
    );

    SET @i = @i + 1;
END;

-- ============================================================
-- BƯỚC 5: In QR codes để test trực tiếp
-- ============================================================
PRINT N'';
PRINT N'=== QR TOKENS cho trip ' + CAST(@TestTripId AS VARCHAR(10)) + ' (06:00 hôm nay) ===';
SELECT
    ptd.ticketDetailId,
    ptd.fullName,
    ptd.seatCodeSnapshot AS seat,
    ptd.[status],
    ptd.qrcode AS qr_token_to_scan
FROM [passenger_ticket_detail] ptd
JOIN [passenger_ticket] pt ON pt.passengerTicketId = ptd.passengerTicketId
WHERE pt.tripId = @TestTripId
ORDER BY ptd.ticketDetailId;

PRINT N'';
PRINT N'=== THÔNG TIN ĐĂNG NHẬP TRIP STAFF ===';
PRINT N'Username: 0932000001  |  Password: 123456';
PRINT N'StaffId: ' + CAST(@StaffId33 AS VARCHAR(10));
PRINT N'Trips của staff hôm nay (2026-07-03): 3 chuyến (06:00, 13:00, 20:00)';
PRINT N'Trip test có vé: tripId = ' + CAST(@TestTripId AS VARCHAR(10));
PRINT N'  - Vé 1-5: CONFIRMED → có thể check-in (cả manual lẫn QR)';
PRINT N'  - Vé 6: CHECKED_IN → sẽ báo lỗi "đã quét trước đó" khi quét lại';
PRINT N'  - Vé 3 (Lê Quốc Cường): có trẻ em đi kèm "Lê Bé Gấu" (sinh 2021)';
