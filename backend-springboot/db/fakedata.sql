-- CRE: NIKO(Gemini Agent) - UPDATE: ĐỒNG BỘ THIẾT KẾ DDL MỚI
-- FIX NEW DDL: Đồng bộ các trường Snapshot (pickupStopName, dropoffStopName, seatCodeSnapshot) và Token QR Code.
-- FIX BIRTH YEAR: Đã xóa trường birthYear khỏi passenger_ticket_detail theo thiết kế tối giản.

USE VeXeDB;
GO

SET NOCOUNT ON;
PRINT N'=== BẮT ĐẦU RE-SEED MASTER: FIX THEO SCHEMA DDL MỚI ===';

-- ============================================================================
-- CLEANUP: XÓA DỮ LIỆU THEO ĐÚNG THỨ TỰ RÀN BUỘC KHÓA NGOẠI
-- ============================================================================
PRINT N'-> Đang dọn dẹp dữ liệu cũ...';
DELETE FROM [refund];
DELETE FROM [accompanied_child];
DELETE FROM [payment];
DELETE FROM [cargo_ticket_detail];
DELETE FROM [passenger_ticket_detail];
DELETE FROM [cargo_ticket];
DELETE FROM [passenger_ticket];
DELETE FROM [trip_seat]; 
DELETE FROM [trip];
DELETE FROM [seat];
DELETE FROM [coach];
DELETE FROM [cargo_type_price];
DELETE FROM [coach_type_price]; 
DELETE FROM [route_stop];
DELETE FROM [staff];
DELETE FROM [ticket_agency];
DELETE FROM [customer];
DELETE FROM [account_role];
DELETE FROM [cargo_type];
DELETE FROM [coach_type]; 
DELETE FROM [route];
DELETE FROM [coach_stop];
DELETE FROM [voucher];
DELETE FROM [role];
DELETE FROM [account];

-- RESET TOÀN BỘ CỘT IDENTITY MỘT CÁCH AN TOÀN
PRINT N'-> Đang reset bộ đếm Identity thông minh...';

DECLARE @TableName NVARCHAR(256);
DECLARE @SQL NVARCHAR(MAX);

DECLARE cur CURSOR FOR
SELECT QUOTENAME(s.name) + '.' + QUOTENAME(t.name)
FROM sys.tables t
JOIN sys.schemas s ON t.schema_id = s.schema_id
JOIN sys.identity_columns ic ON t.object_id = ic.object_id;

OPEN cur;
FETCH NEXT FROM cur INTO @TableName;

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @SQL = N'
    IF EXISTS (SELECT 1 FROM sys.identity_columns WHERE object_id = OBJECT_ID(''' + @TableName + ''') AND last_value IS NOT NULL)
    BEGIN
        DBCC CHECKIDENT (''' + @TableName + ''', RESEED, 0);
    END';
    
    EXEC sp_executesql @SQL;
    FETCH NEXT FROM cur INTO @TableName;
END;

CLOSE cur;
DEALLOCATE cur;

-- ============================================================================
-- LEVEL 1: STRONG ENTITIES
-- ============================================================================
PRINT N'-> Đang nạp danh mục cấu trúc nền tảng...';

INSERT INTO [role] (roleName) VALUES ('ADMIN'), ('MANAGER'), ('TICKET_STAFF'), ('TRIP_STAFF'), ('CUSTOMER');

INSERT INTO [voucher] (voucherCode, discountValue, startEffectiveDate, endEffectiveDate, discountType, maxDiscountValue, minOrderValue, usageLimit) VALUES 
('HE2026', 10.00, '2026-01-01', '2028-12-31', 'PERCENT', 50000.00, 200000.00, 1000), 
('GIAM50K', 50000.00, '2026-01-01', '2028-12-31', 'FIXED', 50000.00, 0.00, 1000);

INSERT INTO [coach_stop] (stopPointName, address, city, surcharge, isActive, latitude, longitude) VALUES
(N'Bến Xe Nước Ngầm', N'Số 1 Ngọc Hồi, Hoàng Mai', N'Hà Nội',0.00, 1,20.939917437, 105.844225125),
(N'Sảnh T1+T2 - Sân bay Nội Bài', N'Sảnh E, Nhà ga T1, Sóc Sơn', N'Hà Nội',100000.00, 1, 21.2149337, 105.8007099),
(N'Văn Phòng Đồng Hới', N'Trần Hưng Đạo, Đồng Hới', N'Quảng Bình',0.00, 1,17.4691879169492, 106.61043838942),
(N'Trạm Dừng Lệ Thủy', N'Quốc Lộ 1A, Lệ Thủy', N'Quảng Bình',0.00, 1,17.2425945873282, 106.814788476285),
(N'Trạm Dừng Ba Đồn', N'Phường Ba Đồn, Thị xã Ba Đồn', N'Quảng Bình',0.00, 1,17.7546377497501, 106.42331210725);

INSERT INTO [route] (routeName, totalKilometers, totalMinutes) VALUES 
(N'Hà Nội - Quảng Bình', 500.00, 600), 
(N'Quảng Bình - Hà Nội', 500.00, 600); 

INSERT INTO [coach_type] (coachTypeName, totalSeat) VALUES 
(N'Xe Limousine VIP 20 phòng', 20),      
(N'Xe Giường Nằm Luxury 32 chỗ', 32),    
(N'Xe Khách Truyền Thống 38 chỗ', 38);   

INSERT INTO [cargo_type] (cargoTypeName) VALUES 
(N'Hàng khô / Thùng Carton'), (N'Xe máy / Xe điện'), (N'Hàng dễ vỡ');

-- ============================================================================
-- LEVEL 2: PERSONNEL & TICKET AGENCY GENERATION
-- ============================================================================
PRINT N'-> Đang cấu hình mạng lưới 22 Đại lý/Phòng vé chiến lược...';

INSERT INTO [ticket_agency] (stopPointId, ticketAgencyName) VALUES 
(1, N'Đại lý Bến Xe Nước Ngầm'),
(2, N'Đại lý Sân Bay Nội Bài'),
(3, N'Đại lý Văn Phòng Đồng Hới'),
(4, N'Đại lý Lệ Thủy'),
(5, N'Đại lý Ba Đồn');

DECLARE @AgencyTable TABLE (RowIdx INT IDENTITY(1,1), AgencyId INT);
INSERT INTO @AgencyTable (AgencyId) SELECT ticketAgencyId FROM [ticket_agency];
DECLARE @TotalAgencies INT = (SELECT COUNT(*) FROM @AgencyTable);

PRINT N'-> Đang khởi tạo tài khoản hệ thống và đồng bộ hồ sơ nhân sự...';

DECLARE @IdOutput TABLE (Id INT);
DECLARE @GeneratedId INT;
DECLARE @idx INT = 1;
DECLARE @phoneStr VARCHAR(20);
DECLARE @dobStr DATE;
DECLARE @cccdStr VARCHAR(20);
DECLARE @emailStr VARCHAR(100);

-- Admin Hệ Thống
INSERT INTO [account] (username, passwordHash, isActive) OUTPUT inserted.accountId INTO @IdOutput VALUES ('0901111111', '$2a$10$G1TCgI4zgHQpN1hyuRMEaOOvGeoSg7MCMQDapcuLl0NsIZNn104w2', 1);
SELECT TOP 1 @GeneratedId = Id FROM @IdOutput; DELETE FROM @IdOutput;
INSERT INTO [account_role] (accountId, roleId) VALUES (@GeneratedId, 1);
INSERT INTO [staff] (accountId, ticketAgencyId, staffName, phone, email, dob, cccd, staffPosition, hireDate) 
VALUES (@GeneratedId, NULL, N'Hệ Thống Admin', '0901111111', 'admin.root@vexedb.vn', '1990-05-15', '030090000001', 'MANAGER', '2024-01-01');

-- Manager Điều Hành
INSERT INTO [account] (username, passwordHash, isActive) OUTPUT inserted.accountId INTO @IdOutput VALUES ('0902222222', '$2a$10$G1TCgI4zgHQpN1hyuRMEaOOvGeoSg7MCMQDapcuLl0NsIZNn104w2', 1);
SELECT TOP 1 @GeneratedId = Id FROM @IdOutput; DELETE FROM @IdOutput;
INSERT INTO [account_role] (accountId, roleId) VALUES (@GeneratedId, 2);
INSERT INTO [staff] (accountId, ticketAgencyId, staffName, phone, email, dob, cccd, staffPosition, hireDate) 
VALUES (@GeneratedId, 1, N'Quản Lý Trưởng', '0902222222', 'quanlytruong@vexedb.vn', '1985-10-20', '030085000002', 'MANAGER', '2024-01-01');

-- Sinh dữ liệu cho 30 Ticket Staff
SET @idx = 1;
WHILE @idx <= 30
BEGIN
    SET @phoneStr = '0931000' + RIGHT('00' + CAST(@idx AS VARCHAR(2)), 2);
    SET @dobStr = CAST((1990 + (@idx % 12)) AS VARCHAR(4)) + '-03-' + RIGHT('0' + CAST((10 + (@idx % 18)) AS VARCHAR(2)), 2);
    SET @cccdStr = '03009' + CAST((1 + (@idx % 9)) AS VARCHAR(1)) + '000' + RIGHT('00' + CAST(@idx AS VARCHAR(2)), 2);
    SET @emailStr = 'ticketstaff' + CAST(@idx AS VARCHAR(2)) + '@vexedb.vn';

    INSERT INTO [account] (username, passwordHash, isActive) OUTPUT inserted.accountId INTO @IdOutput VALUES (@phoneStr, '$2a$10$G1TCgI4zgHQpN1hyuRMEaOOvGeoSg7MCMQDapcuLl0NsIZNn104w2', 1);
    SELECT TOP 1 @GeneratedId = Id FROM @IdOutput; DELETE FROM @IdOutput;
    
    DECLARE @TargetAgencyId INT;
    SELECT @TargetAgencyId = AgencyId FROM @AgencyTable WHERE RowIdx = ((@idx % @TotalAgencies) + 1);

    INSERT INTO [account_role] (accountId, roleId) VALUES (@GeneratedId, 3);
    INSERT INTO [staff] (accountId, ticketAgencyId, staffName, phone, email, dob, cccd, staffPosition, hireDate) 
    VALUES (@GeneratedId, @TargetAgencyId, N'NV Bán Vé ' + CAST(@idx AS NVARCHAR(5)), @phoneStr, @emailStr, @dobStr, @cccdStr, 'TICKET_STAFF', '2025-01-01');
    SET @idx = @idx + 1;
END;

-- Sinh dữ liệu chuẩn hóa cho 80 Tài xế (Driver)
SET @idx = 1;
WHILE @idx <= 80
BEGIN
    SET @phoneStr = '0932000' + RIGHT('00' + CAST(@idx AS VARCHAR(2)), 2);
    SET @dobStr = CAST((1975 + (@idx % 20)) AS VARCHAR(4)) + '-07-' + RIGHT('0' + CAST((1 + (@idx % 28)) AS VARCHAR(2)), 2);
    SET @cccdStr = '03007' + CAST((1 + (@idx % 8)) AS VARCHAR(1)) + '000' + RIGHT('000' + CAST(@idx AS VARCHAR(3)), 3);
    SET @emailStr = 'driver.tx' + CAST(@idx AS VARCHAR(2)) + '@vexedb.vn';

    INSERT INTO [account] (username, passwordHash, isActive) OUTPUT inserted.accountId INTO @IdOutput VALUES (@phoneStr, '$2a$10$G1TCgI4zgHQpN1hyuRMEaOOvGeoSg7MCMQDapcuLl0NsIZNn104w2', 1);
    SELECT TOP 1 @GeneratedId = Id FROM @IdOutput; DELETE FROM @IdOutput;

    INSERT INTO [account_role] (accountId, roleId) VALUES (@GeneratedId, 4);
    INSERT INTO [staff] (accountId, ticketAgencyId, staffName, phone, email, dob, cccd, staffPosition, hireDate) 
    VALUES (@GeneratedId, NULL, N'Tài Xế Chuyên Nghiệp ' + CAST(@idx AS NVARCHAR(5)), @phoneStr, @emailStr, @dobStr, @cccdStr, 'DRIVER', '2025-01-01');
    SET @idx = @idx + 1;
END;

-- Sinh dữ liệu chuẩn hóa cho 80 Phụ xe (Attendant)
SET @idx = 1;
WHILE @idx <= 80
BEGIN
    SET @phoneStr = '0933000' + RIGHT('00' + CAST(@idx AS VARCHAR(2)), 2);
    SET @dobStr = CAST((1985 + (@idx % 20)) AS VARCHAR(4)) + '-09-' + RIGHT('0' + CAST((1 + (@idx % 28)) AS VARCHAR(2)), 2);
    SET @cccdStr = '03008' + CAST((1 + (@idx % 8)) AS VARCHAR(1)) + '000' + RIGHT('000' + CAST(@idx AS VARCHAR(3)), 3);
    SET @emailStr = 'attendant.px' + CAST(@idx AS VARCHAR(2)) + '@vexedb.vn';

    INSERT INTO [account] (username, passwordHash, isActive) OUTPUT inserted.accountId INTO @IdOutput VALUES (@phoneStr, '$2a$10$G1TCgI4zgHQpN1hyuRMEaOOvGeoSg7MCMQDapcuLl0NsIZNn104w2', 1);
    SELECT TOP 1 @GeneratedId = Id FROM @IdOutput; DELETE FROM @IdOutput;

    INSERT INTO [account_role] (accountId, roleId) VALUES (@GeneratedId, 4);
    INSERT INTO [staff] (accountId, ticketAgencyId, staffName, phone, email, dob, cccd, staffPosition, hireDate) 
    VALUES (@GeneratedId, NULL, N'Phụ Xe Tuyến Đường ' + CAST(@idx AS NVARCHAR(5)), @phoneStr, @emailStr, @dobStr, @cccdStr, 'ATTENDANT', '2025-01-01');
    SET @idx = @idx + 1;
END;

-- Khách hàng hệ thống (Bảng customer giữ nguyên dob theo dạng DATE cho CRM)
SET @idx = 1;
WHILE @idx <= 100
BEGIN
    SET @phoneStr = '0960000' + RIGHT('000' + CAST(@idx AS VARCHAR(3)), 3);
    SET @dobStr = CAST((1980 + (@idx % 25)) AS VARCHAR(4)) + '-11-' + RIGHT('0' + CAST((1 + (@idx % 25)) AS VARCHAR(2)), 2);

    INSERT INTO [account] (username, passwordHash, firebaseUid, isActive) OUTPUT inserted.accountId INTO @IdOutput VALUES (@phoneStr, '$2a$10$G1TCgI4zgHQpN1hyuRMEaOOvGeoSg7MCMQDapcuLl0NsIZNn104w2', CAST(NEWID() AS VARCHAR(128)), 1);
    SELECT TOP 1 @GeneratedId = Id FROM @IdOutput; DELETE FROM @IdOutput;

    INSERT INTO [account_role] (accountId, roleId) VALUES (@GeneratedId, 5);
    INSERT INTO [customer] (accountId, customerName, phone, email, dob) 
    VALUES (@GeneratedId, N'Thành Viên App ' + CAST(@idx AS NVARCHAR(5)), @phoneStr, 'user' + CAST(@idx AS VARCHAR(3)) + '@gmail.com', @dobStr);
    SET @idx = @idx + 1;
END;

-- ============================================================================
-- ADMIN TEST DATA (Manage Customers, Ticket Agencies, Staff)
-- ============================================================================
PRINT N'-> Them du lieu test cho admin module...';

-- Customers khong co account (test CRUD truc tiep)
INSERT INTO [customer] (customerName, phone, email, dob, isActive)
VALUES (N'Nguyen Van A', '0911111111', 'nguyenvana@gmail.com', '1990-01-15', 1),
       (N'Tran Thi B', '0922222222', 'tranthib@gmail.com', '1995-06-20', 1),
       (N'Le Van C', '0933333333', 'levanc@gmail.com', '1988-12-05', 1),
       (N'Pham Thi D', '0944444444', 'phamthid@gmail.com', '2000-03-10', 1),
       (N'Hoang Van E', '0955555555', 'hoangvane@gmail.com', '1975-09-25', 0);

-- Coach stop o Tp.HCM cho ticket agency test
INSERT INTO [coach_stop] (stopPointName, address, city, surcharge, isActive, latitude, longitude)
VALUES (N'Ben Xe Mien Dong', N'292 Dinh Bo Linh, Binh Thanh', N'TP. Ho Chi Minh', 0.00, 1, 10.8231, 106.6947),
       (N'Ben Xe Mien Tay', N'395 Kinh Duong Vuong, Binh Tan', N'TP. Ho Chi Minh', 0.00, 1, 10.7553, 106.6258);

-- Them ticket agency o Tp.HCM
INSERT INTO [ticket_agency] (stopPointId, ticketAgencyName)
VALUES (6, N'Dai ly Ben Xe Mien Dong'),
       (7, N'Dai ly Ben Xe Mien Tay');

-- Mot staff da bi vo hieu hoa (test filter)
INSERT INTO [account] (username, passwordHash, isActive) OUTPUT inserted.accountId INTO @IdOutput
VALUES ('0999999999', '$2a$10$G1TCgI4zgHQpN1hyuRMEaOOvGeoSg7MCMQDapcuLl0NsIZNn104w2', 1);
SELECT TOP 1 @GeneratedId = Id FROM @IdOutput; DELETE FROM @IdOutput;
INSERT INTO [account_role] (accountId, roleId) VALUES (@GeneratedId, 4);
INSERT INTO [staff] (accountId, ticketAgencyId, staffName, phone, email, dob, cccd, staffPosition, hireDate, isActive)
VALUES (@GeneratedId, 6, N'Tai Xe Nghi Viec', '0999999999', 'taixenghiviec@vexedb.vn', '1992-07-15', '030092000099', 'DRIVER', '2025-06-01', 0);

PRINT N'-> Hoan tat du lieu test admin!';

-- ============================================================================
-- LEVEL 2.5: CONFIGURING ROUTE STOPS & PRICES
-- ============================================================================
INSERT INTO [route_stop] (routeId, stopPointId, stopOrder, kilometersFromStart, minutesFromStart) VALUES 
(1, 1, 1, 0.00, 0), (1, 2, 2, 90.00, 120), (1, 3, 3, 290.00, 360), (1, 4, 4, 500.00, 600),
(2, 4, 1, 0.00, 0), (2, 3, 2, 210.00, 240), (2, 2, 3, 410.00, 480), (2, 1, 4, 500.00, 600);

INSERT INTO [coach_type_price] (coachTypeId, seatPrice, startEffectiveDate, endEffectiveDate) VALUES 
(1, 590000.00, '2026-01-01', '2029-12-31'), 
(2, 450000.00, '2026-01-01', '2029-12-31'), 
(3, 350000.00, '2026-01-01', '2029-12-31'); 

INSERT INTO [cargo_type_price] (cargoTypeId, unit, pricePerUnit, startEffectiveDate, endEffectiveDate) VALUES 
(1, N'Thùng', 60000.00, '2026-01-01', '2029-12-31'), 
(2, N'Chiếc', 400000.00, '2026-01-01', '2029-12-31'), 
(3, N'Kiện', 150000.00, '2026-01-01', '2029-12-31');

-- ============================================================================
-- LEVEL 3: GENERATING COACHES & SEATS (FIXED: MATCH SEAT LAYOUT MATRIX)
-- ============================================================================
PRINT N'-> Đang lắp ráp hạ tầng 365 xe khách và đồng bộ tọa độ Ghế theo cấu trúc JSON...';

DECLARE @ManufacturerTable TABLE (Id INT IDENTITY(1,1), Brand NVARCHAR(100));
INSERT INTO @ManufacturerTable (Brand) VALUES 
(N'Thaco Mobihome'), (N'Kia Granbird Silk Road'), (N'Hyundai Universe Express'), (N'Volvo B11R Premium'),
(N'Scania Marcopolo'), (N'Tracomeco Universe'), (N'Samco Primas ISUZU'), (N'Daewoo BX212');
DECLARE @TotalBrands INT = (SELECT COUNT(*) FROM @ManufacturerTable);

DECLARE @c INT = 1;
DECLARE @generatedPlate VARCHAR(20);
DECLARE @coachStatus VARCHAR(20);
DECLARE @pickedBrand NVARCHAR(100);
DECLARE @TargetCoachTypeId INT;
DECLARE @NewCoachId INT;

-- Khai báo các biến chạy ma trận tọa độ
DECLARE @f INT, @r INT, @c_idx INT, @seatCount INT;

WHILE @c <= 365
BEGIN
    SET @generatedPlate = CASE WHEN @c % 3 = 0 THEN '29B-' WHEN @c % 3 = 1 THEN '30B-' ELSE '73B-' END + 
                          LEFT(CAST((@c * 137 + 10000) AS VARCHAR(10)), 3) + '.' + 
                          RIGHT(CAST((@c * 79 + 10) AS VARCHAR(10)), 2);
    SET @coachStatus = CASE WHEN @c <= 320 THEN 'ACTIVE' ELSE 'MAINTENANCE' END;
    SELECT @pickedBrand = Brand FROM @ManufacturerTable WHERE Id = ((@c % @TotalBrands) + 1);
    
    SET @TargetCoachTypeId = CASE WHEN @c % 3 = 0 THEN 1 WHEN @c % 3 = 1 THEN 2 ELSE 3 END;

    INSERT INTO [coach] (coachTypeId, licensePlate, [status], manufacturer, [year]) 
    VALUES (@TargetCoachTypeId, @generatedPlate, @coachStatus, @pickedBrand, 2024);
    
    SET @NewCoachId = SCOPE_IDENTITY();
    SET @seatCount = 1;

    -- ==========================================
    -- LOẠI 1: Xe Limousine VIP 20 phòng (2 Tầng x 5 Hàng x 2 Cột)
    -- ==========================================
    IF @TargetCoachTypeId = 1
    BEGIN
        SET @f = 1;
        WHILE @f <= 2
        BEGIN
            SET @r = 1;
            WHILE @r <= 5
            BEGIN
                SET @c_idx = 1;
                WHILE @c_idx <= 2
                BEGIN
                    INSERT INTO [seat] (coachId, seatCode, rowIndex, colIndex, floorIndex) 
                    VALUES (@NewCoachId, 'L' + RIGHT('0' + CAST(@seatCount AS VARCHAR(2)), 2), @r, @c_idx, @f); 
                    
                    SET @seatCount = @seatCount + 1; 
                    SET @c_idx = @c_idx + 1; 
                END;
                SET @r = @r + 1;
            END;
            SET @f = @f + 1;
        END;
    END
    
    -- ==========================================
    -- LOẠI 2: Xe Giường Nằm Luxury 32 chỗ (2 Tầng x 6 Hàng x 3 Cột)
    -- Hàng 1-5 full 3 cột. Hàng 6 chỉ có 1 ghế ở cột 2 (Cột 1, 3 là EMPTY)
    -- ==========================================
    ELSE IF @TargetCoachTypeId = 2
    BEGIN
        SET @f = 1;
        WHILE @f <= 2
        BEGIN
            SET @r = 1;
            WHILE @r <= 6
            BEGIN
                SET @c_idx = 1;
                WHILE @c_idx <= 3
                BEGIN
                    -- Kiểm tra điều kiện vị trí có ghế thực tế
                    IF (@r <= 5) OR (@r = 6 AND @c_idx = 2)
                    BEGIN
                        INSERT INTO [seat] (coachId, seatCode, rowIndex, colIndex, floorIndex) 
                        VALUES (@NewCoachId, 'LX' + RIGHT('0' + CAST(@seatCount AS VARCHAR(2)), 2), @r, @c_idx, @f); 
                        
                        SET @seatCount = @seatCount + 1;
                    END;
                    SET @c_idx = @c_idx + 1;
                END;
                SET @r = @r + 1;
            END;
            SET @f = @f + 1;
        END;
    END
    
    -- ==========================================
    -- LOẠI 3: Xe Khách Truyền Thống 38 chỗ (2 Tầng x 7 Hàng x 3 Cột)
    -- Hàng 1-6 full 3 cột. Hàng 7 chỉ có 1 ghế ở cột 2 (Cột 1, 3 là EMPTY)
    -- ==========================================
    ELSE
    BEGIN
        SET @f = 1;
        WHILE @f <= 2
        BEGIN
            SET @r = 1;
            WHILE @r <= 7
            BEGIN
                SET @c_idx = 1;
                WHILE @c_idx <= 3
                BEGIN
                    -- Kiểm tra điều kiện vị trí có ghế thực tế
                    IF (@r <= 6) OR (@r = 7 AND @c_idx = 2)
                    BEGIN
                        INSERT INTO [seat] (coachId, seatCode, rowIndex, colIndex, floorIndex) 
                        VALUES (@NewCoachId, 'T' + RIGHT('0' + CAST(@seatCount AS VARCHAR(2)), 2), @r, @c_idx, @f); 
                        
                        SET @seatCount = @seatCount + 1;
                    END;
                    SET @c_idx = @c_idx + 1;
                END;
                SET @r = @r + 1;
            END;
            SET @f = @f + 1;
        END;
    END

    SET @c = @c + 1;
END;

-- Mẫu lịch sử trạng thái xe (xe đang bảo trì)
INSERT INTO [coach_status_log] (coachId, fromStatus, toStatus, reason, expectedEndAt)
SELECT c.coachId, 'ACTIVE', 'MAINTENANCE', N'Bảo dưỡng định kỳ hệ thống phanh', DATEADD(day, 7, GETDATE())
FROM [coach] c
WHERE c.[status] = 'MAINTENANCE'
  AND c.coachId IN (SELECT TOP 5 coachId FROM [coach] WHERE [status] = 'MAINTENANCE' ORDER BY coachId);

-- ============================================================================
-- LEVEL 4: OPERATIONAL ENTITIES - TRIPS & TRIP_SEATS
-- ============================================================================
PRINT N'-> Đang chạy thuật toán ca trực và tự động đổ dữ liệu snapshot trạng thái ghế chuyến...';

DECLARE @DriverTable TABLE (RowIdx INT IDENTITY(1,1), StaffId INT);
DECLARE @AttendantTable TABLE (RowIdx INT IDENTITY(1,1), StaffId INT);
DECLARE @ActiveCoachTable TABLE (RowIdx INT IDENTITY(1,1), CoachId INT);

INSERT INTO @DriverTable (StaffId) SELECT staffId FROM [staff] WHERE staffPosition = 'DRIVER' ORDER BY staffId ASC;
INSERT INTO @AttendantTable (StaffId) SELECT staffId FROM [staff] WHERE staffPosition = 'ATTENDANT' ORDER BY staffId ASC;
INSERT INTO @ActiveCoachTable (CoachId) SELECT coachId FROM [coach] WHERE [status] = 'ACTIVE' ORDER BY coachId ASC;

DECLARE @TotalDrivers INT = (SELECT COUNT(*) FROM @DriverTable);
DECLARE @TotalAttendants INT = (SELECT COUNT(*) FROM @AttendantTable);
DECLARE @TotalActiveCoaches INT = (SELECT COUNT(*) FROM @ActiveCoachTable);

DECLARE @StartDate DATETIME = '2026-01-01';
DECLARE @EndDate DATETIME = '2026-01-15'; 
DECLARE @CurrentDate DATETIME = @StartDate;
DECLARE @TripCounter INT = 0;
DECLARE @NewTripId INT;

WHILE @CurrentDate <= @EndDate
BEGIN
    -- Chiều đi: Hà Nội -> Quảng Bình
    DECLARE @Slot1 INT = 0;
    WHILE @Slot1 < 24
    BEGIN
        DECLARE @DrvId1 INT, @AtnId1 INT, @CchId1 INT;
        
        IF (@TripCounter % 2 = 0)
        BEGIN
            SELECT @DrvId1 = StaffId FROM @DriverTable WHERE RowIdx = ((@TripCounter % @TotalDrivers) + 1);
            SELECT @AtnId1 = StaffId FROM @AttendantTable WHERE RowIdx = (((@TripCounter + 2) % @TotalAttendants) + 1);
        END
        ELSE
        BEGIN
            SELECT @DrvId1 = StaffId FROM @DriverTable WHERE RowIdx = (((@TripCounter + 5) % @TotalDrivers) + 1);
            SELECT @AtnId1 = StaffId FROM @AttendantTable WHERE RowIdx = ((@TripCounter % @TotalAttendants) + 1);
        END
        
        SELECT @CchId1 = CoachId FROM @ActiveCoachTable WHERE RowIdx = ((@TripCounter % @TotalActiveCoaches) + 1);
        DECLARE @Time1 DATETIME = DATEADD(hour, @Slot1, DATEADD(dd, DATEDIFF(dd, 0, @CurrentDate), 0));

        INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
        VALUES (1, @CchId1, @Time1, 'SCHEDULED', @DrvId1, @AtnId1);
        
        SET @NewTripId = SCOPE_IDENTITY();

        INSERT INTO [trip_seat] (tripId, seatId, price, [status])
        SELECT @NewTripId, s.seatId, ctp.seatPrice, 'AVAILABLE'
        FROM [seat] s
        JOIN [coach] c ON s.coachId = c.coachId
        JOIN [coach_type_price] ctp ON c.coachTypeId = ctp.coachTypeId
        WHERE c.coachId = @CchId1
          AND @Time1 BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate;

        SET @TripCounter = @TripCounter + 1;
        SET @Slot1 = @Slot1 + 1;
    END;

    -- Chiều về: Quảng Bình -> Hà Nội
    DECLARE @Slot2 INT = 0;
    WHILE @Slot2 < 24
    BEGIN
        DECLARE @DrvId2 INT, @AtnId2 INT, @CchId2 INT;
        
        IF (@TripCounter % 2 = 0)
        BEGIN
            SELECT @DrvId2 = StaffId FROM @DriverTable WHERE RowIdx = ((@TripCounter % @TotalDrivers) + 1);
            SELECT @AtnId2 = StaffId FROM @AttendantTable WHERE RowIdx = (((@TripCounter + 3) % @TotalAttendants) + 1);
        END
        ELSE
        BEGIN
            SELECT @DrvId2 = StaffId FROM @DriverTable WHERE RowIdx = (((@TripCounter + 7) % @TotalDrivers) + 1);
            SELECT @AtnId2 = StaffId FROM @AttendantTable WHERE RowIdx = ((@TripCounter % @TotalAttendants) + 1);
        END
        
        SELECT @CchId2 = CoachId FROM @ActiveCoachTable WHERE RowIdx = ((@TripCounter % @TotalActiveCoaches) + 1);
        DECLARE @Time2 DATETIME = DATEADD(minute, (@Slot2 * 60) + 30, DATEADD(dd, DATEDIFF(dd, 0, @CurrentDate), 0));

        INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId)
        VALUES (2, @CchId2, @Time2, 'SCHEDULED', @DrvId2, @AtnId2);
        
        SET @NewTripId = SCOPE_IDENTITY();

        INSERT INTO [trip_seat] (tripId, seatId, price, [status])
        SELECT @NewTripId, s.seatId, ctp.seatPrice, 'AVAILABLE'
        FROM [seat] s
        JOIN [coach] c ON s.coachId = c.coachId
        JOIN [coach_type_price] ctp ON c.coachTypeId = ctp.coachTypeId
        WHERE c.coachId = @CchId2
          AND @Time2 BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate;

        SET @TripCounter = @TripCounter + 1;
        SET @Slot2 = @Slot2 + 1;
    END;

    SET @CurrentDate = DATEADD(day, 1, @CurrentDate);
END;

PRINT N' -> Hoàn thành sinh ca trực & cấu hình Ghế Chuyến. Tổng số chuyến xe: ' + CAST(@TripCounter AS VARCHAR(10));

-- ============================================================================
-- LEVEL 5.1: PASSENGER TRANSACTIONAL GENERATION (🌟 ĐÃ XÓA TRƯỜNG BIRTH YEAR 🌟)
-- ============================================================================
PRINT N'-> Đang phân bổ ngẫu nhiên vé hành khách mẫu và đồng bộ hóa sang Trip_Seat...';

DECLARE @TicketIdx INT = 1;
DECLARE @MinNewTripId INT = (SELECT MIN(tripId) FROM [trip]);
DECLARE @MaxNewTripId INT = (SELECT MAX(tripId) FROM [trip]);
DECLARE @MinCusId INT = (SELECT MIN(customerId) FROM [customer]);
DECLARE @MaxCusId INT = (SELECT MAX(customerId) FROM [customer]);
DECLARE @TicketStaffId INT = (SELECT MIN(staffId) FROM [staff] WHERE staffPosition = 'TICKET_STAFF');

-- Hoist các biến xử lý logic ra đầu khối để chạy tối ưu hiệu năng
DECLARE @TargetTripId INT;
DECLARE @TargetCoachTypeId2 INT;
DECLARE @CalculatedTicketPrice DECIMAL(15,2);
DECLARE @TargetTripSeatId INT;
DECLARE @TargetCusId INT;
DECLARE @TicketStatus VARCHAR(20);
DECLARE @SeatStatusUpdate VARCHAR(20);
DECLARE @SeatCodeSnapshot VARCHAR(10);
DECLARE @NewPId INT;

-- Lấy sẵn Tên trạm tương ứng ID 1 và 4 để đưa vào làm Snapshot
DECLARE @PickupStopNameSnap NVARCHAR(255);
DECLARE @DropoffStopNameSnap NVARCHAR(255);
SELECT @PickupStopNameSnap = stopPointName FROM [coach_stop] WHERE stopPointId = 1;
SELECT @DropoffStopNameSnap = stopPointName FROM [coach_stop] WHERE stopPointId = 4;

WHILE @TicketIdx <= 500
BEGIN
    SET @TargetTripId = @MinNewTripId + (@TicketIdx % (@MaxNewTripId - @MinNewTripId + 1));
    
    SELECT @TargetCoachTypeId2 = c.coachTypeId FROM [trip] t JOIN [coach] c ON t.coachId = c.coachId WHERE t.tripId = @TargetTripId;
    SELECT @CalculatedTicketPrice = seatPrice FROM [coach_type_price] WHERE coachTypeId = @TargetCoachTypeId2;

    SET @TargetCusId = @MinCusId + (@TicketIdx % (@MaxCusId - @MinCusId + 1));
    SET @TicketStatus = CASE WHEN @TicketIdx % 7 = 0 THEN 'PENDING' ELSE 'CONFIRMED' END;
    SET @SeatStatusUpdate = CASE WHEN @TicketStatus = 'PENDING' THEN 'LOCKED' ELSE 'SOLD' END;

    -- Lọc chuẩn: Chỉ lấy những ghế thực sự CÒN TRỐNG để tránh duplicate logic đặt trùng 1 ghế
    SET @TargetTripSeatId = NULL;
    SELECT TOP 1 @TargetTripSeatId = tripSeatId 
    FROM [trip_seat] 
    WHERE tripId = @TargetTripId AND [status] = 'AVAILABLE'
    ORDER BY seatId;

    IF @TargetTripSeatId IS NOT NULL
    BEGIN
        -- Cập nhật đồng bộ sang trạng thái Ghế Chuyến (TripSeat)
        UPDATE [trip_seat] SET [status] = @SeatStatusUpdate WHERE tripSeatId = @TargetTripSeatId;

        -- Tìm chính xác seatCode vật lý để đưa vào làm Snapshot
        SELECT @SeatCodeSnapshot = s.seatCode 
        FROM [trip_seat] ts
        JOIN [seat] s ON ts.seatId = s.seatId
        WHERE ts.tripSeatId = @TargetTripSeatId;

        -- Bổ sung 3 cột snapshot: [pickupStopName], [dropoffStopName], [voucherCodeSnapshot]
        INSERT INTO [passenger_ticket] (
            customerId, tripId, voucherId, soldBy, ticketCode, totalPrice, 
            pickupStopId, dropoffStopId, pickupStopName, dropoffStopName, voucherCodeSnapshot, [status]
        )
        VALUES (
            @TargetCusId, @TargetTripId, NULL, 
            CASE WHEN @TicketIdx % 3 = 0 THEN @TicketStaffId ELSE NULL END, 
            'TK_SYS_' + RIGHT('0000' + CAST(@TicketIdx AS VARCHAR(4)), 4), 
            @CalculatedTicketPrice, 1, 4, @PickupStopNameSnap, @DropoffStopNameSnap, NULL, @TicketStatus
        );

        SET @NewPId = SCOPE_IDENTITY();

        -- 🌟 FIX: Đã bóc tách hoàn toàn trường [birthYear] khỏi bảng detail theo yêu cầu thiết kế mới
        INSERT INTO [passenger_ticket_detail] (passengerTicketId, tripSeatId, seatCodeSnapshot, qrcode, fullName, phone, price, [status])
        VALUES (
            @NewPId, @TargetTripSeatId, @SeatCodeSnapshot, 
            'TOKEN_QR_SECURE_' + CAST(NEWID() AS VARCHAR(36)), 
            N'Hành Khách Ghế ' + CAST(@TicketIdx AS NVARCHAR(5)), 
            '0985' + RIGHT('00000' + CAST(@TicketIdx AS VARCHAR(5)), 5), 
            @CalculatedTicketPrice, @TicketStatus
        );

        INSERT INTO [payment] (passengerTicketId, cargoTicketId, amount, paymentMethod, transactionId, [status], paymentTime)
        VALUES (@NewPId, NULL, @CalculatedTicketPrice, 'SEPAY', 'TXN_P_' + CAST(@TicketIdx AS VARCHAR(5)), CASE WHEN @TicketStatus = 'CONFIRMED' THEN 'COMPLETED' ELSE 'PENDING' END, CASE WHEN @TicketStatus = 'CONFIRMED' THEN GETDATE() ELSE NULL END);
    END
    
    SET @TicketIdx = @TicketIdx + 1;
END;

-- ============================================================================
-- LEVEL 5.2: CARGO TRANSACTIONAL GENERATION
-- ============================================================================
PRINT N'-> Đang khởi tạo 300 hóa đơn ký gửi bưu kiện mẫu (Lấp đầy Cargo Ticket)...';

DECLARE @CargoIdx INT = 1;
DECLARE @CalculatedCargoPrice DECIMAL(15,2);
DECLARE @PickCargoTypePriceId INT;
DECLARE @PricePerUnit DECIMAL(15,2);

WHILE @CargoIdx <= 300
BEGIN
    SET @TargetTripId = @MinNewTripId + (@CargoIdx % (@MaxNewTripId - @MinNewTripId + 1));
    SET @TargetCusId = @MinCusId + (@CargoIdx % (@MaxCusId - @MinCusId + 1));
    
    SET @PickCargoTypePriceId = (@CargoIdx % 3) + 1;
    SELECT @PricePerUnit = pricePerUnit FROM [cargo_type_price] WHERE cargoTypePriceId = @PickCargoTypePriceId;
    
    DECLARE @Quantity INT = (@CargoIdx % 4) + 1;
    SET @CalculatedCargoPrice = @PricePerUnit * @Quantity;

    INSERT INTO [cargo_ticket] (
        tripId, customerId, senderName, senderPhone, 
        receiverName, receiverPhone, ticketCode, totalPrice, 
        feePayer, codAmount, pickupStopId, dropoffStopId, [status], soldBy
    )
    VALUES (
        @TargetTripId, @TargetCusId, 
        N'Người Gửi Số ' + CAST(@CargoIdx AS NVARCHAR(5)), '0912' + RIGHT('00000' + CAST( @CargoIdx AS VARCHAR(5)), 5),
        N'Người Nhận Số ' + CAST(@CargoIdx AS NVARCHAR(5)), '0978' + RIGHT('00000' + CAST(@CargoIdx AS VARCHAR(5)), 5),
        'CG_CODE_' + RIGHT('0000' + CAST(@CargoIdx AS VARCHAR(4)), 4), @CalculatedCargoPrice,
        CASE WHEN @CargoIdx % 2 = 0 THEN 'SENDER' ELSE 'RECEIVER' END,
        CASE WHEN @CargoIdx % 5 = 0 THEN 200000.00 ELSE 0.00 END,
        1, 4, 'RECEIVED', @TicketStaffId
    );

    DECLARE @NewCargoId INT = SCOPE_IDENTITY();

    INSERT INTO [cargo_ticket_detail] (cargoTicketId, cargoTypePriceId, description, quantity, weightKg, dimensionVol, calculatedPrice)
    VALUES (@NewCargoId, @PickCargoTypePriceId, N'Kiện bưu phẩm ký gửi mẫu số ' + CAST(@CargoIdx AS NVARCHAR(5)), @Quantity, @Quantity * 5.5, @Quantity * 0.2, @CalculatedCargoPrice);

    IF (@CargoIdx % 2 = 0)
    BEGIN
        INSERT INTO [payment] (passengerTicketId, cargoTicketId, amount, paymentMethod, transactionId, [status], paymentTime)
        VALUES (NULL, @NewCargoId, @CalculatedCargoPrice, 'CASH', 'TXN_C_' + CAST(@CargoIdx AS VARCHAR(5)), 'COMPLETED', GETDATE());
    END;

    SET @CargoIdx = @CargoIdx + 1;
END;

PRINT N'=== TOÀN BỘ CƠ SỞ DỮ LIỆU ĐÃ ĐƯỢC RESET VÀ SEED HOÀN HẢO CHUẨN SCHEMA MỚI ===';
GO

-- ============================================================================
-- LEVEL 6: UPDATE SEAT LAYOUT JSON (BỔ SUNG CẤU TRÚC MA TRẬN GHẾ ĐƠN GIẢN)
-- ============================================================================
PRINT N'-> Đang đồng bộ hóa dữ liệu cấu trúc Seat Layout JSON cho các loại xe...';

UPDATE [coach_type]
SET [seatLayout] = '{"totalFloors":2,"rows":5,"cols":2,"floors":[
    [["SEAT","SEAT"],["SEAT","SEAT"],["SEAT","SEAT"],["SEAT","SEAT"],["SEAT","SEAT"]],
    [["SEAT","SEAT"],["SEAT","SEAT"],["SEAT","SEAT"],["SEAT","SEAT"],["SEAT","SEAT"]]
]}'
WHERE [coachTypeId] = 1;

-- 2. Xe Giường Nằm Luxury 32 chỗ (Mỗi tầng 16 chỗ: 5 hàng full 3 cột + 1 ghế giữa ở hàng cuối)
UPDATE [coach_type]
SET [seatLayout] = '{"totalFloors":2,"rows":6,"cols":3,"floors":[
    [["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["EMPTY","SEAT","EMPTY"]],
    [["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["EMPTY","SEAT","EMPTY"]]
]}'
WHERE [coachTypeId] = 2;

-- 3. Xe Khách Truyền Thống 38 chỗ (Mỗi tầng 19 chỗ: 6 hàng full 3 cột + 1 ghế giữa ở hàng cuối)
UPDATE [coach_type]
SET [seatLayout] = '{"totalFloors":2,"rows":7,"cols":3,"floors":[
    [["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["EMPTY","SEAT","EMPTY"]],
    [["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["SEAT","SEAT","SEAT"],["EMPTY","SEAT","EMPTY"]]
]}'
WHERE [coachTypeId] = 3;

-- RECHECK QUERY AFTER SEED
SELECT * FROM [account];
SELECT * FROM [role];
SELECT * FROM [voucher];
SELECT * FROM [coach_stop];
SELECT * FROM [route];
SELECT * FROM [coach_type];
SELECT * FROM [cargo_type];
SELECT * FROM [account_role];
SELECT * FROM [customer];
SELECT * FROM [ticket_agency];
SELECT * FROM [staff];
SELECT * FROM [route_stop];
SELECT * FROM [seat];
SELECT * FROM [coach_type_price];
SELECT * FROM [cargo_type_price];
SELECT * FROM [coach];
SELECT * FROM [trip];
SELECT * FROM [trip_seat];
SELECT * FROM [passenger_ticket];
SELECT * FROM [cargo_ticket];
SELECT * FROM [passenger_ticket_detail];
SELECT * FROM [cargo_ticket_detail];
SELECT * FROM [payment];
SELECT * FROM [accompanied_child];
SELECT * FROM [refund];