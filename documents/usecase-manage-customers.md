# Use Case: Quản lý Khách Hàng (Manage Customers) — UC-C01

## 1. Thông tin cơ bản

| Field | Value |
|-------|-------|
| Use Case ID | UC-C01 |
| Tên | Quản lý khách hàng (Manage Customers) |
| Actor | System Admin |
| Mô tả | Cho phép Admin xem danh sách, tìm kiếm, xem chi tiết, thêm mới, cập nhật, vô hiệu hóa và xóa khách hàng. |
| Trigger | Admin chọn menu "Quản lý khách hàng" |
| Preconditions | Admin đã đăng nhập, token hợp lệ. |

## 2. Entity: Customer

| Column | Type | Notes |
|--------|------|-------|
| customerId | int (PK, auto-increment) | |
| accountId | Integer (unique) | FK to Account; null nếu chưa liên kết |
| customerName | varchar (not null) | Họ tên khách hàng |
| phone | varchar | |
| email | varchar | |
| dob | date | |
| isActive | boolean (default true) | Soft-delete flag |
| createdAt | datetime | Audit (BaseEntity) |
| createdBy | int | Audit |
| updatedAt | datetime | Audit |
| updatedBy | int | Audit |

## 3. Backend Endpoints

### Base: `/api/v1/admin/customers`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | ADMIN | Filter + paginate customers (search: name, phone, email) |
| GET | `/{customerId}` | ADMIN | Get customer detail |
| POST | `/` | ADMIN | Create customer |
| PUT | `/{customerId}` | ADMIN | Update customer |
| PATCH | `/{customerId}/toggle-active` | ADMIN | Toggle isActive |
| DELETE | `/{customerId}` | ADMIN | Delete customer (only if no orders) |

### DTOs

**CustomerFilterRequest:**
```
search: String
isActive: Boolean
```

**CreateCustomerRequest:**
```
@NotBlank customerName
phone
email
dob: LocalDate
```

**UpdateCustomerRequest:**
```
@NotBlank customerName
phone
email
dob: LocalDate
Boolean isActive
```

**CustomerListResponse:**
```
customerId: Integer
customerName: String
phone: String
email: String
dob: LocalDate
@JsonProperty("active") isActive: boolean
createdAt: LocalDateTime
```

**CustomerDetailResponse:**
```
customerId: Integer
customerName: String
phone: String
email: String
dob: LocalDate
@JsonProperty("active") isActive: boolean
createdAt: LocalDateTime
createdBy: Integer
updatedAt: LocalDateTime
updatedBy: Integer
```

## 4. Business Rules

1. **customerName** bắt buộc, max 100 ký tự.
2. **phone** không bắt buộc, nhưng nếu có thì phải là duy nhất (trừ record hiện tại khi update). Cho phép null.
3. **isActive**: Mặc định true khi tạo mới. Toggle để vô hiệu hóa hoặc kích hoạt.
4. **Delete**: Chỉ cho phép xóa khách hàng không có vé (passenger_ticket). Nếu đã có vé → trả về 409 với thông báo.
5. **Không được xóa Admin** — không áp dụng vì Customer không phải Admin role.
6. **accountId**: Khi tạo bằng form admin, customer không cần tài khoản login; accountId = null. Nếu sau đó khách đăng ký tài khoản riêng thì sẽ liên kết qua AuthService luồng hiện tại.

## 5. Frontend

### Menu
- SideBar: thêm mục "Quản lý khách hàng" dưới nhóm ADMIN (cùng cấp với "Quản lý tài khoản" và "Quản lý vai trò")
- Route: `/management/manage-customers`

### Components (mirror manage-roles structure)

| Component | Description |
|-----------|-------------|
| CustomerFilter | Search + isActive filter |
| CustomerTable | Table with name, phone, email, active badge, actions |
| CustomerCreateModal | Create form: name, phone, email, dob |
| CustomerUpdateModal | Update form: name, phone, email, dob, isActive switch |
| CustomerDetailModal | Detail view with audit info |
| CustomerListPage | Page combining filter, table, pagination, modals |

### Features
- Server-side pagination (Spring Pageable)
- Search by name/phone/email
- Filter by active/inactive
- Loading states, empty state, error handling
- Confirm before delete/toggle
- Refresh after every mutation
- Error messages from backend (validation + business rules)
- `@JsonProperty("active")` pattern same as Accounts/Roles

## 6. Permissions

- Tất cả endpoint yêu cầu `hasRole('ADMIN')`
- Controller-level: `@PreAuthorize("hasRole('ADMIN')")`
