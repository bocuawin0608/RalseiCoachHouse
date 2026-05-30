-- CRE: NIKO(Gemini Agent) - DATE: 15/06/2024

USE VeXeDB;
GO

SET NOCOUNT ON;
PRINT N'=== BẮT ĐẦU RE-SEED MASTER (V12): FIX TRIỆT ĐỂ CA TRỰC & KHỞI TẠO CARGO TICKET ===';

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
DELETE FROM [trip];
DELETE FROM [coach];
DELETE FROM [cargo_type_price];
DELETE FROM [seat_layout_price];
DELETE FROM [seat];
DELETE FROM [route_stop];
DELETE FROM [staff];
DELETE FROM [ticket_agency];
DELETE FROM [customer];
DELETE FROM [account_role];
DELETE FROM [cargo_type];
DELETE FROM [seat_layout];
DELETE FROM [route];
DELETE FROM [coach_stop];
DELETE FROM [voucher];
DELETE FROM [role];
DELETE FROM [account];

-- RESET TOÀN BỘ CỘT IDENTITY VỀ 0
PRINT N'-> Đang reset bộ đếm Identity...';
DBCC CHECKIDENT ('[account]', RESEED, 0);
DBCC CHECKIDENT ('[role]', RESEED, 0);
DBCC CHECKIDENT ('[voucher]', RESEED, 0);
DBCC CHECKIDENT ('[coach_stop]', RESEED, 0);
DBCC CHECKIDENT ('[route]', RESEED, 0);
DBCC CHECKIDENT ('[seat_layout]', RESEED, 0);
DBCC CHECKIDENT ('[cargo_type]', RESEED, 0);
DBCC CHECKIDENT ('[account_role]', RESEED, 0);
DBCC CHECKIDENT ('[customer]', RESEED, 0);
DBCC CHECKIDENT ('[ticket_agency]', RESEED, 0);
DBCC CHECKIDENT ('[staff]', RESEED, 0);
DBCC CHECKIDENT ('[route_stop]', RESEED, 0);
DBCC CHECKIDENT ('[seat]', RESEED, 0);
DBCC CHECKIDENT ('[seat_layout_price]', RESEED, 0);
DBCC CHECKIDENT ('[cargo_type_price]', RESEED, 0);
DBCC CHECKIDENT ('[coach]', RESEED, 0);
DBCC CHECKIDENT ('[trip]', RESEED, 0);
DBCC CHECKIDENT ('[passenger_ticket]', RESEED, 0);
DBCC CHECKIDENT ('[cargo_ticket]', RESEED, 0);
DBCC CHECKIDENT ('[passenger_ticket_detail]', RESEED, 0);
DBCC CHECKIDENT ('[cargo_ticket_detail]', RESEED, 0);
DBCC CHECKIDENT ('[payment]', RESEED, 0);
DBCC CHECKIDENT ('[accompanied_child]', RESEED, 0);
DBCC CHECKIDENT ('[refund]', RESEED, 0);

-- ============================================================================
-- LEVEL 1: STRONG ENTITIES
-- ============================================================================
PRINT N'-> Đang nạp danh mục cấu trúc nền tảng...';

INSERT INTO [role] (roleName) VALUES ('admin'), ('manager'), ('ticket_staff'), ('trip_staff'), ('customer');

INSERT INTO [voucher] (voucherCode, discountValue, startEffectiveDate, endEffectiveDate, discountType, maxDiscountValue, minOrderValue, usageLimit) VALUES 
('HE2026', 10.00, '2026-01-01', '2028-12-31', 'percent', 50000.00, 200000.00, 1000), 
('GIAM50K', 50000.00, '2026-01-01', '2028-12-31', 'fixed', 50000.00, 0.00, 1000);

INSERT INTO [coach_stop] (stopPointName, address) VALUES 
(N'Bến Xe Nước Ngầm (Hà Nội)', N'Số 1 Ngọc Hồi, Hoàng Mai, Hà Nội'),        
(N'Trạm Dừng Nghỉ Ninh Bình', N'Quốc Lộ 1A, TP. Ninh Bình'),               
(N'Bến Xe Vinh (Nghệ An)', N'Số 77 Lê Lợi, TP. Vinh, Nghệ An'),            
(N'Văn Phòng Đồng Hới (Quảng Bình)', N'Trần Hưng Đạo, Đồng Hới, Quảng Bình');

INSERT INTO [route] (routeName, totalKilometers, totalMinutes) VALUES 
(N'Hà Nội - Quảng Bình', 500.00, 600), 
(N'Quảng Bình - Hà Nội', 500.00, 600); 

INSERT INTO [seat_layout] (seatLayoutName, totalSeat) VALUES 
(N'Xe Limousine VIP 20 phòng', 20),      
(N'Xe Giường Nằm Luxury 32 chỗ', 32),    
(N'Xe Khách Truyền Thống 38 chỗ', 38);   

INSERT INTO [cargo_type] (cargoTypeName) VALUES 
(N'Hàng khô / Thùng Carton'), (N'Xe máy / Xe điện'), (N'Hàng dễ vỡ');


-- ============================================================================
-- LEVEL 2: PERSONNEL & TICKET AGENCY GENERATION (Mạng lưới 22 Đại lý + Data Sạch)
-- ============================================================================
PRINT N'-> Đang cấu hình mạng lưới 22 Đại lý/Phòng vé chiến lược...';

INSERT INTO [ticket_agency] (stopPointId, ticketAgencyName) VALUES 
(1, N'Phòng Vé Số 1 - Bến Xe Nước Ngầm'),
(1, N'Phòng Vé Số 2 - Bến Xe Nước Ngầm'),
(1, N'Văn Phòng Trung Chuyển Hoàn Kiếm (HN)'),
(1, N'Văn Phòng Ký Gửi Hàng Hóa Cầu Giấy (HN)'),
(1, N'Đại Lý Ủy Quyền Mỹ Đình (HN)'),
(1, N'Trạm Thu Gom Bưu Kiện Thanh Xuân (HN)'),
(2, N'Văn Phòng Đại Diện TP. Ninh Bình'),
(2, N'Quầy Vé Di Động Trạm Dừng Ninh Bình'),
(2, N'Đại Lý Nhượng Quyền Tam Điệp (NB)'),
(2, N'Điểm Gom Hàng Huyện Gia Viễn (NB)'),
(3, N'Phòng Vé Chính - Bến Xe Vinh'),
(3, N'Văn Phòng Nhận Trả Hàng Lê Lợi (Vinh)'),
(3, N'Đại Lý Vé Xe Buýt Liên Tỉnh Diễn Châu'),
(3, N'Điểm Ký Gửi Chuyển Phát Nhanh Hoàng Mai (NA)'),
(3, N'Đại Lý Ủy Quyền Thị Xã Cửa Lò (NA)'),
(4, N'Phòng Vé Trung Tâm - Văn Phòng Đồng Hới'),
(4, N'Văn Phòng Ký Gửi Hàng Hóa Ba Đồn (QB)'),
(4, N'Đại Lý Phân Phối Vé Lệ Thủy (QB)'),
(4, N'Trạm Thu Nhận Bưu Kiện Hoàn Lão (QB)'),
(4, N'Điểm Giao Nhận Ký Gửi Huyện Quảng Trạch'),
(4, N'Quầy Vé Số 2 - Văn Phòng Đồng Hới'),
(4, N'Mạng Lưới Đại Lý Nhượng Quyền Tuyên Hóa (QB)');

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
INSERT INTO [account] (username, passwordHash, isActive) OUTPUT inserted.accountId INTO @IdOutput VALUES ('0901111111', 'hash_admin', 1);
SELECT TOP 1 @GeneratedId = Id FROM @IdOutput; DELETE FROM @IdOutput;
INSERT INTO [account_role] (accountId, roleId) VALUES (@GeneratedId, 1);
INSERT INTO [staff] (accountId, ticketAgencyId, staffName, phone, email, dob, cccd, staffPosition, hireDate) 
VALUES (@GeneratedId, NULL, N'Hệ Thống Admin', '0901111111', 'admin.root@vexedb.vn', '1990-05-15', '030090000001', 'Manager', '2024-01-01');

-- Manager Điều Hành
INSERT INTO [account] (username, passwordHash, isActive) OUTPUT inserted.accountId INTO @IdOutput VALUES ('0902222222', 'hash_manager1', 1);
SELECT TOP 1 @GeneratedId = Id FROM @IdOutput; DELETE FROM @IdOutput;
INSERT INTO [account_role] (accountId, roleId) VALUES (@GeneratedId, 2);
INSERT INTO [staff] (accountId, ticketAgencyId, staffName, phone, email, dob, cccd, staffPosition, hireDate) 
VALUES (@GeneratedId, 1, N'Quản Lý Trưởng', '0902222222', 'quanlytruong@vexedb.vn', '1985-10-20', '030085000002', 'Manager', '2024-01-01');

-- Sinh dữ liệu cho 30 Ticket Staff
SET @idx = 1;
WHILE @idx <= 30
BEGIN
    SET @phoneStr = '0931000' + RIGHT('00' + CAST(@idx AS VARCHAR(2)), 2);
    SET @dobStr = CAST((1990 + (@idx % 12)) AS VARCHAR(4)) + '-03-' + RIGHT('0' + CAST((10 + (@idx % 18)) AS VARCHAR(2)), 2);
    SET @cccdStr = '03009' + CAST((1 + (@idx % 9)) AS VARCHAR(1)) + '000' + RIGHT('00' + CAST(@idx AS VARCHAR(2)), 2);
    SET @emailStr = 'ticketstaff' + CAST(@idx AS VARCHAR(2)) + '@vexedb.vn';

    INSERT INTO [account] (username, passwordHash, isActive) OUTPUT inserted.accountId INTO @IdOutput VALUES (@phoneStr, 'hash_ticket', 1);
    SELECT TOP 1 @GeneratedId = Id FROM @IdOutput; DELETE FROM @IdOutput;
    
    DECLARE @TargetAgencyId INT;
    SELECT @TargetAgencyId = AgencyId FROM @AgencyTable WHERE RowIdx = ((@idx % @TotalAgencies) + 1);

    INSERT INTO [account_role] (accountId, roleId) VALUES (@GeneratedId, 3);
    INSERT INTO [staff] (accountId, ticketAgencyId, staffName, phone, email, dob, cccd, staffPosition, hireDate) 
    VALUES (@GeneratedId, @TargetAgencyId, N'NV Bán Vé ' + CAST(@idx AS NVARCHAR(5)), @phoneStr, @emailStr, @dobStr, @cccdStr, 'Ticket Staff', '2025-01-01');
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

    INSERT INTO [account] (username, passwordHash, isActive) OUTPUT inserted.accountId INTO @IdOutput VALUES (@phoneStr, 'hash_driver', 1);
    SELECT TOP 1 @GeneratedId = Id FROM @IdOutput; DELETE FROM @IdOutput;

    INSERT INTO [account_role] (accountId, roleId) VALUES (@GeneratedId, 4);
    INSERT INTO [staff] (accountId, ticketAgencyId, staffName, phone, email, dob, cccd, staffPosition, hireDate) 
    VALUES (@GeneratedId, NULL, N'Tài Xế Chuyên Nghiệp ' + CAST(@idx AS NVARCHAR(5)), @phoneStr, @emailStr, @dobStr, @cccdStr, 'Driver', '2025-01-01');
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

    INSERT INTO [account] (username, passwordHash, isActive) OUTPUT inserted.accountId INTO @IdOutput VALUES (@phoneStr, 'hash_attendant', 1);
    SELECT TOP 1 @GeneratedId = Id FROM @IdOutput; DELETE FROM @IdOutput;

    INSERT INTO [account_role] (accountId, roleId) VALUES (@GeneratedId, 4);
    INSERT INTO [staff] (accountId, ticketAgencyId, staffName, phone, email, dob, cccd, staffPosition, hireDate) 
    VALUES (@GeneratedId, NULL, N'Phụ Xe Tuyến Đường ' + CAST(@idx AS NVARCHAR(5)), @phoneStr, @emailStr, @dobStr, @cccdStr, 'Attendant', '2025-01-01');
    SET @idx = @idx + 1;
END;

-- Khách hàng hệ thống
SET @idx = 1;
WHILE @idx <= 100
BEGIN
    SET @phoneStr = '0960000' + RIGHT('000' + CAST(@idx AS VARCHAR(3)), 3);
    SET @dobStr = CAST((1980 + (@idx % 25)) AS VARCHAR(4)) + '-11-' + RIGHT('0' + CAST((1 + (@idx % 25)) AS VARCHAR(2)), 2);
    SET @cccdStr = '03009' + CAST((1 + (@idx % 5)) AS VARCHAR(1)) + '123' + RIGHT('000' + CAST(@idx AS VARCHAR(3)), 3);

    INSERT INTO [account] (username, passwordHash, isActive) OUTPUT inserted.accountId INTO @IdOutput VALUES (@phoneStr, 'hash_customer', 1);
    SELECT TOP 1 @GeneratedId = Id FROM @IdOutput; DELETE FROM @IdOutput;

    INSERT INTO [account_role] (accountId, roleId) VALUES (@GeneratedId, 5);
    INSERT INTO [customer] (accountId, customerName, phone, email, dob, cccd) 
    VALUES (@GeneratedId, N'Thành Viên App ' + CAST(@idx AS NVARCHAR(5)), @phoneStr, 'user' + CAST(@idx AS VARCHAR(3)) + '@gmail.com', @dobStr, @cccdStr);
    SET @idx = @idx + 1;
END;


-- ============================================================================
-- LEVEL 2.5: CONFIGURING ROUTE STOPS & SEATS
-- ============================================================================
INSERT INTO [route_stop] (routeId, stopPointId, stopOrder, kilometersFromStart, minutesFromStart) VALUES 
(1, 1, 1, 0.00, 0), (1, 2, 2, 90.00, 120), (1, 3, 3, 290.00, 360), (1, 4, 4, 500.00, 600),
(2, 4, 1, 0.00, 0), (2, 3, 2, 210.00, 240), (2, 2, 3, 410.00, 480), (2, 1, 4, 500.00, 600);

DECLARE @s INT = 1;
WHILE @s <= 20 BEGIN INSERT INTO [seat] (seatLayoutId, seatCode, rowIndex, colIndex) VALUES (1, 'L' + RIGHT('0' + CAST(@s AS VARCHAR(2)), 2), (@s+1)/2, CASE WHEN @s%2=0 THEN 2 ELSE 1 END); SET @s = @s + 1; END;
SET @s = 1;
WHILE @s <= 32 BEGIN INSERT INTO [seat] (seatLayoutId, seatCode, rowIndex, colIndex) VALUES (2, 'LX' + RIGHT('0' + CAST(@s AS VARCHAR(2)), 2), (@s+1)/2, CASE WHEN @s%2=0 THEN 2 ELSE 1 END); SET @s = @s + 1; END;
SET @s = 1;
WHILE @s <= 38 BEGIN INSERT INTO [seat] (seatLayoutId, seatCode, rowIndex, colIndex) VALUES (3, 'T' + RIGHT('0' + CAST(@s AS VARCHAR(2)), 2), (@s+1)/2, CASE WHEN @s%2=0 THEN 2 ELSE 1 END); SET @s = @s + 1; END;

INSERT INTO [seat_layout_price] (seatLayoutId, seatPrice, startEffectiveDate, endEffectiveDate) VALUES 
(1, 590000.00, '2026-01-01', '2029-12-31'), 
(2, 450000.00, '2026-01-01', '2029-12-31'), 
(3, 350000.00, '2026-01-01', '2029-12-31'); 

INSERT INTO [cargo_type_price] (cargoTypeId, unit, pricePerUnit, startEffectiveDate, endEffectiveDate) VALUES 
(1, N'Thùng', 60000.00, '2026-01-01', '2029-12-31'), 
(2, N'Chiếc', 400000.00, '2026-01-01', '2029-12-31'), 
(3, N'Kiện', 150000.00, '2026-01-01', '2029-12-31');


-- ============================================================================
-- LEVEL 3: GENERATING COACHES (Đội xe giường nằm chất lượng cao)
-- ============================================================================
PRINT N'-> Đang lắp ráp hạ tầng 365 xe khách giường nằm...';

DECLARE @ManufacturerTable TABLE (Id INT IDENTITY(1,1), Brand NVARCHAR(100));
INSERT INTO @ManufacturerTable (Brand) VALUES 
(N'Thaco Mobihome'), (N'Kia Granbird Silk Road'), (N'Hyundai Universe Express'), (N'Volvo B11R Premium'),
(N'Scania Marcopolo'), (N'Tracomeco Universe'), (N'Samco Primas ISUZU'), (N'Daewoo BX212');
DECLARE @TotalBrands INT = (SELECT COUNT(*) FROM @ManufacturerTable);

DECLARE @c INT = 1;
DECLARE @generatedPlate VARCHAR(20);
DECLARE @coachStatus VARCHAR(20);
DECLARE @pickedBrand NVARCHAR(100);

WHILE @c <= 365
BEGIN
    SET @generatedPlate = CASE WHEN @c % 3 = 0 THEN '29B-' WHEN @c % 3 = 1 THEN '30B-' ELSE '73B-' END + 
                          LEFT(CAST((@c * 137 + 10000) AS VARCHAR(10)), 3) + '.' + 
                          RIGHT(CAST((@c * 79 + 10) AS VARCHAR(10)), 2);
    SET @coachStatus = CASE WHEN @c <= 320 THEN 'active' ELSE 'maintenance' END;
    SELECT @pickedBrand = Brand FROM @ManufacturerTable WHERE Id = ((@c % @TotalBrands) + 1);

    INSERT INTO [coach] (seatLayoutId, licensePlate, [status], manufacturer, [year]) 
    VALUES (CASE WHEN @c%3=0 THEN 1 WHEN @c%3=1 THEN 2 ELSE 3 END, @generatedPlate, @coachStatus, @pickedBrand, 2024);
    SET @c = @c + 1;
END;


-- ============================================================================
-- LEVEL 4: OPERATIONAL ENTITIES - TRIPS (Tần suất sầm uất 24 chuyến/chiều/ngày)
-- ============================================================================
PRINT N'-> Đang chạy thuật toán cách ly thuật toán ca trực chống trùng ID...';

DECLARE @DriverTable TABLE (RowIdx INT IDENTITY(1,1), StaffId INT);
DECLARE @AttendantTable TABLE (RowIdx INT IDENTITY(1,1), StaffId INT);
DECLARE @ActiveCoachTable TABLE (RowIdx INT IDENTITY(1,1), CoachId INT);

INSERT INTO @DriverTable (StaffId) SELECT staffId FROM [staff] WHERE staffPosition = 'Driver' ORDER BY staffId ASC;
INSERT INTO @AttendantTable (StaffId) SELECT staffId FROM [staff] WHERE staffPosition = 'Attendant' ORDER BY staffId ASC;
INSERT INTO @ActiveCoachTable (CoachId) SELECT coachId FROM [coach] WHERE [status] = 'active' ORDER BY coachId ASC;

DECLARE @TotalDrivers INT = (SELECT COUNT(*) FROM @DriverTable);
DECLARE @TotalAttendants INT = (SELECT COUNT(*) FROM @AttendantTable);
DECLARE @TotalActiveCoaches INT = (SELECT COUNT(*) FROM @ActiveCoachTable);

-- Thu hẹp khoảng thời gian seed lịch trình từ 01/01/2026 đến 15/01/2026 để tránh tràn bộ nhớ Log dữ liệu phình to quá mức
DECLARE @StartDate DATETIME = '2026-01-01';
DECLARE @EndDate DATETIME = '2026-01-15'; 
DECLARE @CurrentDate DATETIME = @StartDate;
DECLARE @TripCounter INT = 0;

WHILE @CurrentDate <= @EndDate
BEGIN
    -- Chiều đi: Hà Nội -> Quảng Bình (24 Chuyến/Ngày)
    DECLARE @Slot1 INT = 0;
    WHILE @Slot1 < 24
    BEGIN
        DECLARE @DrvId1 INT, @AtnId1 INT, @CchId1 INT;
        
        -- THUẬT TOÁN OFFSET CÁCH LY CHỐNG TRÙNG ID TUYỆT ĐỐI (CHECK CONSTRAINT)
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
        VALUES (1, @CchId1, @Time1, 'scheduled', @DrvId1, @AtnId1);

        SET @TripCounter = @TripCounter + 1;
        SET @Slot1 = @Slot1 + 1;
    END;

    -- Chiều về: Quảng Bình -> Hà Nội (24 Chuyến/Ngày)
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
        VALUES (2, @CchId2, @Time2, 'scheduled', @DrvId2, @AtnId2);

        SET @TripCounter = @TripCounter + 1;
        SET @Slot2 = @Slot2 + 1;
    END;

    SET @CurrentDate = DATEADD(day, 1, @CurrentDate);
END;

PRINT N' -> Hoàn thành sinh ca trực. Tổng số chuyến xe đã thông qua kiểm tra pháp lý: ' + CAST(@TripCounter AS VARCHAR(10));


-- ============================================================================
-- LEVEL 5.1: PASSENGER TRANSACTIONAL GENERATION
-- ============================================================================
PRINT N'-> Đang phân bổ ngẫu nhiên vé hành khách mẫu và đồng bộ hóa dòng tiền...';

DECLARE @TicketIdx INT = 1;
DECLARE @MinNewTripId INT = (SELECT MIN(tripId) FROM [trip]);
DECLARE @MaxNewTripId INT = (SELECT MAX(tripId) FROM [trip]);
DECLARE @MinCusId INT = (SELECT MIN(customerId) FROM [customer]);
DECLARE @MaxCusId INT = (SELECT MAX(customerId) FROM [customer]);
DECLARE @TicketStaffId INT = (SELECT MIN(staffId) FROM [staff] WHERE staffPosition = 'Ticket Staff');

DECLARE @TargetTripId INT;
DECLARE @TargetLayoutId INT;
DECLARE @CalculatedTicketPrice DECIMAL(15,2);

WHILE @TicketIdx <= 500
BEGIN
    SET @TargetTripId = @MinNewTripId + (@TicketIdx % (@MaxNewTripId - @MinNewTripId + 1));
    
    SELECT @TargetLayoutId = c.seatLayoutId FROM [trip] t JOIN [coach] c ON t.coachId = c.coachId WHERE t.tripId = @TargetTripId;
    SELECT @CalculatedTicketPrice = seatPrice FROM [seat_layout_price] WHERE seatLayoutId = @TargetLayoutId;

    DECLARE @TargetCusId INT = @MinCusId + (@TicketIdx % (@MaxCusId - @MinCusId + 1));
    DECLARE @TicketStatus VARCHAR(20) = CASE WHEN @TicketIdx % 7 = 0 THEN 'pending' ELSE 'confirmed' END;

    INSERT INTO [passenger_ticket] (customerId, tripId, voucherId, soldBy, ticketCode, totalPrice, pickupStopId, dropoffStopId, [status])
    VALUES (@TargetCusId, @TargetTripId, NULL, CASE WHEN @TicketIdx % 3 = 0 THEN @TicketStaffId ELSE NULL END, 'TK_SYS_' + RIGHT('0000' + CAST(@TicketIdx AS VARCHAR(4)), 4), @CalculatedTicketPrice, 1, 4, @TicketStatus);

    DECLARE @NewPId INT = SCOPE_IDENTITY();

    INSERT INTO [passenger_ticket_detail] (passengerTicketId, seatId, fullName, phone, dob, cccd, price, [status])
    VALUES (@NewPId, (@TicketIdx % 18) + 1, N'Hành Khách Ghế ' + CAST(@TicketIdx AS NVARCHAR(5)), '0985' + RIGHT('00000' + CAST(@TicketIdx AS VARCHAR(5)), 5), '1998-04-20', '030098001234', @CalculatedTicketPrice, @TicketStatus);

    INSERT INTO [payment] (passengerTicketId, cargoTicketId, amount, paymentMethod, transactionId, [status], paymentTime)
    VALUES (@NewPId, NULL, @CalculatedTicketPrice, 'vnpay', 'TXN_P_' + CAST(@TicketIdx AS VARCHAR(5)), CASE WHEN @TicketStatus = 'confirmed' THEN 'completed' ELSE 'pending' END, CASE WHEN @TicketStatus = 'confirmed' THEN GETDATE() ELSE NULL END);

    SET @TicketIdx = @TicketIdx + 1;
END;


-- ============================================================================
-- LEVEL 5.2: CARGO TRANSACTIONAL GENERATION (Đã sửa đổi lấp đầy dữ liệu)
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
    
    -- Lấy ngẫu nhiên đơn giá loại hàng hóa ký gửi (Id từ 1 đến 3)
    SET @PickCargoTypePriceId = (@CargoIdx % 3) + 1;
    SELECT @PricePerUnit = pricePerUnit FROM [cargo_type_price] WHERE cargoTypePriceId = @PickCargoTypePriceId;
    
    DECLARE @Quantity INT = (@CargoIdx % 4) + 1;
    SET @CalculatedCargoPrice = @PricePerUnit * @Quantity;

    -- Khởi tạo thực thể hóa đơn vận chuyển bưu kiện tổng
    INSERT INTO [cargo_ticket] (
        tripId, customerId, senderName, senderPhone, senderCccd, 
        receiverName, receiverPhone, receiverCccd, ticketCode, totalPrice, 
        feePayer, codAmount, pickupStopId, dropoffStopId, [status], soldBy
    )
    VALUES (
        @TargetTripId, @TargetCusId, 
        N'Người Gửi Số ' + CAST(@CargoIdx AS NVARCHAR(5)), '0912' + RIGHT('00000' + CAST( @CargoIdx AS VARCHAR(5)), 5), '030091000' + RIGHT('003' + CAST(@CargoIdx AS VARCHAR(3)), 3),
        N'Người Nhận Số ' + CAST(@CargoIdx AS NVARCHAR(5)), '0978' + RIGHT('00000' + CAST(@CargoIdx AS VARCHAR(5)), 5), '030097000' + RIGHT('003' + CAST(@CargoIdx AS VARCHAR(3)), 3),
        'CG_CODE_' + RIGHT('0000' + CAST(@CargoIdx AS VARCHAR(4)), 4), @CalculatedCargoPrice,
        CASE WHEN @CargoIdx % 2 = 0 THEN 'sender' ELSE 'receiver' END,
        CASE WHEN @CargoIdx % 5 = 0 THEN 200000.00 ELSE 0.00 END,
        1, 4, 'received', @TicketStaffId
    );

    DECLARE @NewCargoId INT = SCOPE_IDENTITY();

    -- Khởi tạo chi tiết hàng hóa ký gửi
    INSERT INTO [cargo_ticket_detail] (cargoTicketId, cargoTypePriceId, description, quantity, weightKg, dimensionVol, calculatedPrice)
    VALUES (@NewCargoId, @PickCargoTypePriceId, N'Kiện bưu phẩm ký gửi mẫu số ' + CAST(@CargoIdx AS NVARCHAR(5)), @Quantity, @Quantity * 5.5, @Quantity * 0.2, @CalculatedCargoPrice);

    -- Tạo dòng tiền Payment tương ứng nếu người gửi thanh toán trước luôn tại bến
    IF (@CargoIdx % 2 = 0)
    BEGIN
        INSERT INTO [payment] (passengerTicketId, cargoTicketId, amount, paymentMethod, transactionId, [status], paymentTime)
        VALUES (NULL, @NewCargoId, @CalculatedCargoPrice, 'cash', 'TXN_C_' + CAST(@CargoIdx AS VARCHAR(5)), 'completed', GETDATE());
    END;

    SET @CargoIdx = @CargoIdx + 1;
END;

PRINT N'=== TOÀN BỘ CƠ SỞ DỮ LIỆU ĐÃ ĐƯỢC RESET VÀ SEED HOÀN HẢO CHUẨN V12 ===';
GO
PRINT N'=== TOÀN BỘ CƠ SỞ DỮ LIỆU ĐÃ ĐƯỢC RESET VÀ SEED HOÀN HẢO CHUẨN V11 ===';
GO

--RECHECK QUERRY AFTER SEED
SELECT * FROM [account];
SELECT * FROM [role];
SELECT * FROM [voucher];
SELECT * FROM [coach_stop];     
SELECT * FROM [route];
SELECT * FROM [seat_layout];
SELECT * FROM [cargo_type];
SELECT * FROM [account_role];
SELECT * FROM [customer];
SELECT * FROM [ticket_agency];
SELECT * FROM [staff];
SELECT * FROM [route_stop];
SELECT * FROM [seat];
SELECT * FROM [seat_layout_price];
SELECT * FROM [cargo_type_price];
SELECT * FROM [coach];
SELECT * FROM [trip];
SELECT * FROM [passenger_ticket];
SELECT * FROM [cargo_ticket];
SELECT * FROM [passenger_ticket_detail];
SELECT * FROM [cargo_ticket_detail];
SELECT * FROM [payment];
SELECT * FROM [accompanied_child];
SELECT * FROM [refund];     
    

-- Xem tất cả chuyến xe có trong DBSELECT t.tripId, r.routeName, t.departureTime
SELECT t.tripId,sl.seatLayoutName,r.routeName, t.departureTime
FROM trip t
JOIN route r ON t.routeId = r.routeId
JOIN coach c ON t.coachId = c.coachId
JOIN seat_layout sl ON c.seatLayoutId = sl.seatLayoutId
WHERE t.departureTime BETWEEN '2026-01-01 00:00:00.000' AND '2026-01-01 23:59:59.999'
AND r.routeName = N'Hà Nội - Quảng Bình'
ORDER BY t.departureTime