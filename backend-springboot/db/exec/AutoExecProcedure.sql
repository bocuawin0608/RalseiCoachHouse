-- chống chỉ định máy yếu, ai yếu thì tắt hết app để mỗi sql server để chạy.
-- t xin miễn trừ trách nhiệm cho ai chạy script này mà ko đọc code tao

USE VeXeDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

IF OBJECT_ID(N'dbo.sp_AutoGenerateWeeklySchedule_Final', N'P') IS NULL
BEGIN
    THROW 50001, 'Procedure dbo.sp_AutoGenerateWeeklySchedule_Final does not exist. Run Procedure.sql first.', 1;
END;

DECLARE @NumberOfWeeks INT = 10;
DECLARE @CurrentWeek INT = 1;
DECLARE @WeekStartDate DATE;
DECLARE @WeekEndDate DATE;
DECLARE @LastTripEndDate DATE;
DECLARE @Today DATE = CAST(GETDATE() AS DATE);

-- A trip ends after its route duration, which can be on the following day.
-- Using the latest departure date here could schedule new trips while the
-- final overnight trip is still running.
SELECT @LastTripEndDate = CAST(
    MAX(DATEADD(MINUTE, r.totalMinutes, t.departureTime)) AS DATE
)
FROM dbo.[trip] AS t
INNER JOIN dbo.[route] AS r ON r.routeId = t.routeId;

-- Never generate a schedule in the past. Continue after the latest trip when
-- it is current/future data; otherwise, start from today. The added day means
-- generation cannot begin on a calendar date when the final trip still runs.
SET @WeekStartDate = CASE
    WHEN @LastTripEndDate IS NOT NULL AND @LastTripEndDate >= @Today
        THEN DATEADD(DAY, 1, @LastTripEndDate)
    ELSE @Today
END;

WHILE @CurrentWeek <= @NumberOfWeeks
BEGIN
    SET @WeekEndDate = DATEADD(DAY, 7, @WeekStartDate);

    -- Use a half-open range so trips at any time on the seventh day are
    -- included, while midnight of the following week is not.
    IF NOT EXISTS
    (
        SELECT 1
        FROM dbo.[trip] AS t
        WHERE t.departureTime >= @WeekStartDate
          AND t.departureTime < @WeekEndDate
    )
    BEGIN
        PRINT CONCAT(
            N'Generating week ', @CurrentWeek, N' of ', @NumberOfWeeks,
            N' (', CONVERT(CHAR(10), @WeekStartDate, 23), N' to ',
            CONVERT(CHAR(10), DATEADD(DAY, 6, @WeekStartDate), 23), N').'
        );

        EXEC dbo.sp_AutoGenerateWeeklySchedule_Final @StartDate = @WeekStartDate;
    END
    ELSE
    BEGIN
        PRINT CONCAT(
            N'Skipping week ', @CurrentWeek, N' of ', @NumberOfWeeks,
            N' because it already contains at least one trip (',
            CONVERT(CHAR(10), @WeekStartDate, 23), N' to ',
            CONVERT(CHAR(10), DATEADD(DAY, 6, @WeekStartDate), 23), N').'
        );
    END;

    SET @WeekStartDate = DATEADD(DAY, 7, @WeekStartDate);
    SET @CurrentWeek += 1;
END;
GO
