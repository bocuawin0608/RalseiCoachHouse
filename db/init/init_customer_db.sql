-- ============================================================
-- CUSTOMER SERVICE DATABASE INIT SCRIPT
-- Database: customer_db
-- ============================================================
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'customer_db')
BEGIN
    CREATE DATABASE customer_db;
END
GO

USE customer_db;
GO

-- ============================================================
-- TABLE: customer
-- NOTE: accountId is scalar ref to auth_db.account (NO FK)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='customer' AND xtype='U')
BEGIN
    CREATE TABLE customer (
        customerId      INT IDENTITY(1,1) PRIMARY KEY,
        accountId       INT             NOT NULL UNIQUE, -- [CROSS-SERVICE REF] auth_db.account.accountId — NO FK
        fullName        NVARCHAR(150)   NOT NULL,
        phone           VARCHAR(20)     NULL,
        email           VARCHAR(150)    NULL,
        dateOfBirth     DATE            NULL,
        gender          VARCHAR(10)     NULL,
        address         NVARCHAR(300)   NULL,
        createdAt       DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt       DATETIME2       NULL,
        createdBy       INT             NULL,
        updatedBy       INT             NULL
    );
END
GO

-- ============================================================
-- TABLE: voucher
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='voucher' AND xtype='U')
BEGIN
    CREATE TABLE voucher (
        voucherId       INT IDENTITY(1,1) PRIMARY KEY,
        code            VARCHAR(50)     NOT NULL UNIQUE,
        discountPercent DECIMAL(5,2)    NULL,
        discountAmount  DECIMAL(18,2)   NULL,
        voucherType     VARCHAR(30)     NOT NULL,
        minOrderValue   DECIMAL(18,2)   NULL,
        maxDiscount     DECIMAL(18,2)   NULL,
        usageLimit      INT             NULL,
        usedCount       INT             NOT NULL DEFAULT 0,
        expiryDate      DATETIME2       NULL,
        isActive        BIT             NOT NULL DEFAULT 1,
        createdAt       DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt       DATETIME2       NULL,
        createdBy       INT             NULL,
        updatedBy       INT             NULL
    );
END
GO

-- ============================================================
-- TABLE: passenger_ticket
-- NOTE: tripId, soldBy are scalar cross-service refs (NO FK)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='passenger_ticket' AND xtype='U')
BEGIN
    CREATE TABLE passenger_ticket (
        passengerTicketId   INT IDENTITY(1,1) PRIMARY KEY,
        ticketCode          VARCHAR(30)     NOT NULL UNIQUE,
        customerId          INT             NOT NULL,
        tripId              INT             NOT NULL,   -- [CROSS-SERVICE REF] staff_db.trip.tripId — NO FK
        voucherId           INT             NULL,
        soldBy              INT             NULL,        -- [CROSS-SERVICE REF] staff_db.staff.staffId — NO FK
        totalPrice          DECIMAL(18,2)   NOT NULL,
        status              VARCHAR(30)     NOT NULL,
        transactionId       VARCHAR(100)    NULL,
        createdAt           DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt           DATETIME2       NULL,
        createdBy           INT             NULL,
        updatedBy           INT             NULL,
        CONSTRAINT FK_passenger_ticket_customer FOREIGN KEY (customerId) REFERENCES customer(customerId),
        CONSTRAINT FK_passenger_ticket_voucher  FOREIGN KEY (voucherId)  REFERENCES voucher(voucherId)
    );
END
GO

-- ============================================================
-- TABLE: trip_seat
-- NOTE: tripId ref staff_db.trip, seatId ref driver_db.seat (NO FK)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='trip_seat' AND xtype='U')
BEGIN
    CREATE TABLE trip_seat (
        tripSeatId      INT IDENTITY(1,1) PRIMARY KEY,
        tripId          INT             NOT NULL,   -- [CROSS-SERVICE REF] staff_db.trip.tripId — NO FK
        seatId          INT             NOT NULL,   -- [CROSS-SERVICE REF] driver_db.seat.seatId — NO FK
        status          VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE',
        lockedUntil     DATETIME2       NULL,
        lockedBy        INT             NULL,
        createdAt       DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt       DATETIME2       NULL,
        CONSTRAINT UQ_trip_seat UNIQUE (tripId, seatId)
    );
END
GO

-- ============================================================
-- TABLE: passenger_ticket_detail
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='passenger_ticket_detail' AND xtype='U')
BEGIN
    CREATE TABLE passenger_ticket_detail (
        detailId            INT IDENTITY(1,1) PRIMARY KEY,
        passengerTicketId   INT             NOT NULL,
        tripSeatId          INT             NOT NULL,
        passengerName       NVARCHAR(150)   NOT NULL,
        passengerPhone      VARCHAR(20)     NULL,
        passengerIdCard     VARCHAR(20)     NULL,
        price               DECIMAL(18,2)   NOT NULL,
        status              VARCHAR(30)     NOT NULL,
        boardingStopId      INT             NULL,   -- [CROSS-SERVICE REF] staff_db.coach_stop.stopId
        alightingStopId     INT             NULL,   -- [CROSS-SERVICE REF] staff_db.coach_stop.stopId
        qrToken             VARCHAR(255)    NULL,
        createdAt           DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt           DATETIME2       NULL,
        createdBy           INT             NULL,
        updatedBy           INT             NULL,
        CONSTRAINT FK_ptd_passenger_ticket FOREIGN KEY (passengerTicketId) REFERENCES passenger_ticket(passengerTicketId),
        CONSTRAINT FK_ptd_trip_seat        FOREIGN KEY (tripSeatId)        REFERENCES trip_seat(tripSeatId)
    );
END
GO

-- ============================================================
-- TABLE: accompanied_child
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='accompanied_child' AND xtype='U')
BEGIN
    CREATE TABLE accompanied_child (
        childId             INT IDENTITY(1,1) PRIMARY KEY,
        passengerTicketId   INT             NOT NULL,
        childName           NVARCHAR(150)   NOT NULL,
        dateOfBirth         DATE            NULL,
        createdAt           DATETIME2       NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_accompanied_child_ticket FOREIGN KEY (passengerTicketId) REFERENCES passenger_ticket(passengerTicketId)
    );
END
GO

-- ============================================================
-- TABLE: payment
-- NOTE: cargoTicketId is scalar cross-service ref (NO FK)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='payment' AND xtype='U')
BEGIN
    CREATE TABLE payment (
        paymentId           INT IDENTITY(1,1) PRIMARY KEY,
        passengerTicketId   INT             NULL,
        cargoTicketId       INT             NULL,   -- [CROSS-SERVICE REF] staff_db.cargo_ticket.cargoTicketId — NO FK
        amount              DECIMAL(18,2)   NOT NULL,
        paymentMethod       VARCHAR(30)     NULL,
        paymentStatus       VARCHAR(30)     NOT NULL,
        transactionId       VARCHAR(100)    NULL,
        paidAt              DATETIME2       NULL,
        createdAt           DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt           DATETIME2       NULL,
        CONSTRAINT FK_payment_passenger_ticket FOREIGN KEY (passengerTicketId) REFERENCES passenger_ticket(passengerTicketId)
    );
END
GO

-- ============================================================
-- TABLE: refund
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='refund' AND xtype='U')
BEGIN
    CREATE TABLE refund (
        refundId        INT IDENTITY(1,1) PRIMARY KEY,
        paymentId       INT             NOT NULL,
        refundAmount    DECIMAL(18,2)   NOT NULL,
        refundMethod    VARCHAR(30)     NOT NULL,
        refundStatus    VARCHAR(30)     NOT NULL,
        reason          NVARCHAR(500)   NULL,
        refundedAt      DATETIME2       NULL,
        processedBy     INT             NULL,   -- [CROSS-SERVICE REF] staff_db.staff.staffId — NO FK
        createdAt       DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt       DATETIME2       NULL,
        CONSTRAINT FK_refund_payment FOREIGN KEY (paymentId) REFERENCES payment(paymentId)
    );
END
GO
