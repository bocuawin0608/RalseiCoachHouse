-- ============================================================
-- DRIVER SERVICE DATABASE INIT SCRIPT
-- Database: driver_db
-- ============================================================
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'driver_db')
BEGIN
    CREATE DATABASE driver_db;
END
GO

USE driver_db;
GO

-- ============================================================
-- TABLE: coach_type
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='coach_type' AND xtype='U')
BEGIN
    CREATE TABLE coach_type (
        coachTypeId     INT IDENTITY(1,1) PRIMARY KEY,
        coachTypeName   VARCHAR(100)    NOT NULL UNIQUE,
        totalSeat       INT             NOT NULL,
        description     NVARCHAR(500)   NULL,
        imageUrl        VARCHAR(255)    NULL,
        createdAt       DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt       DATETIME2       NULL,
        createdBy       INT             NULL,
        updatedBy       INT             NULL
    );
END
GO

-- ============================================================
-- TABLE: coach_type_price
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='coach_type_price' AND xtype='U')
BEGIN
    CREATE TABLE coach_type_price (
        coachTypePriceId    INT IDENTITY(1,1) PRIMARY KEY,
        coachTypeId         INT             NOT NULL,
        pricePerKm          DECIMAL(18,2)   NOT NULL,
        effectiveFrom       DATE            NOT NULL,
        effectiveTo         DATE            NULL,
        status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
        createdAt           DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt           DATETIME2       NULL,
        createdBy           INT             NULL,
        updatedBy           INT             NULL,
        CONSTRAINT FK_coach_type_price_coach_type FOREIGN KEY (coachTypeId) REFERENCES coach_type(coachTypeId)
    );
END
GO

-- ============================================================
-- TABLE: coach
-- NOTE: routeId is a scalar reference to staff_db.route (NO FK)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='coach' AND xtype='U')
BEGIN
    CREATE TABLE coach (
        coachId         INT IDENTITY(1,1) PRIMARY KEY,
        coachTypeId     INT             NOT NULL,
        routeId         INT             NULL,   -- [CROSS-SERVICE REF] staff_db.route.routeId — NO FK
        licensePlate    VARCHAR(20)     NOT NULL UNIQUE,
        color           VARCHAR(50)     NULL,
        manufactureYear INT             NULL,
        status          VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',
        createdAt       DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt       DATETIME2       NULL,
        createdBy       INT             NULL,
        updatedBy       INT             NULL,
        CONSTRAINT FK_coach_coach_type FOREIGN KEY (coachTypeId) REFERENCES coach_type(coachTypeId)
    );
END
GO

-- ============================================================
-- TABLE: seat
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='seat' AND xtype='U')
BEGIN
    CREATE TABLE seat (
        seatId      INT IDENTITY(1,1) PRIMARY KEY,
        coachId     INT             NOT NULL,
        seatCode    VARCHAR(10)     NOT NULL,
        floor       INT             NOT NULL DEFAULT 1,
        rowNum      INT             NOT NULL,
        colNum      INT             NOT NULL,
        seatType    VARCHAR(20)     NULL,
        createdAt   DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt   DATETIME2       NULL,
        createdBy   INT             NULL,
        updatedBy   INT             NULL,
        CONSTRAINT FK_seat_coach FOREIGN KEY (coachId) REFERENCES coach(coachId),
        CONSTRAINT UQ_seat_coach_code UNIQUE (coachId, seatCode)
    );
END
GO

-- ============================================================
-- TABLE: coach_status_log
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='coach_status_log' AND xtype='U')
BEGIN
    CREATE TABLE coach_status_log (
        logId           INT IDENTITY(1,1) PRIMARY KEY,
        coachId         INT             NOT NULL,
        previousStatus  VARCHAR(30)     NULL,
        newStatus       VARCHAR(30)     NOT NULL,
        reason          NVARCHAR(500)   NULL,
        changedAt       DATETIME2       NOT NULL DEFAULT GETDATE(),
        changedBy       INT             NULL,
        CONSTRAINT FK_coach_status_log_coach FOREIGN KEY (coachId) REFERENCES coach(coachId)
    );
END
GO
