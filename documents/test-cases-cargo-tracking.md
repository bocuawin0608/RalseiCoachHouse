# Test Cases: Cargo Tracking

> **Feature:** Tra cứu đơn hàng
> **Module:** Customer Frontend + Backend API

---

## 1. Backend API

### TC-BE-01: Tra cứu đơn hàng tồn tại

| Mục | Giá trị |
|-----|---------|
| **Mô tả** | Tra cứu đơn hàng có mã hợp lệ trong DB |
| **Endpoint** | `GET /api/v1/cargo-tracking/CG_CODE_0001` |
| **Expected** | Status 200, trả về `CargoTrackingResponse` với đầy đủ fields (ticketCode, status, sender, receiver, items, ...) |

### TC-BE-02: Tra cứu mã không tồn tại

| Mục | Giá trị |
|-----|---------|
| **Mô tả** | Tra cứu mã vận đơn không có trong DB |
| **Endpoint** | `GET /api/v1/cargo-tracking/INVALID-CODE` |
| **Expected** | Status 404, message: "Không tìm thấy đơn hàng với mã: INVALID-CODE" |

### TC-BE-03: Tra cứu không cần auth

| Mục | Giá trị |
|-----|---------|
| **Mô tả** | Endpoint cho phép truy cập public (không cần JWT) |
| **Expected** | Status 200 (không trả về 401) |

### TC-BE-04: Đơn hàng có tripId = 0

| Mục | Giá trị |
|-----|---------|
| **Mô tả** | Đơn hàng chưa được gán chuyến xe |
| **Expected** | `tripRouteName` = null, `tripDepartureTime` = null |

### TC-BE-05: Đơn hàng có nhiều items

| Mục | Giá trị |
|-----|---------|
| **Mô tả** | Đơn hàng có 3 dòng hàng hóa |
| **Expected** | `items` trả về 3 objects, mỗi object có `calculatedPrice`, `unit`, `weightKg` |

### TC-BE-06: Đơn hàng không có items

| Mục | Giá trị |
|-----|---------|
| **Mô tả** | Đơn hàng không có chi tiết hàng hóa |
| **Expected** | `items` = [] (mảng rỗng) |

### TC-BE-07: pickupStopId/dropoffStopId không tồn tại

| Mục | Giá trị |
|-----|---------|
| **Mô tả** | Điểm đón/trả đã bị xóa khỏi DB |
| **Expected** | `pickupStopName` / `dropoffStopName` = "N/A" |

### TC-BE-08: CORS

| Mục | Giá trị |
|-----|---------|
| **Mô tả** | Request từ origin `http://localhost:3000` |
| **Expected** | Response có header `Access-Control-Allow-Origin: http://localhost:3000` |

---

## 2. Frontend (Customer)

### TC-FE-01: Giao diện mặc định

| Mục | Giá trị |
|-----|---------|
| **Steps** | 1. Vào trang `/tra-cuu` |
| **Expected** | Hiển thị ô input + nút "Tra cứu", không có lỗi |

### TC-FE-02: Submit form rỗng

| Mục | Giá trị |
|-----|---------|
| **Steps** | 1. Để trống ô input 2. Click "Tra cứu" |
| **Expected** | Không gọi API, không có thông báo lỗi |

### TC-FE-03: Tra cứu thành công

| Mục | Giá trị |
|-----|---------|
| **Steps** | 1. Nhập `CG_CODE_0001` 2. Click "Tra cứu" |
| **Expected** | Hiển thị timeline + 4 thẻ thông tin + bảng hàng hóa |

### TC-FE-04: Tra cứu mã không tồn tại

| Mục | Giá trị |
|-----|---------|
| **Steps** | 1. Nhập `INVALID-CODE` 2. Click "Tra cứu" |
| **Expected** | Hiển thị lỗi đỏ "Không tìm thấy đơn hàng với mã này." |

### TC-FE-05: Loading state

| Mục | Giá trị |
|-----|---------|
| **Steps** | 1. Nhập mã 2. Click "Tra cứu" |
| **Expected** | Nút chuyển thành "Đang tra..." và bị disabled |

### TC-FE-06: Lỗi mạng

| Mục | Giá trị |
|-----|---------|
| **Steps** | 1. Tắt backend 2. Nhập mã 3. Click "Tra cứu" |
| **Expected** | Hiển thị "Có lỗi xảy ra, vui lòng thử lại sau." |

### TC-FE-07: Timeline — RECEIVED

| Mục | Giá trị |
|-----|---------|
| **Steps** | 1. Tra cứu đơn có status = `RECEIVED` |
| **Expected** | Chỉ step 1 (Đã nhận hàng) active (màu xanh) |

### TC-FE-08: Timeline — LOADED

| Mục | Giá trị |
|-----|---------|
| **Steps** | 1. Tra cứu đơn có status = `LOADED` |
| **Expected** | Step 1-2 active |

### TC-FE-09: Timeline — ARRIVED

| Mục | Giá trị |
|-----|---------|
| **Steps** | 1. Tra cứu đơn có status = `ARRIVED` |
| **Expected** | Step 1-3 active |

### TC-FE-10: Timeline — DELIVERED

| Mục | Giá trị |
|-----|---------|
| **Steps** | 1. Tra cứu đơn có status = `DELIVERED` |
| **Expected** | Cả 4 steps active, mỗi step có icon ✓ |

### TC-FE-11: COD > 0

| Mục | Giá trị |
|-----|---------|
| **Steps** | 1. Tra cứu đơn có `codAmount = 200000` |
| **Expected** | Hiển thị dòng "COD: 200.000 đ" |

### TC-FE-12: COD = 0

| Mục | Giá trị |
|-----|---------|
| **Steps** | 1. Tra cứu đơn có `codAmount = 0` |
| **Expected** | Không hiển thị dòng COD |

### TC-FE-13: Responsive mobile

| Mục | Giá trị |
|-----|---------|
| **Steps** | 1. Mở DevTools 2. Chọn viewport ≤ 640px |
| **Expected** | Grid chuyển 1 cột, timeline xếp dọc, form xếp dọc |

### TC-FE-14: Route `/tra-cuu` tồn tại

| Mục | Giá trị |
|-----|---------|
| **Steps** | 1. Click nút "Tra cứu đơn" trên header |
| **Expected** | Điều hướng đến `/tra-cuu` |

---

## 3. Dữ liệu test (fakedata.sql)

Các mã vận đơn có sẵn trong seed data:

| Mã vận đơn | Status | COD | Items |
|-----------|--------|-----|-------|
| `CG_CODE_0001` | RECEIVED | 0 | 1 item |
| `CG_CODE_0002` | RECEIVED | 0 | 1 item |
| `CG_CODE_0005` | RECEIVED | 200.000 | 1 item |
| ... | ... | ... | ... |
| `CG_CODE_0300` | RECEIVED | 0 | 1 item |

Chạy `fakedata.sql` trong SQL Server để tạo dữ liệu.

---

## 4. Môi trường kiểm thử

| Component | URL | Ghi chú |
|-----------|-----|---------|
| Frontend | `https://localhost:3000` | Vite dev server |
| Backend | `https://localhost:9090` | Spring Boot |
| Database | `localhost:1433` | SQL Server, DB: `VeXeDB` |
| Firebase | `swp-firebase-dbdpa` | Auth emulator tùy chọn |
