# Use Case: Quản lý Nhân Viên (Manage Staff) — UC-STF01

## 1. Thông tin cơ bản

| Field | Value |
|-------|-------|
| Use Case ID | UC-STF01 |
| Tên | Quản lý nhân viên (Manage Staff) |
| Actor | System Admin |
| Mô tả | Cho phép Admin xem danh sách, tìm kiếm, lọc, xem chi tiết và cập nhật thông tin / trạng thái nhân viên. |
| Trigger | Admin chọn menu "Quản lý nhân viên" |
| Preconditions | Admin đã đăng nhập, token hợp lệ. |

## 2. Entity: Staff

| Column | Type | Notes |
|--------|------|-------|
| staffId | int (PK, auto-increment) | |
| accountId | int (FK → Account, unique, nullable) | Tài khoản đăng nhập |
| ticketAgencyId | int (FK → TicketAgency, nullable) | Bến xe trực thuộc |
| staffName | varchar (not null) | Họ tên nhân viên |
| phone | varchar (not null) | Số điện thoại |
| email | varchar | Email |
| dob | date | Ngày sinh |
| cccd | varchar | Số CCCD/CMND |
| staffPosition | varchar (not null) | Chức vụ (DRIVER, TICKET_STAFF, TRIP_STAFF, ...) |
| hireDate | date (not null) | Ngày vào làm |
| isActive | boolean (default true) | Trạng thái hoạt động |
| createdAt | datetime | Audit (BaseEntity) |
| createdBy | int | Audit |
| updatedAt | datetime | Audit |
| updatedBy | int | Audit |

## 3. Backend Endpoints

### Base: `/api/v1/admin/staff`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | ADMIN | Filter + paginate staff |
| GET | `/{id}` | ADMIN | Get detail (includes account info, ticket agency name) |
| PUT | `/{id}` | ADMIN | Update staff profile + status |
| PATCH | `/{id}/toggle-active` | ADMIN | Toggle isActive |

### DTOs

**StaffFilterRequest:** search (staffName, phone, email, cccd), isActive, staffPosition, ticketAgencyId

**UpdateStaffRequest:**
```java
record UpdateStaffRequest(
    @NotBlank String staffName, String phone, String email,
    LocalDate dob, String cccd, String staffPosition,
    LocalDate hireDate, Integer ticketAgencyId, Boolean isActive
)
```

**StaffListResponse:**
```java
record StaffListResponse(
    Integer staffId, String staffName, String phone, String email,
    String cccd, String staffPosition, Integer ticketAgencyId,
    String ticketAgencyName, String username,
    @JsonProperty("active") boolean isActive,
    LocalDateTime createdAt
)
```

**StaffDetailResponse:** same + accountId, dob, hireDate, account isActive + audit fields

## 4. Business Rules

1. **staffName**, **phone**, **staffPosition**, **hireDate** bắt buộc.
2. **accountId** unique — mỗi nhân viên gán đúng một tài khoản (nếu có).
3. **isActive** mặc định true khi tạo. Toggle để vô hiệu hóa/kích hoạt.
4. **Search** tìm theo staffName, phone, email, cccd.
5. **Filter** theo staffPosition và ticketAgencyId (dropdown).
6. **Onboarding** (tạo mới staff + account + role) — sẽ triển khai ở phase sau.

## 5. Frontend

### Menu / Route
- SideBar: "Quản lý nhân viên" dưới nhóm ADMIN (sau "Quản lý bến xe")
- Route: `/management/manage-staff`

### Components (same pattern as manage-ticket-agencies)
- StaffFilter, StaffTable, StaffUpdateModal, StaffDetailModal, StaffListPage
- Filter includes search, staffPosition dropdown, ticketAgency dropdown, isActive select

### Features
- Server-side pagination, search, filter by position/agency/status
- Loading/empty/error states
- Confirm before toggle-active
- Refresh after mutations
- `@JsonProperty("active")`, error handling pattern consistent with all other modules
