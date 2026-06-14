USE VeXeDB;
GO

CREATE OR ALTER PROCEDURE sp_AutoGenerateWeeklySchedule_Final
    @StartDate DATE -- Chỉ cần duy nhất ngày bắt đầu, dẹp bỏ hoàn toàn ManagerId!
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Định nghĩa ID mặc định cho hệ thống chạy tự động ngầm (Audit Trail = 0 tức là SYSTEM)
    DECLARE @SystemUserId INT = 0; 
    
    DECLARE @CurrentDate DATE = @StartDate;
    DECLARE @EndDate DATE = DATEADD(DAY, 6, @StartDate);
    
    -- Lấy danh sách tài xế, phụ xe và xe đang hoạt động ổn định
    DECLARE @Coaches TABLE (RowIdx INT IDENTITY(1,1), CoachId INT, CoachTypeId INT);
    DECLARE @Drivers TABLE (RowIdx INT IDENTITY(1,1), DriverId INT);
    DECLARE @Attendants TABLE (RowIdx INT IDENTITY(1,1), AttendantId INT);
    
    INSERT INTO @Coaches (CoachId, CoachTypeId) SELECT coachId, coachTypeId FROM [coach] WHERE [status] = 'ACTIVE';
    INSERT INTO @Drivers (DriverId) SELECT staffId FROM [staff] WHERE staffPosition = 'DRIVER' AND isActive = 1;
    INSERT INTO @Attendants (AttendantId) SELECT staffId FROM [staff] WHERE staffPosition = 'ATTENDANT' AND isActive = 1;
    
    DECLARE @TotalCoaches INT = (SELECT COUNT(*) FROM @Coaches);
    DECLARE @TotalDrivers INT = (SELECT COUNT(*) FROM @Drivers);
    DECLARE @TotalAttendants INT = (SELECT COUNT(*) FROM @Attendants);

    PRINT N'=== BẮT ĐẦU THUẬT TOÁN SINH LỊCH TỰ ĐỘNG CHUẨN XOAY VÒNG ===';

    -- 2. Vòng lặp quét qua từng ngày trong tuần cần lên lịch
    WHILE @CurrentDate <= @EndDate
    BEGIN
        -- Khởi tạo 10 khung giờ cố định trong ngày cho Chiều Đi (Hà Nội -> Quảng Bình)
        -- Chọn các khung giờ tối/đêm tập trung từ 18:00 đến 22:30 để khớp nghiệp vụ xoay đầu trưa hôm sau
        DECLARE @GoingSlots TABLE (SlotId INT IDENTITY(1,1), StartTime TIME);
        DELETE FROM @GoingSlots;
        INSERT INTO @GoingSlots (StartTime) VALUES 
        ('18:00:00'), ('18:30:00'), ('19:00:00'), ('19:30:00'), ('20:00:00'),
        ('20:30:00'), ('21:00:00'), ('21:30:00'), ('22:00:00'), ('22:30:00');

        -- Khởi tạo 10 khung giờ cố định trong ngày cho Chiều Về (Quảng Bình -> Hà Nội)
        -- Tập trung vào khung giờ trưa từ 11:00 đến 15:30
        DECLARE @ReturningSlots TABLE (SlotId INT IDENTITY(1,1), StartTime TIME);
        DELETE FROM @ReturningSlots;
        INSERT INTO @ReturningSlots (StartTime) VALUES 
        ('11:00:00'), ('11:30:00'), ('12:00:00'), ('12:30:00'), ('13:00:00'),
        ('13:30:00'), ('14:00:00'), ('14:30:00'), ('15:00:00'), ('15:30:00');

        DECLARE @SlotIdx INT = 1;
        
        -- ----------------------------------------------------------------------
        -- LUỒNG 1: XẾP 10 CHUYẾN CHIỀU ĐI (HÀ NỘI -> QUẢNG BÌNH) - TUYẾN ID = 1
        -- ----------------------------------------------------------------------
        WHILE @SlotIdx <= 10
        BEGIN
            DECLARE @TargetTime DATETIME;
            DECLARE @SlotTime TIME;
            SELECT @SlotTime = StartTime FROM @GoingSlots WHERE SlotId = @SlotIdx;
            SET @TargetTime = CAST(@CurrentDate AS DATETIME) + CAST(@SlotTime AS DATETIME);

            -- Thuật toán bốc tài nguyên dựa trên mã ngày để né trùng lặp và tạo độ giãn cách nghỉ ngơi
            DECLARE @Seed INT = (DATEPART(DAY, @CurrentDate) * 10) + @SlotIdx;
            
            -- Quy luật kiểm tra chu kỳ nghỉ: Nếu số ngày chia hết cho chu kỳ nghỉ thì cho nghỉ
            -- Bảo đảm tài xế/xe chạy 2 ngày liên tục thì rơi vào block nghỉ ngơi không xếp lịch
            IF (@Seed % 4 IN (0, 1)) 
            BEGIN
                DECLARE @CchId INT, @DrvId INT, @AtnId INT, @CchTypeId INT;
                SELECT @CchId = CoachId, @CchTypeId = CoachTypeId FROM @Coaches WHERE RowIdx = ((@Seed % @TotalCoaches) + 1);
                SELECT @DrvId = DriverId FROM @Drivers WHERE RowIdx = ((@Seed % @TotalDrivers) + 1);
                SELECT @AtnId = AttendantId FROM @Attendants WHERE RowIdx = (((@Seed + 5) % @TotalAttendants) + 1);

                -- Tiến hành ghi nhận chuyến đi vật lý
                DECLARE @NewTripId INT;
                INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId, createdAt, createdBy)
                VALUES (1, @CchId, @TargetTime, 'SCHEDULED', @DrvId, @AtnId, GETDATE(), @SystemUserId);
                
                SET @NewTripId = SCOPE_IDENTITY();

                -- Đổ Snapshot ma trận ghế trống cho chuyến đi ngay lập tức
                INSERT INTO [trip_seat] (tripId, seatId, price, [status], createdAt, createdBy)
                SELECT @NewTripId, s.seatId, ctp.seatPrice, 'AVAILABLE', GETDATE(), @SystemUserId
                FROM [seat] s
                JOIN [coach_type_price] ctp ON ctp.coachTypeId = @CchTypeId
                WHERE s.coachId = @CchId
                  AND @TargetTime BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate;
            END

            SET @SlotIdx = @SlotIdx + 1;
        END;

        -- ----------------------------------------------------------------------
        -- LUỒNG 2: XẾP 10 CHUYẾN CHIỀU VỀ (QUẢNG BÌNH -> HÀ NỘI) - TUYẾN ID = 2
        -- ----------------------------------------------------------------------
        SET @SlotIdx = 1;
        WHILE @SlotIdx <= 10
        BEGIN
            DECLARE @RetTime TIME;
            SELECT @RetTime = StartTime FROM @ReturningSlots WHERE SlotId = @SlotIdx;
            
            -- Ràng buộc cốt lõi: Xe chạy tối hôm trước (Ngày - 1) sẽ quay đầu vào trưa ngày hôm sau
            DECLARE @TargetReturnTime DATETIME = CAST(@CurrentDate AS DATETIME) + CAST(@RetTime AS DATETIME);
            
            -- Lấy lại Seed dịch chuyển từ ngày hôm trước để bốc đúng cặp Xe/Tài xế đã đi chiều đi hôm qua
            DECLARE @Yesterday DATE = DATEADD(DAY, -1, @CurrentDate);
            DECLARE @PrevSeed INT = (DATEPART(DAY, @Yesterday) * 10) + @SlotIdx;

            IF (@PrevSeed % 4 IN (0, 1))
            BEGIN
                DECLARE @RCchId INT, @RDrvId INT, @RAtnId INT, @RCchTypeId INT;
                SELECT @RCchId = CoachId, @RCchTypeId = CoachTypeId FROM @Coaches WHERE RowIdx = ((@PrevSeed % @TotalCoaches) + 1);
                SELECT @RDrvId = DriverId FROM @Drivers WHERE RowIdx = ((@PrevSeed % @TotalDrivers) + 1);
                SELECT @RAtnId = AttendantId FROM @Attendants WHERE RowIdx = (((@PrevSeed + 5) % @TotalAttendants) + 1);

                DECLARE @NewRetTripId INT;
                INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId, createdAt, createdBy)
                VALUES (2, @RCchId, @TargetReturnTime, 'SCHEDULED', @RDrvId, @RAtnId, GETDATE(), @SystemUserId);
                
                SET @NewRetTripId = SCOPE_IDENTITY();

                -- Đổ Snapshot ma trận ghế
                INSERT INTO [trip_seat] (tripId, seatId, price, [status], createdAt, createdBy)
                SELECT @NewRetTripId, s.seatId, ctp.seatPrice, 'AVAILABLE', GETDATE(), @SystemUserId
                FROM [seat] s
                JOIN [coach_type_price] ctp ON ctp.coachTypeId = @RCchTypeId
                WHERE s.coachId = @RCchId
                  AND @TargetReturnTime BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate;
            END

            SET @SlotIdx = @SlotIdx + 1;
        END;

        SET @CurrentDate = DATEADD(DAY, 1, @CurrentDate);
    END;

    PRINT N'=== HOÀN THÀNH XẾP LỊCH TỰ ĐỘNG TẬP TRUNG CHUẨN 20 CHUYẾN/NGÀY ===';
END;
GO