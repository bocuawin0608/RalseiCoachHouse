-- ============================================================
-- DRIVER SERVICE DATABASE DDL (SQL Server / T-SQL)
-- Database: driver_db
-- Bounded Context: Driver & Vehicles
-- ============================================================

-- 1. Table: coach_type
CREATE TABLE coach_type (
    coachTypeId INT IDENTITY(1,1) PRIMARY KEY,
    coachTypeName VARCHAR(100) NOT NULL UNIQUE,
    totalSeat INT NOT NULL,
    seatLayout NVARCHAR(MAX) NOT NULL,
    isActive BIT NOT NULL DEFAULT 1,
    createdAt DATETIME2 DEFAULT GETDATE(),
    createdBy INT NULL,
    updatedAt DATETIME2 NULL,
    updatedBy INT NULL
);

-- 2. Table: coach_type_price
CREATE TABLE coach_type_price (
    coachTypePriceId INT IDENTITY(1,1) PRIMARY KEY,
    coachTypeId INT NOT NULL,
    seatPrice DECIMAL(18,2) NOT NULL,
    startEffectiveDate DATETIME2 NOT NULL,
    endEffectiveDate DATETIME2 NULL,
    createdAt DATETIME2 DEFAULT GETDATE(),
    createdBy INT NULL,
    updatedAt DATETIME2 NULL,
    updatedBy INT NULL,
    CONSTRAINT FK_CoachTypePrice_CoachType FOREIGN KEY (coachTypeId) REFERENCES coach_type(coachTypeId)
);

-- 3. Table: coach
-- CRITICAL BOUNDED CONTEXT RULE: routeId is a scalar column (INT).
-- ABSOLUTELY NO Foreign Key pointing to Route table or Staff Service database.
CREATE TABLE coach (
    coachId INT IDENTITY(1,1) PRIMARY KEY,
    coachTypeId INT NOT NULL,
    routeId INT NULL, -- Scalar reference to Staff Service Route ID
    licensePlate VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    manufacturer VARCHAR(100) NULL,
    year INT NULL,
    createdAt DATETIME2 DEFAULT GETDATE(),
    createdBy INT NULL,
    updatedAt DATETIME2 NULL,
    updatedBy INT NULL,
    CONSTRAINT FK_Coach_CoachType FOREIGN KEY (coachTypeId) REFERENCES coach_type(coachTypeId)
);

-- 4. Table: seat
CREATE TABLE seat (
    seatId INT IDENTITY(1,1) PRIMARY KEY,
    coachId INT NOT NULL,
    seatCode VARCHAR(20) NOT NULL,
    rowIndex INT NOT NULL,
    colIndex INT NOT NULL,
    floorIndex INT NOT NULL,
    isActive BIT NOT NULL DEFAULT 1,
    CONSTRAINT FK_Seat_Coach FOREIGN KEY (coachId) REFERENCES coach(coachId) ON DELETE CASCADE
);

-- 5. Table: coach_status_log
CREATE TABLE coach_status_log (
    coachStatusLogId INT IDENTITY(1,1) PRIMARY KEY,
    coachId INT NOT NULL,
    fromStatus VARCHAR(50) NULL,
    toStatus VARCHAR(50) NOT NULL,
    reason NVARCHAR(500) NULL,
    expectedEndAt DATETIME2 NULL,
    createdAt DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_CoachStatusLog_Coach FOREIGN KEY (coachId) REFERENCES coach(coachId) ON DELETE CASCADE
);
