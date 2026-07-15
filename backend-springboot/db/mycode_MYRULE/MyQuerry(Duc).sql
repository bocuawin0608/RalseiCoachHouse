USE vexedb
SELECT a.username, s.email
FROM account a JOIN account_role ar ON a.accountId = ar.accountId JOIN role r on ar.roleId = r.roleId JOIN staff s on s.accountId = a.accountId
SELECT *
FROM coach
SELECT *
FROM coach_type
SELECT *
FROM coach_stop
SELECT *
FROM trip
SELECT *
FROM trip_seat
SELECT *
FROM route_stop
SELECT *
FROM trip_seat

-- Customer trip seat count: same logic used by TripRepository.
DECLARE @departureTime DATETIME = '2026-07-01 08:00:00';

SELECT
    t.tripId,
    t.coachId,
    c.licensePlate,
    t.departureTime,
    COUNT(ts.tripSeatId) AS totalSeatsInTrip,
    SUM(CASE WHEN ts.[status] = 'AVAILABLE' THEN 1 ELSE 0 END) AS availableSeats,
    SUM(CASE WHEN ts.[status] IN ('LOCKED', 'SOLD') THEN 1 ELSE 0 END) AS unavailableSeats
FROM trip t
    JOIN coach c ON c.coachId = t.coachId
    JOIN trip_seat ts ON ts.tripId = t.tripId
    JOIN seat s ON s.seatId = ts.seatId
WHERE t.departureTime = @departureTime
    AND t.[status] = 'SCHEDULED'
    AND s.isActive = 1
GROUP BY
    t.tripId,
    t.coachId,
    c.licensePlate,
    t.departureTime;

-- Debug one tripId directly from trip_seat.
DECLARE @TripId INT = 1;
SELECT
    ts.tripId,
    COUNT(ts.tripSeatId) AS totalSeatsInTrip,
    SUM(CASE WHEN ts.[status] = 'AVAILABLE' THEN 1 ELSE 0 END) AS availableSeats,
    SUM(CASE WHEN ts.[status] IN ('LOCKED', 'SOLD') THEN 1 ELSE 0 END) AS unavailableSeats
FROM trip_seat ts
    JOIN seat s ON s.seatId = ts.seatId
WHERE ts.tripId = @TripId
    AND s.isActive = 1
GROUP BY ts.tripId;

USE VeXeDB;
GO

-------------
SELECT
    t.tripId AS tripId,
    ct.coachTypeName AS coachTypeName,
    r.routeName AS routeName,
    t.departureTime AS departureTime,
    ctp.seatPrice AS seatPrice
FROM trip t
    JOIN route r ON t.routeId = r.routeId
    JOIN coach c ON t.coachId = c.coachId
    JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId
    JOIN coach_type_price ctp ON ct.coachTypeId = ctp.coachTypeId
WHERE r.routeName = N'Hà Nội - Quảng Bình'
    AND t.departureTime BETWEEN '2026-01-01 00:00:00' AND '2026-01-01 23:59:59'
    AND '2026-01-01 08:00:00' BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate -- Giả lập thời gian hiện tại
    AND (1 = 0 OR ( -- checkTimeSlots = 1 để kích hoạt lọc ca chạy
      ('04:00:00' IS NOT NULL AND CAST(t.departureTime AS TIME) BETWEEN '04:00:00' AND '06:00:00') OR
    ('08:00:00' IS NOT NULL AND CAST(t.departureTime AS TIME) BETWEEN '08:00:00' AND '10:00:00') OR
    (NULL IS NOT NULL AND CAST(t.departureTime AS TIME) BETWEEN NULL AND NULL) OR
    (NULL IS NOT NULL AND CAST(t.departureTime AS TIME) BETWEEN NULL AND NULL)
  ))
    AND (1 = 0 OR ( -- checkLayouts = 1 để kích hoạt lọc loại xe
      ct.coachTypeName LIKE N'%Limousine%'
  ))
    AND (200000.00 IS NULL OR ctp.seatPrice >= 200000.00) -- minPrice
    AND (900000.00 IS NULL OR ctp.seatPrice <= 900000.00); -- maxPrice
GO


USE VeXeDB;
GO

SELECT t.tripId, ct.coachTypeName, r.routeName, t.departureTime, ctp.seatPrice
FROM trip t
    JOIN route r ON t.routeId = r.routeId
    JOIN coach c ON t.coachId = c.coachId
    JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId
    JOIN coach_type_price ctp ON ct.coachTypeId = ctp.coachTypeId
WHERE t.departureTime BETWEEN '2026-01-02 00:00:00' AND '2026-01-03 00:00:00'
    AND r.routeName = N'Hà Nội - Quảng Bình'
ORDER BY t.departureTime;
GO

USE VeXeDB;
GO

SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END AS IsCoachOccupied
FROM trip t
WHERE t.coachId = 12 -- ID xe cần kiểm tra (:coachId)
    AND t.tripId <> 0 -- Nếu là tạo mới thì truyền 0, nếu là sửa chính chuyến đó thì truyền ID của chuyến (:currentTripId)
    AND t.status <> 'CANCELLED' -- Bỏ qua các chuyến đã hủy vì chúng không chiếm xe
    AND t.departureTime = '2026-01-02 08:00:00';        -- Thời gian dự định xuất phát (:departureTime)
GO

USE VeXeDB;
GO

-- Chạy lệnh UPDATE logic
UPDATE trip 
SET status = 'CANCELLED', 
    updatedAt = GETDATE(), 
    updatedBy = 1                                    -- ID của Manager thực hiện (:managerId)
WHERE tripId = 10;
-- ID chuyến xe cần hủy (:tripId)

-- Kiểm tra lại kết quả xem dòng đó đã đổi trạng thái chưa
SELECT tripId, routeId, coachId, departureTime, status, updatedAt, updatedBy
FROM trip
WHERE tripId = 10;
GO
USE VeXeDB
-- Giả sử Manager số 2 thực hiện sinh lịch tự động cho 1 tuần từ ngày 01/06/2026
EXEC sp_AutoGenerateWeeklySchedule_Final @StartDate = '2026-06-27';

-- Lệnh 1: Kiểm tra tổng số lượng chuyến xe sinh ra trong ngày đầu tiên xem có chạm mốc 20 chuyến không
SELECT CAST(departureTime AS DATE) AS NgayChay, COUNT(*) AS TongSoChuyenTrongNgay
FROM [trip]
WHERE departureTime BETWEEN '2026-06-01 00:00:00' AND '2026-06-07 23:59:59'
GROUP BY CAST(departureTime AS DATE)
ORDER BY NgayChay;

-- Lệnh 2: Kiểm tra tính đúng đắn của quy trình xoay đầu xe
-- Xem thử con xe số 3 chạy chiều đi tối hôm trước có đúng là sẽ chạy chiều về trưa hôm sau không
SELECT tripId, routeId, coachId, departureTime, status, driverId
FROM [trip]
WHERE coachId = 3
ORDER BY departureTime ASC;

USE VeXeDB;
GO

-- Kiểm tra ngày lớn nhất hiện tại đang có trong DB là ngày nào
DECLARE @MaxDateInDb DATE;
SELECT @MaxDateInDb = MAX(CAST(departureTime AS DATE))
FROM [trip];

PRINT N'-> Ngày lớn nhất hiện tại trong DB: ' + COALESCE(CAST(@MaxDateInDb AS VARCHAR(10)), 'NULL');

-- Định nghĩa ngày gối đầu tiếp theo (MaxDate + 1 ngày)
DECLARE @NextScheduleDate DATE = DATEADD(DAY, 1, @MaxDateInDb);

PRINT N'-> Tiến hành gọi EXEC sinh lịch gối đầu cho ngày: ' + CAST(@NextScheduleDate AS VARCHAR(10));

-- Kích nổ
EXEC sp_AutoGenerateWeeklySchedule_Final @StartDate = @NextScheduleDate;

-- Kiểm tra xem các chuyến mới tạo có đúng là bắt đầu từ ngày gối đầu không
SELECT t.tripId, t.routeId, t.departureTime, t.createdBy
FROM [trip] t
WHERE CAST(t.departureTime AS DATE) >= @NextScheduleDate
ORDER BY t.departureTime ASC;


SELECT COUNT(DISTINCT CAST(departureTime AS DATE)) AS [SO_NGAY_CO_LICH]
FROM [trip]
WHERE CAST(departureTime AS DATE) BETWEEN '2026-06-16' AND '2026-06-28';


USE VeXeDB;
GO

BEGIN TRANSACTION;
BEGIN TRY

    -- ============================================================================
    -- STEP 1: TẠO DỮ LIỆU CHA VỚI ID = 1000 (Bật IDENTITY_INSERT để ép ID)
    -- ============================================================================
    
    -- 1. Tạo Tuyến đường ID = 1000
    SET IDENTITY_INSERT [route] ON;
    INSERT INTO [route]
    (routeId, routeName, totalKilometers, totalMinutes, isActive)
VALUES
    (1000, N'Tuyến Đường Thử Nghiệm 1000', 350.00, 300, 1);
    SET IDENTITY_INSERT [route] OFF;

    -- 2. Tạo Loại xe ID = 1000
    SET IDENTITY_INSERT [coach_type] ON;
    INSERT INTO [coach_type]
    (coachTypeId, coachTypeName, totalSeat, isActive)
VALUES
    (1000, N'Xe Phòng Nằm Thượng Hạng 1000', 22, 1);
    SET IDENTITY_INSERT [coach_type] OFF;

    -- 3. Tạo Xe cụ thể ID = 1000 (Gán vào Route 1000 và CoachType 1000)
    SET IDENTITY_INSERT [coach] ON;
    INSERT INTO [coach]
    (coachId, routeId, coachTypeId, licensePlate, [status], manufacturer, [year])
VALUES
    (1000, 1000, 1000, '30K-999.00', 'ACTIVE', N'Thaco Mobihome', 2026);
    SET IDENTITY_INSERT [coach] OFF;


    -- ============================================================================
    -- STEP 2: HÀNH VI C-U-D CHO THẰNG TRIP 1000
    -- ============================================================================

    -- [CREATE] - Thêm mới Trip ID = 1000
   USE VeXeDB
GO

-- Xóa theo thứ tự từ ngọn xuống gốc để không dính Foreign Key
DELETE FROM [trip_seat] WHERE [tripId] = 1000;
DELETE FROM [passenger_ticket] WHERE [tripId] = 1000;
DELETE FROM [trip] WHERE [tripId] = 1000;

DELETE FROM [coach] WHERE [coachId] = 1000;
DELETE FROM [coach_type] WHERE [coachTypeId] = 1000;
DELETE FROM [route] WHERE [routeId] = 1000;

PRINT '=== ĐÃ DỌN SẠCH DỮ LIỆU RÁC ID 1000! CHỜ ANH TEST LẠI ===';
GO
PRINT '1. [CREATE] Tạo thành công Trip ID = 1000';


-- [UPDATE] - Sửa đổi trạng thái của Trip 1000
UPDATE [trip]
    SET [status] = 'IN_PROGRESS',
        [updatedAt] = GETDATE(),
        [updatedBy] = 999
    WHERE [tripId] = 1000;
PRINT '2. [UPDATE] Cập nhật thành công trạng thái Trip 1000';


-- [DELETE] - Dọn dẹp sạch sẽ Trip 1000 (Và các bảng con của nó nếu có phát sinh ghé/vé)
DELETE FROM [trip_seat] WHERE [tripId] = 983475893475894375893475;

SET IDENTITY_INSERT [trip] ON;
INSERT INTO [trip]
    (
    [tripId], [routeId], [coachId], [departureTime], [status], [driverId], [attendantId], [createdBy]
    )
VALUES
    (
        1000, 1000, 1000, '2026-06-25 14:00:00', 'SCHEDULED', NULL, NULL, 999
);
SET IDENTITY_INSERT [trip] OFF;

PRINT '=== ĐÃ INSERT TRIP 1000 THÀNH CÔNG! ===';
SELECT *
FROM [trip]
WHERE [tripId] = 1000;
GO

USE VeXeDB;
GO

-- 1. Bật tính năng ép ID để tạo Staff 1001 (Tài xế)
SET IDENTITY_INSERT [staff] ON;
INSERT INTO [staff]
    (
    [staffId], [staffName], [phone], [staffPosition], [hireDate], [isActive]
    )
VALUES
    (
        1001, N'Nguyễn Văn Lái', '0912345678', 'DRIVER', '2026-01-01', 1
);

-- 2. Tạo Staff 1002 (Phụ xe)
INSERT INTO [staff]
    (
    [staffId], [staffName], [phone], [staffPosition], [hireDate], [isActive]
    )
VALUES
    (
        1002, N'Trần Văn Phụ', '0987654321', 'ATTENDANT', '2026-01-01', 1
);
SET IDENTITY_INSERT [staff] OFF;

PRINT '=== ĐÃ TẠO XONG NHÂN SỰ MẪU 1001 VÀ 1002 ===';
GO

USE VeXeDB;
GO

-- Lệnh INSERT sử dụng dữ liệu nền có sẵn từ Script Seed của anh
INSERT INTO [trip]
    (
    [routeId], -- Nhận giá trị 1 (Tuyến Hà Nội - Quảng Bình) hoặc 2
    [coachId], -- Chọn xe ID = 50 (Nằm trong dải 1 đến 365 chắc chắn tồn tại)
    [departureTime], -- Giờ khởi hành
    [status], -- Trạng thái ban đầu
    [driverId], -- Tạm thời để NULL để điều phối sau giống logic sinh tự động của anh
    [attendantId], -- Tạm thời để NULL
    [createdBy] -- 1 (ID tài khoản Admin Hệ Thống)
    )
VALUES
    (
        1,
        50,
        '2026-01-20 08:30:00', -- Ngày nằm ngoài dải snapshot để tránh xung đột lịch trình
        'SCHEDULED',
        NULL,
        NULL,
        1
);
USE VeXeDB;
GO

SELECT *
FROM [trip]
WHERE [driverId] IS NULL;
SELECT *
FROM TRIP-- Lấy ra ID của chuyến xe vừa sinh ra để anh dùng debug tiếp
SELECT SCOPE_IDENTITY() AS CurrentTestTripId;


INSERT INTO [trip]
    (
    [routeId], -- 1: Tuyến Hà Nội - Quảng Bình (Đã có sẵn)
    [coachId], -- 50: Xe số 50 thuộc dải xe từ 1 đến 365 (Đã có sẵn)
    [departureTime], -- Thời gian khởi hành (Nằm ngoài dải seed để dễ check)
    [status], -- 'SCHEDULED' (Theo ràng buộc CK_Trip_Status)
    [driverId], -- 3: ID của Tài xế số 1 trong bảng staff (Từ 3 đến 82)
    [attendantId], -- 83: ID của Phụ xe số 1 trong bảng staff (Từ 83 đến 162)
    [createdBy] -- 1: ID người tạo (Admin)
    )
VALUES
    (
        1,
        50,
        '2026-07-20 08:30:00',
        'SCHEDULED',
        3,
        83,
        1
);



USE VeXeDB;
GO

-- Khai báo giả lập tham số truyền từ JPA @Param vào
DECLARE @routeId INT = 1;
-- Tuyến HN - QB
DECLARE @coachId INT = 15;
-- Xe số 15 (ACTIVE)
DECLARE @departureTime DATETIME = '2026-01-20 07:00:00';
-- Ngày mới tinh không sợ trùng lịch
DECLARE @driverId INT = 5;
-- Tài xế số 3 (staffId = 5)
DECLARE @attendantId INT = 85;
-- Phụ xe số 3 (staffId = 85)
DECLARE @createdBy INT = 1;
-- Admin

INSERT INTO [trip]
    (
    [routeId],
    [coachId],
    [departureTime],
    [status],
    [driverId],
    [attendantId],
    [createdBy]
    )
VALUES
    (
        @routeId,
        @coachId,
        @departureTime,
        'SCHEDULED',
        @driverId,
        @attendantId,
        @createdBy
);

PRINT '-> Test Lệnh INSERT TRIP đơn lẻ: THÀNH CÔNG!';
SELECT TOP 1
    *
FROM [trip]
WHERE [departureTime] = '2026-01-20 07:00:00';
GO

USE VeXeDB;
GO

-- Kịch bản: Kiểm tra ông tài xế số 3 (staffId = 5). 
-- Theo dữ liệu seed, ông này chắc chắn đã có lịch chạy vào một ngày nào đó trong khoảng 01/01 đến 15/01.
-- Ta lấy bừa một chuyến của ông ấy ra để test xem hàm có bắt được không.

DECLARE @driverId INT = 5;
DECLARE @testDepartureTime DATETIME;

-- Bốc thử 1 lịch có sẵn của ông này trong DB để làm tham số test
SELECT TOP 1
    @testDepartureTime = departureTime
FROM [trip]
WHERE [driverId] = @driverId AND [status] = 'SCHEDULED';

-- Câu lệnh map từ Repository qua để đếm số ca xung đột lịch của tài xế
SELECT COUNT(*) AS DriverConflictCount
FROM [trip]
WHERE [driverId] = @driverId
    AND [status] IN ('SCHEDULED', 'IN_PROGRESS')
    -- Kiểm tra xem khoảng cách thời gian giữa chuyến mới định tạo và các chuyến cũ có < 180 phút không
    AND ABS(DATEDIFF(MINUTE, [departureTime], @testDepartureTime)) < 180;
GO
USE VeXeDB;
SELECT status
FROM trip
ORDER BY [status] ASC

USE VeXeDB
SELECT *
FROM trip t JOIN coach c ON t.coachId = c.coachId
SELECT *
FROM seat
select *
from trip_seat
SELECT *
FROM trip


SELECT routeName
FROM route

SELECT *
FROM COACH
SELECT *
FROM coach_type

SELECT *
FROM TRIP

SELECT licensePlate, ct.coachTypeName
FROM coach c JOIN coach_type ct
    ON c.coachTypeId = ct.coachTypeId
    JOIN trip t ON t.coachId = c.coachId
    JOIN route rt ON rt.routeId = t.tripId
WHERE CAST(t.departureTime AS DATE) = '2026-01-01'



USE VeXeDB

SELECT s.staffName
FROM staff s
    JOIN account a ON a.accountId = s.accountId
    JOIN account_role ar ON ar.accountId = a.accountId
    JOIN [role] r ON r.roleId = ar.roleId
WHERE r.roleName = 'TRIP_STAFF' AND s.staffPosition = 'DRIVER'
    AND EXISTS (
    SELECT 1
    FROM trip t
    WHERE t.driverId = s.staffId
        AND CAST(t.departureTime AS DATE) = '2026-01-01'
);

SELECT s.staffName
FROM staff s
    JOIN account a ON a.accountId = s.accountId
    JOIN account_role ar ON ar.accountId = a.accountId
    JOIN [role] r ON r.roleId = ar.roleId

WHERE r.roleName = 'TRIP_STAFF' AND s.staffPosition = 'DRIVER' OR s.staffPosition = 'ATTENDANT'
    AND EXISTS (
    SELECT 1
    FROM trip t
    WHERE t.driverId = s.staffId
        AND CAST(t.departureTime AS DATE) = '2026-01-01'
);

SELECT routeName
FROM ROUTE
WHERE routeName LIKE N'Hà Nội%'

SELECT *
FROM staff
where staffPosition = 'ATTENDANT'

SELECT routeName
FROM ROUTE

SELECT *
FROM [role]
SELECT *
FROM STAFF
SELECT *
FROM account_role
SELECt *
FROM account

USE VeXeDB
SELECT *
FROM trip_seat ts
    JOIN trip t ON t.tripId = ts.tripSeatId

SELECT * FROM account_role ar JOIN account a ON ar.accountId = a.accountId 
SELECT * FROM account_role ar JOIN account a ON ar.accountId = a.accountId JOIN role r on r.roleId = ar.roleId 
SELECT *
FROM account_role ar JOIN account a ON ar.accountId = a.accountId JOIN role r on r.roleId = ar.roleId

SELECT *
FROM coach_type


SELECT COUNT(*)
FROM trip_seat ts JOIN trip t on ts.tripId = t.tripId
WHERE ts.tripId = t.tripId


SELECT *
FROM TRIP


------------------
DECLARE @departureTime DATETIME = '2026-07-01 08:00:00';

SELECT
    t.tripId,
    t.coachId,
    c.licensePlate,
    t.departureTime,

    COUNT(ts.tripSeatId) AS totalSeatsInTrip,

    SUM(CASE 
        WHEN ts.status = 'AVAILABLE' THEN 1 
        ELSE 0 
    END) AS availableSeats,

    SUM(CASE 
        WHEN ts.status IN ('LOCKED', 'SOLD') THEN 1 
        ELSE 0 
    END) AS unavailableSeats

FROM trip t
    JOIN coach c
    ON c.coachId = t.coachId
    JOIN trip_seat ts
    ON ts.tripId = t.tripId
    JOIN seat s
    ON s.seatId = ts.seatId

WHERE 
   t.departureTime = @departureTime
    AND t.status = 'SCHEDULED'
    AND s.isActive = 1

GROUP BY
    t.tripId,
    t.coachId,
    c.licensePlate,
    t.departureTime;


EXEC sp_AutoGenerateWeeklySchedule_Final @StartDate = '2026-07-01';

USE VeXeDB;

SELECT *
FROM route

SELECT *
FROM route

SELECT *
FROM trip



SELECT *
FROM route_stop

SELECT *
FROM coach_stop

-- Test customer stop timeline by concrete trip, not ambiguous coach/date.
DECLARE @TripId INT = 1;

SELECT
    t.tripId,
    r.routeName,
    cs.stopPointId,
    cs.stopPointName,
    cs.[address],
    cs.city,
    rs.stopOrder,
    rs.minutesFromStart,
    DATEADD(MINUTE, rs.minutesFromStart, t.departureTime) AS estimatedStopTime
FROM trip t
    JOIN route r ON r.routeId = t.routeId
    JOIN route_stop rs ON rs.routeId = t.routeId
    JOIN coach_stop cs ON cs.stopPointId = rs.stopPointId
WHERE t.tripId = @TripId
    AND cs.isActive = 1
ORDER BY rs.stopOrder ASC;

USE VeXeDB;
SELECT
    r.routeName AS routeName,
    c.manufacturer AS manufacturer,
    t.[status] AS tripStatus,
    ct.coachTypeName AS coachTypeName,
    c.licensePlate AS licensePlate,
    c.[status] AS coachStatus,
    CAST(t.departureTime AS DATE) AS departureDate,
    CAST(t.departureTime AS TIME) AS departureTime,
    COALESCE(seat_counts.availableSeats, 0) AS availableSeats,
    COALESCE(seat_counts.totalSeats, 0) AS totalSeats
FROM trip t
    LEFT JOIN route r ON t.routeId = r.routeId
    LEFT JOIN coach c ON t.coachId = c.coachId
    LEFT JOIN coach_type ct ON c.coachTypeId = ct.coachTypeId
    LEFT JOIN (
                SELECT
        ts.tripId,
        CAST(SUM(CASE WHEN UPPER(LTRIM(RTRIM(ts.[status]))) = 'AVAILABLE' THEN 1 ELSE 0 END) AS INT) AS availableSeats,
        CAST(COUNT(ts.tripSeatId) AS INT) AS totalSeats
    FROM trip_seat ts
        JOIN seat s ON s.seatId = ts.seatId
    WHERE s.isActive = 1
    GROUP BY ts.tripId
    
            ) seat_counts ON seat_counts.tripId = t.tripId
WHERE CAST(t.departureTime AS DATE) = '2026-01-01'
ORDER BY t.departureTime ASC

USE VeXeDB;
SELECT * FROM passenger_ticket_detail

SELECT * FROM customer




select pt.qrcode, ts.tripSeatId, t.tripId FROM trip_seat ts join trip t on ts.tripId = t.tripId
join passenger_ticket_detail pt
on ts.tripSeatId = pt.tripSeatId
JOIN passenger_ticket p on pt.passengerTicketId = p.passengerTicketId
JOIN CUSTOMER c on p.customerId = c.customerId
WHERE c.accountId = 193 AND cast(t.departureTime AS DATE) = '2026-01-11'




select pt.qrcode FROM trip_seat ts join trip t on ts.tripId = t.tripId
join passenger_ticket_detail pt
on ts.tripSeatId = pt.tripSeatId
JOIN passenger_ticket p on pt.passengerTicketId = p.passengerTicketId
JOIN CUSTOMER c on p.customerId = c.customerId
WHERE c.accountId = 193 AND cast(t.departureTime AS DATE) = '2026-01-11'
AND ts.tripSeatId = 15011 AND t.tripId = 501

SELECT * FROM trip_seat

SELECT * FROM trip

-- Booking-history rows used by CustomerTicketHistoryService.
-- Set @ticketCode to NULL for the full history or a code for one detail page.
DECLARE @historyAccountId INT = 193;
DECLARE @ticketCode VARCHAR(64) = NULL;

SELECT pt.passengerTicketId, ptd.ticketDetailId, pt.ticketCode,
       pt.status AS ticketStatus, pt.totalPrice, pt.pickupStopName,
       pt.dropoffStopName, pt.createdAt AS bookedAt, t.departureTime,
       r.routeName, ct.coachTypeName, pay.paymentMethod,
       pay.status AS paymentStatus, ptd.fullName, ptd.phone, ptd.email,
       ptd.seatCodeSnapshot AS seatCode, ptd.price AS seatPrice
FROM passenger_ticket_detail ptd
JOIN passenger_ticket pt ON pt.passengerTicketId = ptd.passengerTicketId
JOIN customer c ON c.customerId = pt.customerId
JOIN trip t ON t.tripId = pt.tripId
JOIN route r ON r.routeId = t.routeId
JOIN coach co ON co.coachId = t.coachId
JOIN coach_type ct ON ct.coachTypeId = co.coachTypeId
LEFT JOIN payment pay ON pay.passengerTicketId = pt.passengerTicketId
WHERE c.accountId = @historyAccountId
  AND (@ticketCode IS NULL OR pt.ticketCode = @ticketCode)
ORDER BY t.departureTime DESC, pt.passengerTicketId DESC, ptd.ticketDetailId;

-- Ownership-safe QR lookup corresponding to GET /customer/history/seats/{id}/qr.
DECLARE @ticketDetailId INT = 1;
SELECT ptd.qrcode
FROM passenger_ticket_detail ptd
JOIN passenger_ticket pt ON pt.passengerTicketId = ptd.passengerTicketId
JOIN customer c ON c.customerId = pt.customerId
WHERE ptd.ticketDetailId = @ticketDetailId
  AND c.accountId = @historyAccountId;

-- Inspect the cancellation/refund result without mutating DDL or fake data.
DECLARE @cancelledTicketCode VARCHAR(64) = 'REPLACE_WITH_TICKET_CODE';
SELECT pt.ticketCode, pt.status AS ticketStatus, p.refundAmount,
       r.amount AS requestedRefundAmount, r.refundMethod,
       r.status AS refundStatus, r.callbackData AS bankDestination
FROM passenger_ticket pt
JOIN payment p ON p.passengerTicketId = pt.passengerTicketId
LEFT JOIN refund r ON r.paymentId = p.paymentId
WHERE pt.ticketCode = @cancelledTicketCode;


-- Test authenticated cargo history without multiplying each item by every route stop.
-- The seller determines the agency; pickup/drop-off come directly from the order.
DECLARE @cargoHistoryAccountId INT = 2;
SELECT ct.ticketCode, ta.ticketAgencyName, co.licensePlate, driver.staffName,
       pickup.stopPointName AS pickupStopName, pickup.[address] AS pickupAddress, pickup.city AS pickupCity,
       dropoff.stopPointName AS dropoffStopName, dropoff.[address] AS dropoffAddress, dropoff.city AS dropoffCity,
       ct.feePayer, ct.senderName, ct.senderPhone, ct.receiverName, ct.receiverPhone,
       ctd.createdAt, ctd.dimensionVol, ctd.quantity, ctd.weightKg, ctd.[description]
FROM cargo_ticket_detail ctd
JOIN cargo_ticket ct ON ct.cargoTicketId = ctd.cargoTicketId
LEFT JOIN customer customer ON customer.customerId = ct.customerId
LEFT JOIN trip t ON t.tripId = ct.tripId
LEFT JOIN coach co ON co.coachId = t.coachId
LEFT JOIN staff driver ON driver.staffId = t.driverId AND driver.staffPosition = 'DRIVER'
JOIN staff seller ON seller.staffId = ct.soldBy
LEFT JOIN ticket_agency ta ON ta.ticketAgencyId = seller.ticketAgencyId
JOIN coach_stop pickup ON pickup.stopPointId = ct.pickupStopId
JOIN coach_stop dropoff ON dropoff.stopPointId = ct.dropoffStopId
WHERE customer.accountId = @cargoHistoryAccountId
ORDER BY ct.createdAt DESC, ctd.cargoTicketDetailId;


-- Detect impossible schedule continuity after auto-generation.
-- A resource cannot run the same route twice in a row because the first trip
-- ends at the opposite city; it must return on the opposite route first.
WITH resource_trips AS (
    SELECT 'DRIVER' AS resourceType, t.driverId AS resourceId, t.tripId,
           t.routeId, r.routeName, t.departureTime
    FROM trip t
    JOIN route r ON r.routeId = t.routeId
    WHERE t.driverId IS NOT NULL
      AND t.[status] NOT IN ('CANCELED', 'CANCELLED')
    UNION ALL
    SELECT 'ATTENDANT' AS resourceType, t.attendantId AS resourceId, t.tripId,
           t.routeId, r.routeName, t.departureTime
    FROM trip t
    JOIN route r ON r.routeId = t.routeId
    WHERE t.attendantId IS NOT NULL
      AND t.[status] NOT IN ('CANCELED', 'CANCELLED')
    UNION ALL
    SELECT 'COACH' AS resourceType, t.coachId AS resourceId, t.tripId,
           t.routeId, r.routeName, t.departureTime
    FROM trip t
    JOIN route r ON r.routeId = t.routeId
    WHERE t.coachId IS NOT NULL
      AND t.[status] NOT IN ('CANCELED', 'CANCELLED')
),
ordered_trips AS (
    SELECT *,
           LAG(tripId) OVER (PARTITION BY resourceType, resourceId ORDER BY departureTime) AS previousTripId,
           LAG(routeId) OVER (PARTITION BY resourceType, resourceId ORDER BY departureTime) AS previousRouteId,
           LAG(routeName) OVER (PARTITION BY resourceType, resourceId ORDER BY departureTime) AS previousRouteName,
           LAG(departureTime) OVER (PARTITION BY resourceType, resourceId ORDER BY departureTime) AS previousDepartureTime
    FROM resource_trips
)
SELECT resourceType, resourceId, previousTripId, previousRouteName, previousDepartureTime,
       tripId, routeName, departureTime
FROM ordered_trips
WHERE previousRouteId = routeId
ORDER BY resourceType, resourceId, departureTime;
