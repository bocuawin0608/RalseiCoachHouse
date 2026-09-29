-- ============================================================
-- STAFF SERVICE DATABASE INIT SCRIPT
-- Database: staff_db
-- ============================================================
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'staff_db')
BEGIN
    CREATE DATABASE staff_db;
END
GO

USE staff_db;
GO

-- ============================================================
-- TABLE: ticket_agency
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='ticket_agency' AND xtype='U')
BEGIN
    CREATE TABLE ticket_agency (
        ticketAgencyId  INT IDENTITY(1,1) PRIMARY KEY,
        agencyName      NVARCHAR(200)   NOT NULL,
        address         NVARCHAR(300)   NULL,
        phone           VARCHAR(20)     NULL,
        isActive        BIT             NOT NULL DEFAULT 1,
        createdAt       DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt       DATETIME2       NULL,
        createdBy       INT             NULL,
        updatedBy       INT             NULL
    );
END
GO

-- ============================================================
-- TABLE: staff
-- NOTE: accountId is scalar ref to auth_db.account (NO FK)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='staff' AND xtype='U')
BEGIN
    CREATE TABLE staff (
        staffId         INT IDENTITY(1,1) PRIMARY KEY,
        accountId       INT             NOT NULL UNIQUE, -- [CROSS-SERVICE REF] auth_db.account.accountId — NO FK
        ticketAgencyId  INT             NULL,
        fullName        NVARCHAR(150)   NOT NULL,
        phone           VARCHAR(20)     NULL,
        email           VARCHAR(150)    NULL,
        position        VARCHAR(50)     NULL,
        isActive        BIT             NOT NULL DEFAULT 1,
        createdAt       DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt       DATETIME2       NULL,
        createdBy       INT             NULL,
        updatedBy       INT             NULL,
        CONSTRAINT FK_staff_ticket_agency FOREIGN KEY (ticketAgencyId) REFERENCES ticket_agency(ticketAgencyId)
    );
END
GO

-- ============================================================
-- TABLE: route
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='route' AND xtype='U')
BEGIN
    CREATE TABLE route (
        routeId         INT IDENTITY(1,1) PRIMARY KEY,
        routeName       NVARCHAR(200)   NOT NULL,
        originProvince  VARCHAR(100)    NOT NULL,
        destProvince    VARCHAR(100)    NOT NULL,
        distanceKm      DECIMAL(10,2)   NULL,
        estimatedHours  INT             NULL,
        isActive        BIT             NOT NULL DEFAULT 1,
        createdAt       DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt       DATETIME2       NULL,
        createdBy       INT             NULL,
        updatedBy       INT             NULL
    );
END
GO

-- ============================================================
-- TABLE: coach_stop
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='coach_stop' AND xtype='U')
BEGIN
    CREATE TABLE coach_stop (
        stopId          INT IDENTITY(1,1) PRIMARY KEY,
        stopName        NVARCHAR(200)   NOT NULL,
        province        VARCHAR(100)    NOT NULL,
        address         NVARCHAR(300)   NULL,
        latitude        DECIMAL(10,7)   NULL,
        longitude       DECIMAL(10,7)   NULL,
        createdAt       DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt       DATETIME2       NULL,
        createdBy       INT             NULL,
        updatedBy       INT             NULL
    );
END
GO

-- ============================================================
-- TABLE: route_stop
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='route_stop' AND xtype='U')
BEGIN
    CREATE TABLE route_stop (
        routeStopId     INT IDENTITY(1,1) PRIMARY KEY,
        routeId         INT             NOT NULL,
        stopId          INT             NOT NULL,
        stopOrder       INT             NOT NULL,
        arrivalOffset   INT             NULL,   -- minutes from departure
        createdAt       DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt       DATETIME2       NULL,
        CONSTRAINT FK_route_stop_route      FOREIGN KEY (routeId) REFERENCES route(routeId),
        CONSTRAINT FK_route_stop_coach_stop FOREIGN KEY (stopId)  REFERENCES coach_stop(stopId),
        CONSTRAINT UQ_route_stop            UNIQUE (routeId, stopOrder)
    );
END
GO

-- ============================================================
-- TABLE: trip
-- NOTE: coachId is scalar ref to driver_db.coach (NO FK)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='trip' AND xtype='U')
BEGIN
    CREATE TABLE trip (
        tripId          INT IDENTITY(1,1) PRIMARY KEY,
        routeId         INT             NOT NULL,
        coachId         INT             NOT NULL,   -- [CROSS-SERVICE REF] driver_db.coach.coachId — NO FK
        departureTime   DATETIME2       NOT NULL,
        arrivalTime     DATETIME2       NULL,
        status          VARCHAR(30)     NOT NULL DEFAULT 'SCHEDULED',
        basePrice       DECIMAL(18,2)   NULL,
        createdAt       DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt       DATETIME2       NULL,
        createdBy       INT             NULL,
        updatedBy       INT             NULL,
        CONSTRAINT FK_trip_route FOREIGN KEY (routeId) REFERENCES route(routeId)
    );
END
GO

-- ============================================================
-- TABLE: cargo_type
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='cargo_type' AND xtype='U')
BEGIN
    CREATE TABLE cargo_type (
        cargoTypeId     INT IDENTITY(1,1) PRIMARY KEY,
        typeName        NVARCHAR(100)   NOT NULL UNIQUE,
        description     NVARCHAR(500)   NULL,
        isActive        BIT             NOT NULL DEFAULT 1,
        createdAt       DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt       DATETIME2       NULL,
        createdBy       INT             NULL,
        updatedBy       INT             NULL
    );
END
GO

-- ============================================================
-- TABLE: cargo_type_price
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='cargo_type_price' AND xtype='U')
BEGIN
    CREATE TABLE cargo_type_price (
        cargoTypePriceId    INT IDENTITY(1,1) PRIMARY KEY,
        cargoTypeId         INT             NOT NULL,
        pricePerKg          DECIMAL(18,2)   NOT NULL,
        effectiveFrom       DATE            NOT NULL,
        effectiveTo         DATE            NULL,
        isActive            BIT             NOT NULL DEFAULT 1,
        createdAt           DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt           DATETIME2       NULL,
        createdBy           INT             NULL,
        updatedBy           INT             NULL,
        CONSTRAINT FK_cargo_type_price_cargo_type FOREIGN KEY (cargoTypeId) REFERENCES cargo_type(cargoTypeId)
    );
END
GO

-- ============================================================
-- TABLE: cargo_ticket
-- NOTE: customerId is scalar ref to customer_db.customer (NO FK)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='cargo_ticket' AND xtype='U')
BEGIN
    CREATE TABLE cargo_ticket (
        cargoTicketId   INT IDENTITY(1,1) PRIMARY KEY,
        ticketCode      VARCHAR(30)     NOT NULL UNIQUE,
        tripId          INT             NOT NULL,
        customerId      INT             NULL,   -- [CROSS-SERVICE REF] customer_db.customer.customerId — NO FK
        soldBy          INT             NULL,
        loadedBy        INT             NULL,
        unloadedBy      INT             NULL,
        deliveredBy     INT             NULL,
        totalPrice      DECIMAL(18,2)   NOT NULL,
        status          VARCHAR(30)     NOT NULL,
        senderName      NVARCHAR(150)   NOT NULL,
        senderPhone     VARCHAR(20)     NOT NULL,
        receiverName    NVARCHAR(150)   NOT NULL,
        receiverPhone   VARCHAR(20)     NOT NULL,
        transactionId   VARCHAR(100)    NULL,
        createdAt       DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt       DATETIME2       NULL,
        createdBy       INT             NULL,
        updatedBy       INT             NULL,
        CONSTRAINT FK_cargo_ticket_trip             FOREIGN KEY (tripId)    REFERENCES trip(tripId),
        CONSTRAINT FK_cargo_ticket_sold_by_staff    FOREIGN KEY (soldBy)    REFERENCES staff(staffId),
        CONSTRAINT FK_cargo_ticket_loaded_by_staff  FOREIGN KEY (loadedBy)  REFERENCES staff(staffId),
        CONSTRAINT FK_cargo_ticket_unloaded_staff   FOREIGN KEY (unloadedBy)REFERENCES staff(staffId),
        CONSTRAINT FK_cargo_ticket_delivered_staff  FOREIGN KEY (deliveredBy)REFERENCES staff(staffId)
    );
END
GO

-- ============================================================
-- TABLE: cargo_ticket_detail
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='cargo_ticket_detail' AND xtype='U')
BEGIN
    CREATE TABLE cargo_ticket_detail (
        cargoDetailId   INT IDENTITY(1,1) PRIMARY KEY,
        cargoTicketId   INT             NOT NULL,
        cargoTypeId     INT             NOT NULL,
        itemName        NVARCHAR(200)   NOT NULL,
        weightKg        DECIMAL(10,3)   NOT NULL,
        volume          DECIMAL(10,3)   NULL,
        itemPrice       DECIMAL(18,2)   NOT NULL,
        quantity        INT             NOT NULL DEFAULT 1,
        createdAt       DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt       DATETIME2       NULL,
        CONSTRAINT FK_cargo_detail_ticket    FOREIGN KEY (cargoTicketId) REFERENCES cargo_ticket(cargoTicketId),
        CONSTRAINT FK_cargo_detail_cargo_type FOREIGN KEY (cargoTypeId)  REFERENCES cargo_type(cargoTypeId)
    );
END
GO
