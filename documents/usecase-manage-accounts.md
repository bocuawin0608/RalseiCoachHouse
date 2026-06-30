# Use Case: Quản lý tài khoản (Manage Accounts)

> **Project:** CargoTrack (NhaXeTuanMV)
> **Module:** System Administration
> **Audience:** ADMIN

---

## UC-A01: Quản lý tài khoản nhân viên

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-A01 |
| **Name** | Quản lý tài khoản nhân viên |
| **Primary Actors** | Admin (Quản trị hệ thống) |
| **Secondary Actor** | Hệ thống backend (AccountService, StaffService, RoleService, Database), Frontend React |
| **Description** | Admin xem danh sách, tạo mới, cập nhật, kích hoạt/vô hiệu hóa, đặt lại mật khẩu và phân quyền tài khoản nhân viên (local auth) trong hệ thống. Bao gồm quản lý thông tin nhân sự (Staff) đi kèm và gán vai trò (role). |
| **Preconditions** | 1. Admin đã đăng nhập với role `ADMIN`. 2. Hệ thống backend đang hoạt động. 3. Các bảng `account`, `role`, `account_role`, `staff` đã tồn tại trong database. 4. Dữ liệu danh mục `role` đã được seed (ít nhất: `MANAGER`, `TICKET_STAFF`, `TRIP_STAFF`, `ADMIN`). 5. Dữ liệu danh mục `ticket_agency` đã tồn tại (để gán staff theo bến). |
| **Postconditions** | 1. Tài khoản được tạo/cập nhật/xóa thành công trong database. 2. Quyền (role) của tài khoản được cập nhật trong bảng `account_role`. 3. Thông tin nhân sự (Staff) được đồng bộ với tài khoản. 4. Audit trail được ghi lại (createdBy, updatedBy). |

---

### Normal Flow

#### UC-A01-NF1: Xem danh sách tài khoản

1. Admin truy cập trang `/management/manage-accounts`.
2. Hệ thống gọi `GET /api/v1/admin/accounts?page={n}&size={m}`.
3. Backend query bảng `account` kết hợp `account_role` và `role` để lấy danh sách.
4. Backend join thêm bảng `staff` để lấy thông tin nhân sự nếu có (staffName, staffPosition, phone).
5. Backend trả về `Page<AccountListResponse>` gồm: accountId, username, authProvider, isActive, lastLogin, roles (danh sách roleName), staffName, staffPosition, createdAt.
6. Frontend render bảng danh sách với các cột: username, staffName, staffPosition, roles, authProvider, isActive (badge), lastLogin, createdAt, actions (Edit, Reset Password, Toggle Active, Delete).

#### UC-A01-NF2: Lọc danh sách tài khoản

1. Admin nhập từ khóa tìm kiếm (username, staffName, phone) hoặc chọn bộ lọc (role, isActive, staffPosition).
2. Frontend gọi `GET /api/v1/admin/accounts?search={keyword}&role={roleName}&isActive={true/false}&staffPosition={position}`.
3. Backend lọc theo điều kiện, trả về danh sách phân trang.
4. Frontend cập nhật bảng kết quả.

#### UC-A01-NF3: Tạo tài khoản nhân viên mới

1. Admin click nút "Thêm tài khoản" trên trang danh sách.
2. Hệ thống hiển thị form modal gồm 2 phần:
   - **Thông tin tài khoản**: username (SĐT), password, confirm password.
   - **Thông tin nhân sự**: staffName, phone, email, cccd, dob, staffPosition (dropdown: DRIVER, ATTENDANT, TICKET_STAFF, MANAGER), ticketAgencyId (dropdown, nullable), hireDate.
   - **Phân quyền**: checkbox list các role có sẵn (MANAGER, TICKET_STAFF, TRIP_STAFF, ADMIN).
3. Admin điền thông tin và submit.
4. Frontend gọi `POST /api/v1/admin/accounts` với `CreateAccountRequest`.
5. Backend validate:
   - Username chưa tồn tại trong bảng `account`.
   - Password đạt yêu cầu (tối thiểu 6 ký tự).
   - staffPosition hợp lệ (`IN ('DRIVER', 'ATTENDANT', 'TICKET_STAFF', 'MANAGER')`).
   - Nếu staffPosition là `MANAGER`, role bắt buộc có `MANAGER`.
   - ticketAgencyId tồn tại trong bảng `ticket_agency` (nếu không null).
6. Backend tạo record trong bảng `account` với `authProvider = 'local'`, `passwordHash` (bcrypt).
7. Backend tạo record trong bảng `staff` liên kết với `accountId`.
8. Backend tạo record trong bảng `account_role` cho mỗi role được chọn.
9. Backend trả về `AccountResponse` với thông tin đã tạo.
10. Frontend hiển thị thông báo thành công, đóng modal và refresh bảng.

#### UC-A01-NF4: Xem chi tiết tài khoản

1. Admin click vào một tài khoản trong danh sách.
2. Hệ thống gọi `GET /api/v1/admin/accounts/{accountId}`.
3. Backend trả về chi tiết tài khoản + thông tin staff + danh sách roles.
4. Frontend hiển thị drawer/modal chi tiết.

#### UC-A01-NF5: Cập nhật thông tin tài khoản

1. Admin click nút "Sửa" trên một tài khoản.
2. Hệ thống hiển thị form edit với dữ liệu hiện tại.
3. Admin chỉnh sửa thông tin (staffName, phone, email, cccd, dob, staffPosition, ticketAgencyId, isActive).
4. Frontend gọi `PUT /api/v1/admin/accounts/{accountId}` với `UpdateAccountRequest`.
5. Backend validate và cập nhật bảng `account` + `staff`.
6. Backend trả về thông tin đã cập nhật.
7. Frontend thông báo thành công và refresh.

#### UC-A01-NF6: Phân quyền cho tài khoản

1. Admin click nút "Phân quyền" trên một tài khoản.
2. Hệ thống hiển thị modal với danh sách các role (checkbox) — các role hiện tại được pre-checked.
3. Admin thay đổi lựa chọn role.
4. Frontend gọi `PUT /api/v1/admin/accounts/{accountId}/roles` với danh sách roleId mới.
5. Backend xóa hết `account_role` cũ của account, insert các record mới (hoặc sync diff).
6. Backend trả về danh sách role mới.
7. Frontend thông báo thành công.

#### UC-A01-NF7: Đặt lại mật khẩu

1. Admin click nút "Đặt lại mật khẩu" trên một tài khoản.
2. Hệ thống hiển thị form nhập mật khẩu mới + xác nhận.
3. Admin nhập mật khẩu mới và submit.
4. Frontend gọi `PATCH /api/v1/admin/accounts/{accountId}/reset-password` với `{ newPassword }`.
5. Backend hash password mới (bcrypt) và cập nhật vào `passwordHash`.
6. Backend trả về thông báo thành công.
7. Frontend thông báo "Đặt lại mật khẩu thành công".

#### UC-A01-NF8: Kích hoạt/Vô hiệu hóa tài khoản

1. Admin click toggle switch hoặc nút "Vô hiệu hóa" / "Kích hoạt" trên một tài khoản.
2. Frontend gọi `PATCH /api/v1/admin/accounts/{accountId}/toggle-active`.
3. Backend đảo ngược giá trị `isActive` trong bảng `account`:
   - Nếu đang `true` → `false`: tài khoản bị khóa, không thể đăng nhập.
   - Nếu đang `false` → `true`: tài khoản được mở khóa.
4. Backend cũng đồng bộ trạng thái `isActive` trong bảng `staff` tương ứng.
5. Frontend cập nhật badge trạng thái trên bảng.

#### UC-A01-NF9: Xóa tài khoản

1. Admin click nút "Xóa" trên một tài khoản.
2. Hệ thống hiển thị confirm dialog: "Bạn có chắc chắn muốn xóa tài khoản [username]?".
3. Admin xác nhận.
4. Frontend gọi `DELETE /api/v1/admin/accounts/{accountId}`.
5. Backend thực hiện soft-delete hoặc hard-delete:
   - **Hard delete**: Xóa record trong `account_role`, set `accountId = NULL` trong `staff`, xóa record trong `account`.
   - **Soft delete**: Set `isActive = false` và ghi log (khuyến nghị).
6. Backend trả về thông báo thành công.
7. Frontend refresh bảng danh sách.

---

### Alternative Flows

| ID | Name | Description |
|----|------|-------------|
| **A1** | **Không tìm thấy kết quả** | Tại NF1–NF2, nếu không có tài khoản nào khớp filter/search, backend trả về page rỗng. Frontend hiển thị "Không tìm thấy tài khoản nào" và ẩn phân trang. |
| **A2** | **Tạo tài khoản không gắn Staff ngay** | Tại NF3, admin có thể tạo tài khoản trước, sau đó gắn thông tin nhân sự sau. Trường hợp này `accountId` trong bảng `staff` = NULL tam thời. Backend vẫn tạo `account` + `account_role` bình thường. |
| **A3** | **Tạo tài khoản không gán role ngay** | Tại NF3, nếu admin không chọn role nào, backend tạo account với danh sách role rỗng. Tài khoản tồn tại nhưng không có quyền truy cập cho đến khi được gán role sau. |
| **A4** | **Cập nhật thông tin Staff khi chưa có Staff** | Tại NF5, nếu account chưa có staff record (accountId = NULL trong staff), backend tự động tạo mới staff record trước khi cập nhật. |
| **A5** | **Không thể xóa tài khoản Admin cuối cùng** | Tại NF9, nếu chỉ còn 1 tài khoản có role ADMIN trong hệ thống, backend từ chối xóa và trả về lỗi "Không thể xóa tài khoản Admin cuối cùng." |
| **A6** | **Vô hiệu hóa tài khoản Admin cuối cùng** | Tại NF8, nếu chỉ còn 1 tài khoản ADMIN đang active, backend từ chối vô hiệu hóa. |

---

### Exception Flows

| ID | Name | Description |
|----|------|-------------|
| **E1** | **Username đã tồn tại** | Tại NF3, username (SĐT) đã có trong bảng `account`. Backend throw `DuplicateKeyException` hoặc `BusinessRuleException("Username đã tồn tại")`. Frontend hiển thị lỗi dưới field username: "Số điện thoại này đã được sử dụng." |
| **E2** | **Mật khẩu không đạt yêu cầu** | Tại NF3/NF7, password quá ngắn hoặc không đủ mạnh. Backend throw `ValidationException`. Frontend hiển thị: "Mật khẩu phải có ít nhất 6 ký tự." |
| **E3** | **Staff position không hợp lệ** | Tại NF3/NF5, `staffPosition` không thuộc danh sách cho phép (`DRIVER`, `ATTENDANT`, `TICKET_STAFF`, `MANAGER`). Backend throw `ValidationException`. |
| **E4** | **ticketAgencyId không tồn tại** | Tại NF3/NF5, `ticketAgencyId` không tìm thấy trong bảng `ticket_agency`. Backend throw `ResourceNotFoundException`. |
| **E5** | **Tài khoản không tồn tại** | Tại NF4–NF9, `accountId` không tồn tại trong bảng `account`. Backend throw `ResourceNotFoundException("Tài khoản không tồn tại")`. Frontend hiển thị thông báo lỗi. |
| **E6** | **Không thể xóa tài khoản đã có dữ liệu giao dịch** | Tại NF9, account đã được sử dụng trong các bảng giao dịch (ví dụ: `passenger_ticket.soldBy`, `cargo_ticket.soldBy`). Backend không cho hard-delete, chỉ cho phép soft-delete (vô hiệu hóa). Nếu backend cố tình xóa, vi phạm FK constraint. |
| **E7** | **Token hết hạn (401)** | Trong tất cả các request, JWT token của admin hết hạn. Backend trả về 401 Unauthorized. Frontend redirect về trang đăng nhập. |
| **E8** | **Không có quyền (403)** | Tài khoản không có role ADMIN cố tình truy cập API. Backend trả về 403 Forbidden. Frontend hiển thị "Bạn không có quyền thực hiện thao tác này." |
| **E9** | **Lỗi hệ thống (500)** | Server gặp lỗi nội bộ (database connection failed, NPE, ...). Backend trả về 500. Frontend hiển thị "Có lỗi xảy ra, vui lòng thử lại sau." |

---

## UC-A01-Supplemental: Dữ liệu & API Endpoints

### Database Schema (relevant tables)

```sql
-- Bảng tài khoản
account (accountId, username, passwordHash, firebaseUid, authProvider, isActive, lastLogin, createdAt, createdBy, updatedAt, updatedBy)

-- Bảng vai trò
role (roleId, roleName, isActive, createdAt, createdBy, updatedAt, updatedBy)

-- Bảng liên kết tài khoản - vai trò (many-to-many)
account_role (accountRoleId, accountId, roleId)

-- Bảng nhân sự
staff (staffId, accountId, ticketAgencyId, staffName, phone, email, dob, cccd, staffPosition, hireDate, isActive, createdAt, createdBy, updatedAt, updatedBy)
```

### API Endpoints (Backend)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/api/v1/admin/accounts` | Danh sách tài khoản (phân trang + search + filter) |
| `GET` | `/api/v1/admin/accounts/{accountId}` | Chi tiết tài khoản |
| `POST` | `/api/v1/admin/accounts` | Tạo tài khoản mới + staff + role |
| `PUT` | `/api/v1/admin/accounts/{accountId}` | Cập nhật thông tin tài khoản + staff |
| `PUT` | `/api/v1/admin/accounts/{accountId}/roles` | Phân quyền (sync danh sách role) |
| `PATCH` | `/api/v1/admin/accounts/{accountId}/reset-password` | Đặt lại mật khẩu |
| `PATCH` | `/api/v1/admin/accounts/{accountId}/toggle-active` | Kích hoạt/Vô hiệu hóa |
| `DELETE` | `/api/v1/admin/accounts/{accountId}` | Xóa tài khoản |

### DTOs (Data Transfer Objects)

```
CreateAccountRequest {
    username: String (SĐT)
    password: String
    confirmPassword: String
    staffName: String
    phone: String
    email: String (optional)
    cccd: String (optional)
    dob: LocalDate (optional)
    staffPosition: String (DRIVER | ATTENDANT | TICKET_STAFF | MANAGER)
    ticketAgencyId: Integer (optional)
    hireDate: LocalDate
    roleIds: List<Integer>
}

UpdateAccountRequest {
    staffName: String
    phone: String
    email: String (optional)
    cccd: String (optional)
    dob: LocalDate (optional)
    staffPosition: String
    ticketAgencyId: Integer (optional)
    hireDate: LocalDate
    isActive: Boolean
}

AccountListResponse {
    accountId: Integer
    username: String
    authProvider: String
    isActive: Boolean
    lastLogin: LocalDateTime
    roles: List<String>
    staffId: Integer (nullable)
    staffName: String (nullable)
    staffPosition: String (nullable)
    createdAt: LocalDateTime
}

AccountDetailResponse {
    accountId: Integer
    username: String
    authProvider: String
    isActive: Boolean
    lastLogin: LocalDateTime
    roles: List<RoleResponse>
    staff: StaffResponse (nullable)
    createdAt: LocalDateTime
    createdBy: Integer
    updatedAt: LocalDateTime
    updatedBy: Integer
}
```

### Frontend Routes

| Route | Component | Mô tả |
|-------|-----------|-------|
| `/management/manage-accounts` | `AccountListPage` | Danh sách tài khoản + CRUD |
| Guard | `RoleGuard allowedRoles={['ADMIN']}` | Chỉ ADMIN mới truy cập được |

---

## Ma trận Use Case - UC-A01

| Tính năng | ADMIN | Hệ thống |
|-----------|-------|----------|
| Xem danh sách tài khoản | ✓ | ✓ |
| Tìm kiếm/lọc tài khoản | ✓ | ✓ |
| Tạo tài khoản + Staff | ✓ | ✓ |
| Xem chi tiết tài khoản | ✓ | ✓ |
| Cập nhật thông tin | ✓ | ✓ |
| Phân quyền (gán role) | ✓ | ✓ |
| Đặt lại mật khẩu | ✓ | ✓ |
| Kích hoạt/Vô hiệu hóa | ✓ | ✓ |
| Xóa tài khoản | ✓ | ✓ |
| Audit trail (createdBy/updatedBy) | | ✓ |

---

## Ràng buộc (Business Rules)

1. **Username duy nhất** — `username` trong bảng `account` phải là UNIQUE, không thể tạo 2 tài khoản cùng username.
2. **Password bắt buộc với local auth** — Khi `authProvider = 'local'`, `passwordHash` không được NULL.
3. **Role hợp lệ** — `roleName` phải thuộc danh sách: `MANAGER`, `TICKET_STAFF`, `TRIP_STAFF`, `ADMIN`, `CUSTOMER` (customer không do admin quản lý).
4. **Admin bảo vệ** — Không thể xóa hoặc vô hiệu hóa tài khoản ADMIN cuối cùng trong hệ thống.
5. **Audit trail** — Mọi thao tác tạo/sửa đều ghi `createdBy` / `updatedBy` là `accountId` của admin thực hiện.
6. **Soft-delete ưu tiên** — Khi xóa tài khoản đã có dữ liệu giao dịch, chỉ cho phép soft-delete (vô hiệu hóa), không hard-delete.
