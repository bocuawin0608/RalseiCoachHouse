USE VeXeDB;
GO

SET NOCOUNT ON;
PRINT N'Đang tiến hành Seed Data...';

-- ============================================================================
-- LEVEL 1: STRONG ENTITIES (Danh mục gốc)
-- ============================================================================

-- 1. ACCOUNT
INSERT INTO [account] (username, passwordHash, isActive) VALUES 
('0901111111', 'hash_admin', 1),     -- 1: Admin
('0902222222', 'hash_manager', 1),   -- 2: Manager
('0903333333', 'hash_ticket', 1),    -- 3: Ticket Staff (NV bán vé)
('0904444444', 'hash_trip', 1),      -- 4: Trip Staff (Tài xế/Phụ xe)
('0905555555', 'hash_cus1', 1),      -- 5: Customer (Khách hàng)
('0906666666', 'hash_cus2', 1);      -- 6: Customer (Khách hàng bom vé)

-- 2. ROLE
INSERT INTO [role] (roleName) VALUES 
('admin'), ('manager'), ('ticket_staff'), ('trip_staff'), ('customer');

-- 3. VOUCHER
INSERT INTO [voucher] (voucherCode, discountValue, startEffectiveDate, endEffectiveDate, discountType, maxDiscountValue, minOrderValue, usageLimit) VALUES 
('HE2026', 10.00, '2026-01-01', '2026-12-31', 'percent', 50000.00, 200000.00, 100), 
('GIAM50K', 50000.00, '2026-01-01', '2026-12-31', 'fixed', 50000.00, 0.00, 50);

-- 4. COACH_STOP: Setup Văn phòng Hà Nội -> Quảng Bình
INSERT INTO [coach_stop] (stopPointName, address) VALUES 
(N'Văn phòng Nước Ngầm', N'Km8 Giải Phóng, Hoàng Mai, Hà Nội'),             -- 1: VP Start
(N'Văn phòng Ninh Bình', N'123 Trần Hưng Đạo, TP Ninh Bình'),               -- 2: Trạm dừng
(N'Văn phòng Vinh', N'78 Nguyễn Trãi, TP Vinh, Nghệ An'),                   -- 3: Trạm dừng
(N'Văn phòng Đồng Hới', N'156 Lý Thường Kiệt, TP Đồng Hới, Quảng Bình');    -- 4: VP End

-- 5. ROUTE: Tuyến đường
INSERT INTO [route] (routeName, totalKilometers, totalMinutes) VALUES 
(N'Hà Nội - Quảng Bình', 500.00, 600); -- ~500km, 10 tiếng

-- 6. SEAT_LAYOUT: Cấu hình 3 loại xe mới
INSERT INTO [seat_layout] (seatLayoutName, totalSeat) VALUES 
(N'Xe Limousine 20 chỗ', 20),      -- 1
(N'Xe Luxury 32 chỗ', 32),         -- 2
(N'Xe truyền thống 38 chỗ', 38);   -- 3

-- 7. CARGO_TYPE
INSERT INTO [cargo_type] (cargoTypeName) VALUES 
(N'Hàng khô / Thùng Carton'), (N'Xe máy'), (N'Hàng dễ vỡ');


-- ============================================================================
-- LEVEL 2: ASSOCIATIVE & WEAK ENTITIES
-- ============================================================================

-- 8. ACCOUNT_ROLE: Phân quyền theo ID mới
INSERT INTO [account_role] (accountId, roleId) VALUES 
(1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 5);

-- 9. CUSTOMER
INSERT INTO [customer] (accountId, customerName, phone, email) VALUES 
(5, N'Nguyễn Văn Online', '0905555555', 'nvo@gmail.com'),  -- 1
(6, N'Trần Thị Pending', '0906666666', 'ttp@gmail.com'),   -- 2
(NULL, N'Lê Vãng Lai', '0999999999', NULL);                -- 3: Khách mua tại VP

-- 10. TICKET_AGENCY: VP Nhà xe
INSERT INTO [ticket_agency] (stopPointId, ticketAgencyName) VALUES 
(1, N'Văn phòng Nước Ngầm - Trụ sở'), 
(4, N'Văn phòng Đồng Hới - Chi nhánh');

-- 11. STAFF 
INSERT INTO [staff] (accountId, ticketAgencyId, staffName, phone, staffPosition, hireDate) VALUES 
(1, 1, N'Admin Hệ Thống', '0901111111', 'Manager', '2023-01-01'),        -- 1 (Admin dùng chung mác Manager dưới DB)
(2, 1, N'Quản Lý VP Nước Ngầm', '0902222222', 'Manager', '2024-01-01'),  -- 2
(3, 1, N'Bán Vé Hà Nội', '0903333333', 'Ticket Staff', '2025-01-01'),    -- 3
(4, NULL, N'Tài Xế Tuấn', '0904444444', 'Driver', '2026-01-01'),         -- 4
(NULL, NULL, N'Phụ Xe Hùng', '0988888888', 'Attendant', '2026-01-01');   -- 5

-- 12. ROUTE_STOP: Lộ trình chi tiết Hà Nội - Quảng Bình
INSERT INTO [route_stop] (routeId, stopPointId, stopOrder, kilometersFromStart, minutesFromStart) VALUES 
(1, 1, 1, 0.00, 0),         -- HN
(1, 2, 2, 90.00, 120),      -- NB
(1, 3, 3, 290.00, 360),     -- Vinh
(1, 4, 4, 500.00, 600);     -- QB

-- 13. SEAT
INSERT INTO [seat] (seatLayoutId, seatCode, rowIndex, colIndex) VALUES 
(1, 'L01', 1, 1), (1, 'L02', 1, 2), -- Limousine 20
(2, 'LX01', 1, 1), (2, 'LX02', 1, 2), -- Luxury 32
(3, 'T01', 1, 1), (3, 'T02', 1, 2); -- Truyền thống 38

-- 14. SEAT_LAYOUT_PRICE
INSERT INTO [seat_layout_price] (seatLayoutId, seatPrice, startEffectiveDate, endEffectiveDate) VALUES 
(1, 550000.00, '2026-01-01', '2099-12-31'), -- Limousine
(2, 450000.00, '2026-01-01', '2099-12-31'), -- Luxury
(3, 300000.00, '2026-01-01', '2099-12-31'); -- Truyền thống

-- 15. CARGO_TYPE_PRICE
INSERT INTO [cargo_type_price] (cargoTypeId, unit, pricePerUnit, startEffectiveDate, endEffectiveDate) VALUES 
(1, 'kg', 5000.00, '2026-01-01', '2099-12-31'),
(2, 'chiếc', 350000.00, '2026-01-01', '2099-12-31');

-- 16. COACH
INSERT INTO [coach] (seatLayoutId, licensePlate, status) VALUES 
(1, '29B-LIMO.01', 'active'),   -- 1: Xe Limousine chạy HN-QB
(2, '29B-LUX.02', 'active');    -- 2: Xe Luxury dự phòng


-- ============================================================================
-- LEVEL 3: OPERATIONAL ENTITIES
-- ============================================================================

-- 17. TRIP
INSERT INTO [trip] (routeId, coachId, departureTime, status, driverId, attendantId) VALUES 
(1, 1, DATEADD(day, 1, GETDATE()), 'scheduled', 4, 5); -- Chuyến HN-QB ngày mai


-- ============================================================================
-- LEVEL 4: TRANSACTIONAL ENTITIES (Tickets)
-- ============================================================================
use VeXeDB;
select * from trip
-- 18. PASSENGER_TICKET
INSERT INTO [passenger_ticket] (customerId, tripId, voucherId, soldBy, ticketCode, totalPrice, pickupStopId, dropoffStopId, status) VALUES 
(1, 1, NULL, NULL, 'TK_ONL_COMPLETED', 550000.00, 1, 4, 'confirmed'), -- 1: Khách tự mua Online, ĐÃ thanh toán
(2, 1, NULL, NULL, 'TK_ONL_PENDING', 550000.00, 1, 4, 'pending'),     -- 2: Khách đang giữ chỗ Online, CHƯA thanh toán
(NULL, 1, NULL, 3, 'TK_OFF_COMPLETED', 550000.00, 1, 4, 'confirmed'); -- 3: Khách vãng lai mua tại VP Nước Ngầm

-- 19. CARGO_TICKET
INSERT INTO [cargo_ticket] (tripId, customerId, senderName, senderPhone, senderCccd, receiverName, receiverPhone, receiverCccd, ticketCode, totalPrice, feePayer, codAmount, pickupStopId, dropoffStopId, status, soldBy) VALUES 
(1, 1, N'Nguyễn Văn Online', '0905555555', '001', N'Người Thân', '0123', '002', 'CG_PRE_01', 50000.00, 'sender', 0, 1, 4, 'received', 3), -- 1: Đơn gửi thường, trả phí ngay
(1, NULL, N'Lê Gửi', '091', '003', N'Trần Nhận', '092', '004', 'CG_COD_01', 350000.00, 'receiver', 2000000.00, 1, 4, 'received', 3);  -- 2: Đơn gửi COD, thu hộ 2tr


-- ============================================================================
-- LEVEL 5: SUB-DETAILS & FINANCIALS
-- ============================================================================

-- 20. PASSENGER_TICKET_DETAIL
INSERT INTO [passenger_ticket_detail] (passengerTicketId, seatId, fullName, phone, dob, cccd, price, status, expiredAt) VALUES 
(1, 1, N'Nguyễn Văn Online', '0905555555', '1990-01-01', '001', 550000.00, 'confirmed', NULL),
(2, 2, N'Trần Thị Pending', '0906666666', '1995-05-05', '002', 550000.00, 'pending', DATEADD(minute, 15, GETDATE())),
(3, 3, N'Lê Vãng Lai', '0999999999', '1980-08-08', '003', 550000.00, 'confirmed', NULL);

-- 21. CARGO_TICKET_DETAIL
INSERT INTO [cargo_ticket_detail] (cargoTicketId, cargoTypePriceId, description, quantity, weightKg, calculatedPrice) VALUES 
(1, 1, N'Thùng đặc sản HN', 1, 10.00, 50000.00), 
(2, 2, N'Xe máy gửi vào Quảng Bình', 1, 120.00, 350000.00); 

-- 22. PAYMENT (Mọi ticket sinh ra đều có payment theo sát trạng thái)
INSERT INTO [payment] (passengerTicketId, cargoTicketId, amount, paymentMethod, transactionId, status, paymentTime) VALUES 
-- Thanh toán cho Passenger Tickets
(1, NULL, 550000.00, 'vnpay', 'VNP_123', 'completed', GETDATE()),     
(2, NULL, 550000.00, 'vnpay', 'VNP_WAIT', 'pending', NULL),           
(3, NULL, 550000.00, 'cash', 'CASH_01', 'completed', GETDATE()),      

-- Thanh toán cho Cargo Tickets
(NULL, 1, 50000.00, 'cash', 'CASH_CG_01', 'completed', GETDATE()),    
(NULL, 2, 350000.00, 'cash', 'CASH_CG_02', 'pending', NULL);          

PRINT N'Hoàn tất Seed Data!';
GO

--===================================================
USE VeXeDB;

SELECT  r.routeName, sl.seatLayoutName,t.[status] ,t.departureTime FROM [trip] t JOIN route R 
ON t.routeId = R.routeId
JOIN coach C
    ON t.coachId = C.coachId
JOIN seat_layout SL
    ON C.seatLayoutId = SL.seatLayoutId 
WHERE departureTime BETWEEN '2026-05-30 00:00:00' AND '2026-05-30 23:59:59';
