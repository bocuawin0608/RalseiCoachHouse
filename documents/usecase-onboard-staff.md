# Use Case: Onboard New Staff — UC-S01

## 1. Thông tin cơ bản

| Field | Value |
|-------|-------|
| Use Case ID | UC-S01 |
| Tên | Onboard nhân viên mới (auto-create account + auto-assign role) |
| Actor | System Admin |
| Mô tả | Cho phép Admin tạo nhân viên mới, tự động tạo tài khoản đăng nhập, và tự động gán vai trò. |
| Preconditions | Admin đã đăng nhập. Có role và bến xe trong hệ thống. |

## 2. Backend

**Endpoint:** `POST /api/v1/admin/staff/onboard`

**Request:**
```
OnboardStaffRequest:
  @NotBlank staffName
  @NotBlank phone (dùng làm username)
  email
  dob
  cccd
  @NotBlank staffPosition
  hireDate
  @NotNull ticketAgencyId
  @NotNull roleIds: List<Integer> (ít nhất 1 role)
```

**Response:** 201 Created + `{ staffId: Integer, accountId: Integer, username: String }`

**Logic:**
1. Validate: phone unique (không trùng staff phone và account username)
2. Tạo `Staff` record
3. Tạo `Account`: username = phone, password = `123456` (mặc định, admin tự đổi sau), authProvider = "local"
4. Tạo `AccountRole` cho từng roleId
5. Xuất thông tin staff + account đã tạo

## 3. Frontend

- Thêm "Onboard" button trên StaffListPage (cạnh "Thêm tài khoản" nhưng ngay trong màn hình staff)
- `StaffOnboardModal`: form nhập thông tin nhân viên + chọn role + auto-generate password
- Sau khi onboard thành công: thông báo username/password mặc định

## 4. Business Rules
- phone phải unique trong cả staff.phone và account.username
- Chọn ít nhất 1 role
- Password mặc định: 123456
