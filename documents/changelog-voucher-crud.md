# Changelog: Voucher CRUD

> **Branch:** `feature/voucher-crud`

---

## 1. Tính năng

### Backend — Quản lý voucher (mã giảm giá)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `POST` | `/api/v1/vouchers` | Tạo voucher mới |
| `GET` | `/api/v1/vouchers` | Danh sách voucher (phân trang, lọc) |
| `GET` | `/api/v1/vouchers/{id}` | Xem chi tiết voucher |
| `PUT` | `/api/v1/vouchers/{id}` | Cập nhật voucher |
| `DELETE` | `/api/v1/vouchers/{id}` | Xóa voucher |
| `GET` | `/api/v1/vouchers/metrics` | Thống kê: còn hiệu lực, sắp hết hạn, đã dùng |

### Frontend — Staff (Manager)
| Route | Page | Chức năng |
|-------|------|-----------|
| `/management/vouchers` | VoucherListPage | Danh sách + lọc + xóa |
| `/management/vouchers/create` | CreateVoucherPage | Tạo voucher mới |
| `/management/vouchers/:id/edit` | EditVoucherPage | Sửa voucher |

---

## 2. File thay đổi

### 2.1 Backend — File mới

| File | Vai trò |
|------|---------|
| `model/Voucher.java` | Entity voucher (voucherCode, discountValue, discountType, maxDiscountValue, minOrderValue, start/endEffectiveDate, usageLimit, usedCount, ...) |
| `repository/VoucherRepository.java` | JPA repository với query methods |
| `controller/VoucherController.java` | REST endpoints CRUD + metrics |
| `service/VoucherService.java` | Interface service |
| `service/impl/VoucherServiceImpl.java` | Implementation: validate, create, update, delete, metrics |
| `dto/request/voucher/CreateVoucherRequest.java` | Request DTO tạo voucher |
| `dto/request/voucher/UpdateVoucherRequest.java` | Request DTO cập nhật voucher |
| `dto/request/voucher/VoucherFilterRequest.java` | Request DTO lọc (keyword, status, sort, page, size) |
| `dto/response/voucher/VoucherResponse.java` | Response DTO chi tiết voucher |
| `dto/response/voucher/VoucherListItemResponse.java` | Response DTO item trong danh sách |
| `dto/response/voucher/VoucherMetricsResponse.java` | Response DTO thống kê voucher |

### 2.2 Backend — File sửa

| File | Thay đổi |
|------|----------|
| `db/ddl.sql` | Thêm bảng `voucher` |
| `db/fakedata.sql` | Thêm seed data voucher |
| `config/SecurityConfig.java` | Cập nhật phân quyền endpoint |

### 2.3 Frontend (Staff) — File mới

| File | Vai trò |
|------|---------|
| `features/vouchers/index.js` | Export module |
| `features/vouchers/api/voucherApi.js` | Gọi API voucher |
| `features/vouchers/hooks/useVouchers.js` | Hook quản lý state, filter, pagination |
| `features/vouchers/routes/VoucherRoutes.jsx` | Nested routes cho voucher |
| `features/vouchers/components/VoucherTable.jsx` | Bảng danh sách voucher |
| `features/vouchers/components/VoucherFilter.jsx` | Thanh lọc (keyword, trạng thái) |
| `features/vouchers/components/VoucherForm.jsx` | Form tạo/sửa voucher |
| `features/vouchers/components/VoucherDeleteModal.jsx` | Modal xác nhận xóa |
| `features/vouchers/components/VoucherDetailModal.jsx` | Modal xem chi tiết |
| `features/vouchers/VouchersPage.css` | Style |
| `pages/manager/vouchers/VoucherListPage.jsx` | Trang danh sách voucher |
| `pages/manager/vouchers/CreateVoucherPage.jsx` | Trang tạo voucher |
| `pages/manager/vouchers/EditVoucherPage.jsx` | Trang sửa voucher |

### 2.4 Frontend (Staff) — File sửa

| File | Thay đổi |
|------|----------|
| `routes/AppRouter.jsx` | Thêm routes `/management/vouchers/*` |
| `components/layout/DesktopStaffLayout/SideBar.jsx` | Thêm menu item "Voucher" |

---

## 3. Entity: Voucher

| Column | Type | Ràng buộc |
|--------|------|-----------|
| voucherId | INT (PK) | Identity |
| voucherCode | VARCHAR(50) | UNIQUE, NOT NULL |
| discountValue | DECIMAL(15,2) | NOT NULL |
| discountType | VARCHAR(10) | `PERCENTAGE` hoặc `FIXED` |
| maxDiscountValue | DECIMAL(15,2) | Giới hạn tối đa (nếu %) |
| minOrderValue | DECIMAL(15,2) | Giá trị đơn hàng tối thiểu |
| startEffectiveDate | DATETIME | Ngày bắt đầu hiệu lực |
| endEffectiveDate | DATETIME | Ngày kết thúc |
| usageLimit | INT | Số lần dùng tối đa |
| usedCount | INT | Số lần đã dùng |

### Business Rules
1. `discountType` = `PERCENTAGE`: `discountValue ≤ 100`, `maxDiscountValue` là cap
2. `discountType` = `FIXED`: `discountValue` là số tiền cố định, bỏ qua `maxDiscountValue`
3. Trạng thái: `UPCOMING` (chưa đến hạn) / `ACTIVE` (đang hiệu lực) / `EXPIRED` (hết hạn) / `EXHAUSTED` (hết lượt)
4. Không xóa cứng (hard delete) — API `DELETE` xóa thật khỏi DB

---

## 4. API Endpoints

### POST `/api/v1/vouchers` — Tạo voucher

**Request:**
```json
{
  "voucherCode": "SUMMER10",
  "discountValue": 10,
  "discountType": "PERCENTAGE",
  "maxDiscountValue": 50000,
  "minOrderValue": 200000,
  "startEffectiveDate": "2026-06-01T00:00:00",
  "endEffectiveDate": "2026-08-31T23:59:59",
  "usageLimit": 100
}
```

### GET `/api/v1/vouchers/metrics` — Thống kê

**Response:**
```json
{
  "active": 5,
  "expiringSoon": 2,
  "exhausted": 3,
  "total": 15
}
```
