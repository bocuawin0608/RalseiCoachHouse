USE vexedb
SELECT a.username, r.roleName
FROM account a JOIN account_role ar ON a.accountId = ar.accountId JOIN role r on ar.roleId = r.roleId
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

-- My new view all available trip query
SELECT t.tripId,
    t.[status] AS "TRANG THAI CHUYEN DI",
    c.manufacturer,
    ct.coachTypeName,
    c.licensePlate,
    c.[status] AS "TINH TRANG XE",
    CAST(t.departureTime AS DATE) AS departureDate, 
    CAST(t.departureTime AS TIME) AS departureTime,
    (SELECT COUNT(*)
    FROM trip_seat ts
    WHERE ts.tripId = t.tripId AND ts.status = 'Available') AS availableSeats,
    (SELECT COUNT(*)
    FROM trip_seat ts
    WHERE ts.tripId = t.tripId) AS totalSeats
FROM trip t
    JOIN coach c ON c.coachId = t.coachId
    JOIN coach_type ct ON ct.coachTypeId = c.coachTypeId
    JOIN trip_seat ts ON ts.tripId = t.tripId
    LEFT JOIN coach_type_price ctp ON ctp.coachTypeId = ct.coachTypeId
    WHERE CAST(t.departureTime AS DATE) = '2026-06-02'
    ORDER BY t.departureTime ASC

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
WHERE t.coachId = 12                                  -- ID xe cần kiểm tra (:coachId)
  AND t.tripId <> 0                                  -- Nếu là tạo mới thì truyền 0, nếu là sửa chính chuyến đó thì truyền ID của chuyến (:currentTripId)
  AND t.status <> 'CANCELLED'                        -- Bỏ qua các chuyến đã hủy vì chúng không chiếm xe
  AND t.departureTime = '2026-01-02 08:00:00';        -- Thời gian dự định xuất phát (:departureTime)
GO

USE VeXeDB;
GO

-- Chạy lệnh UPDATE logic
UPDATE trip 
SET status = 'CANCELLED', 
    updatedAt = GETDATE(), 
    updatedBy = 1                                    -- ID của Manager thực hiện (:managerId)
WHERE tripId = 10;                                   -- ID chuyến xe cần hủy (:tripId)

-- Kiểm tra lại kết quả xem dòng đó đã đổi trạng thái chưa
SELECT tripId, routeId, coachId, departureTime, status, updatedAt, updatedBy 
FROM trip 
WHERE tripId = 10;
GO

-- Giả sử Manager số 2 thực hiện sinh lịch tự động cho 1 tuần từ ngày 01/06/2026
EXEC sp_AutoGenerateWeeklySchedule @StartDate = '2026-06-01';

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
SELECT @MaxDateInDb = MAX(CAST(departureTime AS DATE)) FROM [trip];

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