# Use Case: Quản lý Bến Xe (Manage Ticket Agencies) — UC-TA01

## 1. Thông tin cơ bản

| Field | Value |
|-------|-------|
| Use Case ID | UC-TA01 |
| Tên | Quản lý bến xe (Manage Ticket Agencies) |
| Actor | System Admin |
| Mô tả | Cho phép Admin xem danh sách, tìm kiếm, thêm mới, cập nhật, vô hiệu hóa và xóa bến xe. |
| Trigger | Admin chọn menu "Quản lý bến xe" |
| Preconditions | Admin đã đăng nhập, token hợp lệ. |

## 2. Entity: TicketAgency

| Column | Type | Notes |
|--------|------|-------|
| ticketAgencyId | int (PK, auto-increment) | |
| stopPointId | int (FK → CoachStop, not null) | Điểm dừng xe |
| ticketAgencyName | varchar (not null) | Tên bến xe |
| isActive | boolean (default true) | Soft-delete flag |
| createdAt | datetime | Audit (BaseEntity) |
| createdBy | int | Audit |
| updatedAt | datetime | Audit |
| updatedBy | int | Audit |

## 3. Backend Endpoints

### Base: `/api/v1/admin/ticket-agencies`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | ADMIN | Filter + paginate ticket agencies |
| GET | `/{id}` | ADMIN | Get detail |
| POST | `/` | ADMIN | Create |
| PUT | `/{id}` | ADMIN | Update |
| PATCH | `/{id}/toggle-active` | ADMIN | Toggle isActive |
| DELETE | `/{id}` | ADMIN | Delete (only if no staff assigned) |
| GET | `/coach-stop-dropdown` | ADMIN | List active coach stops for dropdown |

### DTOs

**TicketAgencyFilterRequest:** search, isActive
**CreateTicketAgencyRequest:** @NotBlank ticketAgencyName, @NotNull stopPointId
**UpdateTicketAgencyRequest:** @NotBlank ticketAgencyName, @NotNull stopPointId, Boolean isActive

**TicketAgencyListResponse:**
```java
record TicketAgencyListResponse(
    Integer ticketAgencyId, String ticketAgencyName,
    Integer stopPointId, String stopPointName,
    @JsonProperty("active") boolean isActive,
    Long staffCount,
    LocalDateTime createdAt
)
```

**TicketAgencyDetailResponse:** same + audit fields (createdBy, updatedAt, updatedBy)

## 4. Business Rules

1. **ticketAgencyName** bắt buộc, max 200 ký tự.
2. **stopPointId** bắt buộc, phải là ID của CoachStop hợp lệ.
3. **isActive**: Mặc định true khi tạo. Toggle để vô hiệu hóa/kích hoạt.
4. **Delete**: Chỉ cho phép xóa bến xe không có nhân viên (staff) nào đang gán. Nếu có staff → 409.
5. **Tên bến xe**: không yêu cầu unique (có thể trùng tên ở các điểm dừng khác nhau).

## 5. Frontend

### Menu / Route
- SideBar: "Quản lý bến xe" dưới nhóm ADMIN
- Route: `/management/manage-ticket-agencies`

### Components (same pattern as manage-customers)
- TicketAgencyFilter, TicketAgencyTable, TicketAgencyCreateModal, TicketAgencyUpdateModal, TicketAgencyDetailModal, TicketAgencyListPage
- Create/Update modals include a CoachStop dropdown (fetched from `/coach-stop-dropdown`)

### Features
- Server-side pagination, search (name), filter by active/inactive
- Loading/empty/error states
- Confirm before delete/toggle
- Refresh after mutations
- `@JsonProperty("active")`, error handling pattern consistent with all other modules
