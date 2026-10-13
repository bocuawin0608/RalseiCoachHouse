-- ============================================================
-- AUTH SERVICE DATABASE INIT SCRIPT
-- Database: auth_db
-- ============================================================
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'auth_db')
BEGIN
    CREATE DATABASE auth_db;
END
GO

USE auth_db;
GO

-- ============================================================
-- TABLE: role
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='role' AND xtype='U')
BEGIN
    CREATE TABLE role (
        roleId      INT IDENTITY(1,1) PRIMARY KEY,
        roleName    VARCHAR(50) NOT NULL UNIQUE
    );
END
GO

-- ============================================================
-- TABLE: account
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='account' AND xtype='U')
BEGIN
    CREATE TABLE account (
        accountId       INT IDENTITY(1,1) PRIMARY KEY,
        username        VARCHAR(100)    NOT NULL UNIQUE,
        password        VARCHAR(255)    NULL,
        authProvider    VARCHAR(30)     NOT NULL DEFAULT 'LOCAL',
        isActive        BIT             NOT NULL DEFAULT 1,
        lastLogin       DATETIME2       NULL,
        createdAt       DATETIME2       NOT NULL DEFAULT GETDATE(),
        updatedAt       DATETIME2       NULL,
        createdBy       INT             NULL,
        updatedBy       INT             NULL
    );
END
GO

-- ============================================================
-- TABLE: account_role
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='account_role' AND xtype='U')
BEGIN
    CREATE TABLE account_role (
        accountRoleId   INT IDENTITY(1,1) PRIMARY KEY,
        accountId       INT NOT NULL,
        roleId          INT NOT NULL,
        CONSTRAINT FK_account_role_account FOREIGN KEY (accountId) REFERENCES account(accountId),
        CONSTRAINT FK_account_role_role    FOREIGN KEY (roleId)    REFERENCES role(roleId),
        CONSTRAINT UQ_account_role         UNIQUE (accountId, roleId)
    );
END
GO

-- ============================================================
-- TABLE: refresh_token
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='refresh_token' AND xtype='U')
BEGIN
    CREATE TABLE refresh_token (
        tokenId     INT IDENTITY(1,1) PRIMARY KEY,
        accountId   INT NOT NULL,
        token       VARCHAR(512) NOT NULL UNIQUE,
        expiryDate  DATETIME2   NOT NULL,
        revoked     BIT         NOT NULL DEFAULT 0,
        createdAt   DATETIME2   NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_refresh_token_account FOREIGN KEY (accountId) REFERENCES account(accountId)
    );
END
GO

-- ============================================================
-- SEED DATA: default roles
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM role WHERE roleName = 'ROLE_CUSTOMER')
BEGIN
    INSERT INTO role (roleName) VALUES
        ('ROLE_CUSTOMER'),
        ('ROLE_STAFF'),
        ('ROLE_MANAGER'),
        ('ROLE_DRIVER'),
        ('ROLE_ADMIN');
END
GO
