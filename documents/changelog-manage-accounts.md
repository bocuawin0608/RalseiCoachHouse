# Changelog: Quản lý tài khoản (Manage Accounts)

> **Use Case:** UC-A01 — Quản lý tài khoản nhân viên
> **Role:** ADMIN

---

## 1. Tính năng

### Backend — Quản lý tài khoản (Admin)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/api/v1/admin/accounts` | Danh sách tài khoản (phân trang + lọc: search, role, isActive, staffPosition, authProvider) |
| `GET` | `/api/v1/admin/accounts/roles` | Danh sách tất cả role có sẵn |
| `GET` | `/api/v1/admin/accounts/{accountId}` | Xem chi tiết tài khoản (kèm roles + staff) |
| `POST` | `/api/v1/admin/accounts` | Tạo tài khoản mới + thông tin nhân sự + phân quyền |
| `PUT` | `/api/v1/admin/accounts/{accountId}` | Cập nhật thông tin nhân sự + trạng thái |
| `PUT` | `/api/v1/admin/accounts/{accountId}/roles` | Phân quyền (sync danh sách role) |
| `PATCH` | `/api/v1/admin/accounts/{accountId}/reset-password` | Đặt lại mật khẩu |
| `PATCH` | `/api/v1/admin/accounts/{accountId}/toggle-active` | Kích hoạt/Vô hiệu hóa |
| `DELETE` | `/api/v1/admin/accounts/{accountId}` | Xóa tài khoản |

### Frontend — Staff (Admin)
| Route | Page | Chức năng |
|-------|------|-----------|
| `/management/manage-accounts` | AccountListPage | Danh sách + lọc + view/edit/roles/password/toggle/delete |

---

## 2. File thay đổi

### 2.1 Backend — File mới

| File | Vai trò |
|------|---------|
| `controller/AccountController.java` | REST endpoints — tất cả `@PreAuthorize("hasRole('ADMIN')")` |
| `service/AccountService.java` | Interface service |
| `service/impl/AccountServiceImpl.java` | Implementation: CRUD, roles, password, toggle-active (bảo vệ admin cuối), delete |
| `dto/request/account/AccountFilterRequest.java` | Request DTO lọc (search, role, isActive, staffPosition, authProvider) |
| `dto/request/account/CreateAccountRequest.java` | Request DTO tạo account + staff + roles |
| `dto/request/account/UpdateAccountRequest.java` | Request DTO cập nhật staff + isActive |
| `dto/request/account/AssignRolesRequest.java` | Request DTO gán role (list roleIds) |
| `dto/request/account/ResetPasswordRequest.java` | Request DTO đặt lại mật khẩu |
| `dto/response/account/AccountListResponse.java` | Response DTO item danh sách |
| `dto/response/account/AccountDetailResponse.java` | Response DTO chi tiết tài khoản |
| `dto/response/account/RoleResponse.java` | Response DTO role (roleId, roleName) |
| `dto/response/account/StaffInfoResponse.java` | Response DTO thông tin nhân sự |
| `dto/projection/AccountListProjection.java` | Projection native query — tránh load entity `Account` (vì `getPassword()` throw) |

### 2.2 Backend — File sửa

| File | Thay đổi |
|------|----------|
| `service/impl/AccountServiceImpl.java` | Thêm import `AccountService` (compile fix) + chuyển `filterAccounts` sang native query projection thay vì `findAll()` entity |
| `repository/AccountRepository.java` | Thêm native query `findAllAccountList()` + import `AccountListProjection` |
| `repository/AccountRoleRepository.java` | Thêm `findByAccountId(Integer)`, `deleteByAccountId(Integer)` |
| `repository/StaffRepository.java` | Thêm `findByAccountId(Integer)`, `existsByAccountId(Integer)` |

### 2.3 Frontend (Staff) — File mới

| File | Vai trò |
|------|---------|
| `features/manage-accounts/index.js` | Export module |
| `features/manage-accounts/api/accountApi.js` | 9 hàm gọi API (filter, detail, create, update, roles, password, toggle, delete, allRoles) |
| `features/manage-accounts/hooks/useAccounts.js` | Hook quản lý state, filter, debounce, pagination |
| `features/manage-accounts/routes/AccountRoutes.jsx` | Route `/management/manage-accounts` → AccountListPage |
| `features/manage-accounts/components/AccountFilter.jsx` | Thanh lọc (search, trạng thái, chức vụ, loại tài khoản) |
| `features/manage-accounts/components/AccountTable.jsx` | Bảng danh sách + 6 nút action (view, edit, roles, password, toggle, delete) |
| `features/manage-accounts/components/AccountCreateModal.jsx` | Modal tạo tài khoản (account + staff + role checkboxes) |
| `features/manage-accounts/components/AccountUpdateModal.jsx` | Modal sửa thông tin nhân sự |
| `features/manage-accounts/components/AccountDetailModal.jsx` | Modal xem chi tiết (account + roles + staff) |
| `features/manage-accounts/components/AccountRoleModal.jsx` | Modal phân quyền (toggle switches, có check hasChanges) |
| `features/manage-accounts/components/AccountResetPasswordModal.jsx` | Modal đặt lại mật khẩu |
| `pages/admin/AccountListPage.jsx` | Trang danh sách tài khoản |

### 2.4 Frontend (Staff) — File sửa

| File | Thay đổi |
|------|----------|
| `routes/AppRouter.jsx` | Thêm import `accountRoutes`, thay placeholder bằng `{accountRoutes}` |
| `components/layout/DesktopStaffLayout/SideBar.jsx` | Thêm icon `BsPeopleFill`, thêm menu item "Quản lý tài khoản" (ADMIN-only) |

---

## 3. Database (tables liên quan)

```sql
-- Bảng tài khoản (đã tồn tại)
account (accountId, username, passwordHash, firebaseUid, authProvider, isActive, lastLogin, createdAt, createdBy, updatedAt, updatedBy)

-- Bảng vai trò (đã tồn tại)
role (roleId, roleName, isActive, createdAt, createdBy, updatedAt, updatedBy)

-- Bảng liên kết account-role (đã tồn tại)
account_role (accountRoleId, accountId, roleId)

-- Bảng nhân sự (đã tồn tại)
staff (staffId, accountId, ticketAgencyId, staffName, phone, email, dob, cccd, staffPosition, hireDate, isActive, createdAt, createdBy, updatedAt, updatedBy)
```

---

## 4. Business Rules

1. **Username duy nhất** — Không thể tạo 2 tài khoản cùng username.
2. **Admin bảo vệ** — Không thể vô hiệu hóa hoặc xóa tài khoản ADMIN cuối cùng.
3. **Password ≥ 6 ký tự** — Validation ở cả BE (`@Size(min = 6)`) và FE.
4. **Soft-delete** — Xóa account sẽ set `staff.accountId = NULL`, không hard-delete staff.
5. **Roles sync** — Khi gán role, xóa hết cũ và insert mới (không diff).
6. **Audit trail** — `createdBy`/`updatedBy` có sẵn qua `BaseEntity`.
