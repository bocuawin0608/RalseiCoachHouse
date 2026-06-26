USE VeXeDB;
GO

CREATE OR ALTER PROCEDURE sp_AutoGenerateWeeklySchedule_Final
    @StartDate DATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- 1. KHAI BÁO CÁC BIẾN ĐIỀU KHIỂN
    DECLARE @CurrentDate DATE = @StartDate;
    DECLARE @EndDate DATE = DATEADD(DAY, 6, @StartDate); -- Sinh lịch cho 7 ngày
    DECLARE @Slot INT;
    DECLARE @DepartureTime DATETIME;
    
    -- Thời gian giãn cách tối thiểu (phút) để một Xe/Nhân sự có thể chạy chuyến tiếp theo
    -- Chạy 600 phút (10 tiếng) + Nghỉ ngơi/Dọn dẹp 120 phút (2 tiếng) = 720 phút (12 tiếng)
    DECLARE @BufferMinutes INT = 720; 

    PRINT N'=== KHỞI CHẠY THUẬT TOÁN ĐIỀU PHỐI ĐỘI XE ĐỘNG ===';
    PRINT N'Từ ngày: ' + CAST(@CurrentDate AS VARCHAR(10)) + N' đến ' + CAST(@EndDate AS VARCHAR(10));

    -- VÒNG LẶP DUYỆT QUA TỪNG NGÀY
    WHILE @CurrentDate <= @EndDate
    BEGIN
        
        -- ==========================================
        -- CHIỀU ĐI: TUYẾN 1 (HÀ NỘI -> QUẢNG BÌNH)
        -- Mỗi tiếng 1 chuyến (0h -> 23h) -> 24 chuyến/ngày
        -- ==========================================
        SET @Slot = 0;
        WHILE @Slot < 24
        BEGIN
            SET @DepartureTime = DATEADD(HOUR, @Slot, CAST(@CurrentDate AS DATETIME));

            -- BỐC TÀI NGUYÊN ĐỘNG CHO CHIỀU ĐI
            DECLARE @PickedCoachId INT = NULL;
            DECLARE @PickedDriverId INT = NULL;
            DECLARE @PickedAttendantId INT = NULL;

            -- Thuật toán tìm Xe rảnh: 
            -- Loại bỏ 150 xe có Id nhỏ nhất (hoặc lớn nhất) để làm dự phòng/tuyến khác.
            -- Xe phải ACTIVE và không bị kẹt lịch trong khoảng thời gian (@DepartureTime - @BufferMinutes) đến (@DepartureTime + @BufferMinutes)
            SELECT TOP 1 @PickedCoachId = c.coachId
            FROM [coach] c
            WHERE c.[status] = 'ACTIVE'
              AND c.coachId NOT IN (
                  SELECT TOP 150 coachId FROM [coach] WHERE [status] = 'ACTIVE' ORDER BY coachId ASC
              ) -- BIỆN PHÁP CÔ LẬP 150 XE THEO YÊU CẦU
              AND NOT EXISTS (
                  SELECT 1 FROM [trip] t 
                  WHERE t.coachId = c.coachId 
                    AND t.departureTime BETWEEN DATEADD(MINUTE, -@BufferMinutes, @DepartureTime) 
                                            AND DATEADD(MINUTE, @BufferMinutes, @DepartureTime)
              )
            ORDER BY NEWID(); -- Phân bổ ngẫu nhiên đều đội xe, tránh chạy cố định một xe

            -- Thuật toán tìm Tài xế rảnh tại thời điểm đó
            SELECT TOP 1 @PickedDriverId = staffId
            FROM [staff]
            WHERE staffPosition = 'DRIVER'
              AND NOT EXISTS (
                  SELECT 1 FROM [trip] t 
                  WHERE t.driverId = staffId 
                    AND t.departureTime BETWEEN DATEADD(MINUTE, -@BufferMinutes, @DepartureTime) 
                                            AND DATEADD(MINUTE, @BufferMinutes, @DepartureTime)
              )
            ORDER BY NEWID();

            -- Thuật toán tìm Phụ xe rảnh tại thời điểm đó
            SELECT TOP 1 @PickedAttendantId = staffId
            FROM [staff]
            WHERE staffPosition = 'ATTENDANT'
              AND NOT EXISTS (
                  SELECT 1 FROM [trip] t 
                  WHERE t.attendantId = staffId 
                    AND t.departureTime BETWEEN DATEADD(MINUTE, -@BufferMinutes, @DepartureTime) 
                                            AND DATEADD(MINUTE, @BufferMinutes, @DepartureTime)
              )
            ORDER BY NEWID();

            -- Nếu cấu thành đủ tài nguyên thì mới ném vào bảng Trip
            IF @PickedCoachId IS NOT NULL AND @PickedDriverId IS NOT NULL AND @PickedAttendantId IS NOT NULL
            BEGIN
                DECLARE @NewTripId1 INT;
                
                INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId, createdBy, createdAt)
                VALUES (1, @PickedCoachId, @DepartureTime, 'SCHEDULED', @PickedDriverId, @PickedAttendantId, 0, GETDATE());

                SET @NewTripId1 = SCOPE_IDENTITY();

                -- Đổ Snapshot Ghế chuyến dựa theo đúng cấu trúc loại xe được chọn
                INSERT INTO [trip_seat] (tripId, seatId, price, [status])
                SELECT @NewTripId1, s.seatId, ctp.seatPrice, 'AVAILABLE'
                FROM [seat] s
                JOIN [coach] c ON s.coachId = c.coachId
                JOIN [coach_type_price] ctp ON c.coachTypeId = ctp.coachTypeId
                WHERE c.coachId = @PickedCoachId
                  AND @DepartureTime BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate
                  AND s.isActive = 1;
            END
            ELSE
            BEGIN
                PRINT N'CẢNH BÁO: Không đủ tài nguyên xe/lái xe cho chuyến Chiều Đi lúc: ' + CAST(@DepartureTime AS VARCHAR(20));
            END;

            SET @Slot = @Slot + 1;
        END;

        -- ==========================================
        -- CHIỀU VỀ: TUYẾN 2 (QUẢNG BÌNH -> HÀ NỘI)
        -- Mỗi tiếng 1 chuyến, lệch 30 phút để giãn cách dòng chảy dữ liệu
        -- ==========================================
        SET @Slot = 0;
        WHILE @Slot < 24
        BEGIN
            SET @DepartureTime = DATEADD(MINUTE, (@Slot * 60) + 30, CAST(@CurrentDate AS DATETIME));

            DECLARE @PickedCoachId2 INT = NULL;
            DECLARE @PickedDriverId2 INT = NULL;
            DECLARE @PickedAttendantId2 INT = NULL;

            -- Tìm Xe rảnh cho chiều về (Né 150 xe dự phòng và né xe đang chạy dở trên đường)
            SELECT TOP 1 @PickedCoachId2 = c.coachId
            FROM [coach] c
            WHERE c.[status] = 'ACTIVE'
              AND c.coachId NOT IN (
                  SELECT TOP 150 coachId FROM [coach] WHERE [status] = 'ACTIVE' ORDER BY coachId ASC
              )
              AND NOT EXISTS (
                  SELECT 1 FROM [trip] t 
                  WHERE t.coachId = c.coachId 
                    AND t.departureTime BETWEEN DATEADD(MINUTE, -@BufferMinutes, @DepartureTime) 
                                            AND DATEADD(MINUTE, @BufferMinutes, @DepartureTime)
              )
            ORDER BY NEWID();

            -- Tìm Tài xế rảnh chiều về
            SELECT TOP 1 @PickedDriverId2 = staffId
            FROM [staff]
            WHERE staffPosition = 'DRIVER'
              AND NOT EXISTS (
                  SELECT 1 FROM [trip] t 
                  WHERE t.driverId = staffId 
                    AND t.departureTime BETWEEN DATEADD(MINUTE, -@BufferMinutes, @DepartureTime) 
                                            AND DATEADD(MINUTE, @BufferMinutes, @DepartureTime)
              )
            ORDER BY NEWID();

            -- Tìm Phụ xe rảnh chiều về
            SELECT TOP 1 @PickedAttendantId2 = staffId
            FROM [staff]
            WHERE staffPosition = 'ATTENDANT'
              AND NOT EXISTS (
                  SELECT 1 FROM [trip] t 
                  WHERE t.attendantId = staffId 
                    AND t.departureTime BETWEEN DATEADD(MINUTE, -@BufferMinutes, @DepartureTime) 
                                            AND DATEADD(MINUTE, @BufferMinutes, @DepartureTime)
              )
            ORDER BY NEWID();

            IF @PickedCoachId2 IS NOT NULL AND @PickedDriverId2 IS NOT NULL AND @PickedAttendantId2 IS NOT NULL
            BEGIN
                DECLARE @NewTripId2 INT;

                INSERT INTO [trip] (routeId, coachId, departureTime, [status], driverId, attendantId, createdBy, createdAt)
                VALUES (2, @PickedCoachId2, @DepartureTime, 'SCHEDULED', @PickedDriverId2, @PickedAttendantId2, 0, GETDATE());

                SET @NewTripId2 = SCOPE_IDENTITY();

                INSERT INTO [trip_seat] (tripId, seatId, price, [status])
                SELECT @NewTripId2, s.seatId, ctp.seatPrice, 'AVAILABLE'
                FROM [seat] s
                JOIN [coach] c ON s.coachId = c.coachId
                JOIN [coach_type_price] ctp ON c.coachTypeId = ctp.coachTypeId
                WHERE c.coachId = @PickedCoachId2
                  AND @DepartureTime BETWEEN ctp.startEffectiveDate AND ctp.endEffectiveDate
                AND s.isActive = 1;
            END
            ELSE
            BEGIN
                PRINT N'CẢNH BÁO: Không đủ tài nguyên xe/lái xe cho chuyến Chiều Về lúc: ' + CAST(@DepartureTime AS VARCHAR(20));
            END;

            SET @Slot = @Slot + 1;
        END;

        -- Tăng ngày lên tiếp theo
        SET @CurrentDate = DATEADD(DAY, 1, @CurrentDate);
    END;

    PRINT N'=== THUẬT TOÁN ĐÃ HOÀN THÀNH XẾP LỊCH AN TOÀN VÀ TỰ ĐỘNG ===';
END;
GO