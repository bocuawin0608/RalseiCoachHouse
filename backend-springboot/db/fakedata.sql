USE VeXeDB;
GO

-- Khóa tính năng in thông báo rác để chạy cho nhanh
SET NOCOUNT ON;

-- ============================================================================
-- 1. INSERT LEVEL 1: STRONG ENTITIES
-- ============================================================================
PRINT 'Inserting Level 1...';

-- Accounts (Tài xế, nhân viên, khách hàng)
INSERT INTO [account]
    ([username], [passwordHash], [isActive])
VALUES
    ('0912345678', 'hash_pass_1', 1),
    ('0923456789', 'hash_pass_2', 1),
    ('0934567890', 'hash_pass_3', 1),
    ('0945678901', 'hash_pass_4', 1),
    ('0956789012', 'hash_pass_5', 1),
    ('0967890123', 'hash_pass_6', 1),
    ('0978901234', 'hash_pass_7', 1),
    ('0989012345', 'hash_pass_8', 1),
    ('0990123456', 'hash_pass_9', 1),
    ('0901234567', 'hash_pass_10', 1);

-- Roles
INSERT INTO [role]
    (roleName)
VALUES
    ('admin'),
    ('manager'),
    ('ticket_staff'),
    ('trip_staff'),
    ('customer');

-- Vouchers (Đã FIX lỗi thừa cột ở câu VALUES!)
INSERT INTO [voucher]
    ([voucherCode], [discountValue], [startEffectiveDate], [endEffectiveDate], [discountType], [maxDiscountValue], [minOrderValue], [usageLimit])
VALUES
    ('UUDAI2026', 10.00, '2026-05-28', '2027-05-28', 'percent', 50000.00, 100000.00, 500),
    ('GIAM50K', 50000.00, '2026-05-28', '2027-05-28', 'fixed', 50000.00, 200000.00, 200);

-- Coach Stops (Bến xe/Trạm dừng dọc tuyến Bắc - Nam)
INSERT INTO [coach_stop]
    ([stopPointName], [address])
VALUES
    (N'Bến Xe Mỹ Đình', N'Phạm Hùng, Mỹ Đình, Nam Từ Liêm, Hà Nội'),
    (N'Trạm Dừng Thanh Hóa', N'Quốc lộ 1A, Quảng Xương, Thanh Hóa'),
    (N'Bến Xe Trung Tâm Đà Nẵng', N'Tôn Đức Thắng, Hòa Minh, Liên Chiểu, Đà Nẵng'),
    (N'Trạm Dừng Nha Trang', N'Quốc lộ 1A, Diên Khánh, Khánh Hòa'),
    (N'Bến Xe Miền Đông', N'Đinh Bộ Lĩnh, Phường 26, Bình Thạnh, TP. HCM');

-- Routes (Tuyến đường)
INSERT INTO [route]
    ([routeName], [totalKilometers], [totalMinutes])
VALUES
    (N'Hà Nội - TP. Hồ Chí Minh', 1720.00, 1920),
    -- ~32 tiếng
    (N'Hà Nội - Đà Nẵng', 760.00, 840),
    -- ~14 tiếng
    (N'Đà Nẵng - TP. Hồ Chí Minh', 960.00, 1080)

;
-- ~18 tiếng

-- Seat Layouts (Phân tách rõ ràng 3 loại theo yêu cầu của anh)
INSERT INTO [seat_layout]
    ([seatLayoutName], [totalSeat])
VALUES
    (N'Thường (Ghế ngồi 45 chỗ)', 45),
    (N'Luxury (Giường nằm 34 phòng)', 34),
    (N'Limousine (Phòng VIP 22 chỗ)', 22);

-- Cargo Types
INSERT INTO [cargo_type]
    ([cargoTypeName])
VALUES
    (N'Hàng hóa ký gửi đóng thùng'),
    (N'Xe máy / Phương tiện'),
    (N'Tài liệu bưu phẩm');

-- ============================================================================
-- 2. INSERT LEVEL 2: ASSOCIATIVE & WEAK ENTITIES
-- ============================================================================
PRINT 'Inserting Level 2...';

-- Account Roles
INSERT INTO [account_role]
    (accountId, roleId)
VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 3),
    (5, 3),
    (6, 3),
    (7, 2),
    (8, 4),
    (9, 4),
    (10, 4);

-- Customers
INSERT INTO [customer]
    ([accountId], [customerName], [phone], [email])
VALUES
    (8, N'Đoàn Ngọc Đức', '0989012345', 'duc.dn@gmail.com'),
    (9, N'Nguyễn Văn Trỗi', '0990123456', 'troi.nv@gmail.com'),
    (10, N'Lê Thị Hoa', '0901234567', 'hoa.lt@gmail.com'),
    (NULL, N'Khách Vãng Lai A', '0123444555', NULL);

-- Ticket Agencies (Đại lý tại bến)
INSERT INTO [ticket_agency]
    ([stopPointId], [ticketAgencyName])
VALUES
    (1, N'Quầy Vé Mỹ Đình 01'),
    (3, N'Quầy Vé Đà Nẵng 02'),
    (5, N'Quầy Vé Miền Đông 01');

-- Staff (Phân công Driver, Attendant, Ticket Staff)
INSERT INTO [staff]
    ([accountId], [ticketAgencyId], [staffName], [phone], [staffPosition], [hireDate])
VALUES
    (2, 1, N'Nguyễn Văn Minh', '0923456789', 'Ticket Staff', '2025-01-10'),
    (3, NULL, N'Tài xế Trần Văn Đạo', '0934567890', 'Driver', '2024-05-15'),
    (4, NULL, N'Phụ xe Lê Văn Long', '0945678901', 'Attendant', '2025-02-20'),
    (5, NULL, N'Tài xế Ngô Quốc Bảo', '0956789012', 'Driver', '2023-11-01'),
    (6, NULL, N'Phụ xe Hoàng Văn Vũ', '0967890123', 'Attendant', '2025-03-01'),
    (7, 2, N'Phạm Thị Ngọc', '0978901234', 'Ticket Staff', '2025-04-12');

-- Route Stops (Chi tiết trạm dừng của tuyến)
INSERT INTO [route_stop]
    ([routeId], [stopPointId], [stopOrder], [kilometersFromStart], [minutesFromStart])
VALUES
    (1, 1, 1, 0.00, 0),
    (1, 2, 2, 150.00, 180),
    (1, 3, 3, 760.00, 840),
    (1, 4, 4, 1280.00, 1440),
    (1, 5, 5, 1720.00, 1920);

-- Seats Generation
-- Layout 1 (Thường): Ghế A01 -> A05
INSERT INTO [seat]
    ([seatLayoutId], [seatCode], [rowIndex], [colIndex])
VALUES
    (1, 'A01', 1, 1),
    (1, 'A02', 1, 2),
    (1, 'A03', 2, 1),
    (1, 'A04', 2, 2),
    (1, 'A05', 3, 1);
-- Layout 2 (Luxury): Giường L01 -> L05
INSERT INTO [seat]
    ([seatLayoutId], [seatCode], [rowIndex], [colIndex])
VALUES
    (2, 'L01', 1, 1),
    (2, 'L02', 1, 2),
    (2, 'L03', 2, 1),
    (2, 'L04', 2, 2),
    (2, 'L05', 3, 1);
-- Layout 3 (Limousine): VIP01 -> VIP05
INSERT INTO [seat]
    ([seatLayoutId], [seatCode], [rowIndex], [colIndex])
VALUES
    (3, 'VIP01', 1, 1),
    (3, 'VIP02', 1, 2),
    (3, 'VIP03', 2, 1),
    (3, 'VIP04', 2, 2),
    (3, 'VIP05', 3, 1);

-- Prices Configuration
INSERT INTO [seat_layout_price]
    ([seatLayoutId], [seatPrice], [startEffectiveDate], [endEffectiveDate])
VALUES
    (1, 250000.00, '2026-05-28', '2027-05-28'),
    -- Thường
    (2, 450000.00, '2026-05-28', '2027-05-28'),
    -- Luxury
    (3, 650000.00, '2026-05-28', '2027-05-28');
-- Limousine

INSERT INTO [cargo_type_price]
    ([cargoTypeId], [unit], [pricePerUnit], [startEffectiveDate], [endEffectiveDate])
VALUES
    (1, N'kg', 5000.00, '2026-05-28', '2027-05-28'),
    (2, N'chiếc', 300000.00, '2026-05-28', '2027-05-28'),
    (3, N'kiện', 30000.00, '2026-05-28', '2027-05-28');

-- Coach Generation (3 loại xe chia theo Layout)
INSERT INTO [coach]
    ([seatLayoutId], [licensePlate], [status], [manufacturer], [year])
VALUES
    (1, '29B-111.11', 'active', N'Thaco Thường 45 chỗ', 2022),
    (1, '29B-111.22', 'active', N'Thaco Thường 45 chỗ', 2023),
    (2, '30F-222.22', 'active', N'Universe Luxury Giường nằm', 2024),
    (2, '30F-222.33', 'active', N'Universe Luxury Giường nằm', 2024),
    (3, '51B-333.33', 'active', N'DCar Limousine VIP VIP', 2025),
    (3, '51B-333.44', 'active', N'DCar Limousine VIP VIP', 2025);

-- ============================================================================
-- 3. AUTOMATED LOOP INSERT FOR OPERATIONAL DATA (LEVEL 3, 4, 5, 6)
-- DÀN ĐỀU TỪ 28/05/2026 ĐẾN 28/05/2027
-- ============================================================================
PRINT 'Running loop to populate operational data (70-100 rows)...';

DECLARE @LoopDate DATETIME = '2026-05-28 08:00:00';
DECLARE @EndDate DATETIME = '2027-05-28 00:00:00';
DECLARE @TripId INT;
DECLARE @PassengerTicketId INT;
DECLARE @PaymentId INT;
DECLARE @Counter INT = 1;

-- Cứ cách 4.5 ngày sinh 1 cụm data chuyến xe (~81 chuyến xe trong 1 năm)
WHILE @LoopDate < @EndDate
BEGIN
    -- Đảm bảo driverId <> attendantId
    DECLARE @CurrentDriver INT = CASE WHEN @Counter % 2 = 0 THEN 2 ELSE 4 END;
    DECLARE @CurrentAttendant INT = CASE WHEN @Counter % 2 = 0 THEN 3 ELSE 5 END;
    -- Luân phiên 3 loại xe: Thường (1,2), Luxury (3,4), Limousine (5,6)
    DECLARE @CurrentCoach INT = (@Counter % 6) + 1;
    -- Luân phiên 3 tuyến đường
    DECLARE @CurrentRoute INT = (@Counter % 3) + 1;

    -- Insert Chuyến Xe (Trip)
    INSERT INTO [trip]
        ([routeId], [coachId], [departureTime], [status], [driverId], [attendantId])
    VALUES
        (
            @CurrentRoute,
            @CurrentCoach,
            @LoopDate,
            CASE WHEN @LoopDate < GETDATE() THEN 'completed' ELSE 'scheduled' END,
            @CurrentDriver,
            @CurrentAttendant
    );

    SET @TripId = SCOPE_IDENTITY();

    -- Xác định thông tin vé dựa theo loại xe
    DECLARE @CustId INT = (@Counter % 4) + 1;
    DECLARE @SeatId INT = CASE 
        WHEN @CurrentCoach IN (1,2) THEN (@Counter % 5) + 1  -- Ghế thường ID 1-5
        WHEN @CurrentCoach IN (3,4) THEN (@Counter % 5) + 6  -- Giường nằm ID 6-10
        ELSE (@Counter % 5) + 11                             -- VIP Limousine ID 11-15
    END;

    DECLARE @Price DECIMAL(15,2) = CASE 
        WHEN @CurrentCoach IN (1,2) THEN 250000.00
        WHEN @CurrentCoach IN (3,4) THEN 450000.00
        ELSE 650000.00
    END;

    -- Insert Vé Hành Khách (Passenger Ticket) - Đã đồng bộ chuẩn số lượng cột
    INSERT INTO [passenger_ticket]
        ([customerId], [tripId], [voucherId], [soldBy], [ticketCode], [totalPrice], [pickupStopId], [dropoffStopId], [status], [createdAt])
    VALUES
        (
            CASE WHEN @CustId = 4 THEN NULL ELSE @CustId END,
            @TripId,
            CASE WHEN @Counter % 5 = 0 THEN 1 ELSE NULL END,
            1,
            'TK-' + CAST(@Counter AS VARCHAR) + '-' + CONVERT(VARCHAR, @LoopDate, 112),
            @Price,
            1,
            5,
            CASE WHEN @LoopDate < GETDATE() THEN 'confirmed' ELSE 'pending' END,
            DATEADD(day, -2, @LoopDate)
    );

    SET @PassengerTicketId = SCOPE_IDENTITY();

    -- Insert Chi tiết vé hành khách (Passenger Ticket Detail)
    INSERT INTO [passenger_ticket_detail]
        ([passengerTicketId], [seatId], [fullName], [phone], [dob], [cccd], [price], [status])
    VALUES
        (
            @PassengerTicketId,
            @SeatId,
            N'Hành Khách Số ' + CAST(@Counter AS NVARCHAR),
            '0911111' + RIGHT('00' + CAST(@Counter AS VARCHAR), 3),
            '1995-08-15',
            '037095001' + RIGHT('00' + CAST(@Counter AS VARCHAR), 3),
            @Price,
            CASE WHEN @LoopDate < GETDATE() THEN 'checked_in' ELSE 'pending' END
    );

    -- Insert Thanh toán hóa đơn (Payment)
    INSERT INTO [payment]
        ([passengerTicketId], [cargoTicketId], [amount], [paymentMethod], [transactionId], [status], [paymentTime])
    VALUES
        (
            @PassengerTicketId,
            NULL,
            @Price,
            CASE WHEN @Counter % 3 = 0 THEN 'vnpay' WHEN @Counter % 3 = 1 THEN 'bank_transfer' ELSE 'cash' END,
            'TRANS-' + CAST(@Counter AS VARCHAR),
            CASE WHEN @LoopDate < GETDATE() THEN 'completed' ELSE 'pending' END,
            DATEADD(day, -2, @LoopDate)
    );

    SET @PaymentId = SCOPE_IDENTITY();

    -- Giả lập kịch bản hoàn tiền tự động cho 1 vài vé (Ví dụ cụm vòng lặp chia hết cho 12)
    IF @Counter % 12 = 0
    BEGIN
        UPDATE [passenger_ticket] SET [status] = 'cancelled' WHERE [passengerTicketId] = @PassengerTicketId;
        UPDATE [passenger_ticket_detail] SET [status] = 'cancelled' WHERE [passengerTicketId] = @PassengerTicketId;

        INSERT INTO [refund]
            ([paymentId], [amount], [reason], [refundMethod], [status], [refundTime])
        VALUES
            (@PaymentId, @Price, N'Khách bận việc đột xuất', 'bank_transfer', 'completed', @LoopDate);
    END

    -- Tịnh tiến thời gian lên 4.5 ngày (6480 phút) để dàn trải đều dữ liệu suốt cả năm
    SET @LoopDate = DATEADD(minute, 6480, @LoopDate);
    SET @Counter = @Counter + 1;
END;

PRINT '=======================================================';
PRINT 'All data merged and inserted successfully!';
PRINT 'Total simulated trips generated: ' + CAST(@Counter - 1 AS VARCHAR);
PRINT '=======================================================';
SET NOCOUNT OFF;
GO

SELECT *
FROM [role];
USE VeXeDB;
SELECT *
FROM route t;


SELECT *
FROM route r JOIN seat_layout sl ON r.routeId = sl.seatLayoutId
    JOIN trip t ON r.routeId = t.routeId

SELECT r.routeName, sl.seatLayoutName, t.[status], t.departureTime
FROM
    trip t JOIN route r ON t.routeId = r.routeId
    JOIN coach c ON t.coachId = c.coachId
    JOIN seat_layout sl ON c.seatLayoutId = sl.seatLayoutId
WHERE t.departureTime BETWEEN '2026-05-25 00:00:00' AND '2026-05-25 23:59:59'
    AND r.routeName = N'Hà Nội - Quảng Bình';

SELECT *
FROM coach;
SELECT *
FROM seat_layout;
SELECT *
FROM trip;

SELECT r.routeName AS routeName,
    sl.seatLayoutName AS seatLayoutName,
    t.[status] AS status,
    t.departureTime AS departureTime
FROM [trip] t
    JOIN route R ON t.routeId = R.routeId
    JOIN coach C ON t.coachId = C.coachId
    JOIN seat_layout SL ON C.seatLayoutId = SL.seatLayoutId
WHERE t.departureTime BETWEEN '2026-05-25 00:00:00' AND '2026-05-25 23:59:59'
    AND R.routeName = N'Hà Nội - Quảng Bình';
        SELECT * FROM Trip

SELECT 
    r.routeName AS routeName, 
    sl.seatLayoutName AS seatLayoutName, 
    t.status AS status, 
    t.departureTime AS departureTime
FROM trip t 
JOIN route r ON t.routeId = r.routeId
JOIN coach c ON t.coachId = c.coachId
JOIN seat_layout sl ON c.seatLayoutId = sl.seatLayoutId
WHERE t.departureTime BETWEEN '2026-05-30 00:00:00' AND '2026-05-30 23:59:59'
    AND r.routeName = N'Hà Nội - Quảng Bình'