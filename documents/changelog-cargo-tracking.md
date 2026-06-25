# Changelog: Tra cứu đơn hàng (Cargo Tracking)

> **Branch:** `feature/cargoTracking`
> **Commit:** `2900188` / `b1414ab`

---

## 1. Tính năng mới

### 1.1 Tra cứu đơn hàng (Customer)
- **Mô tả:** Khách hàng nhập mã vận đơn → xem toàn bộ thông tin hàng hóa
- **Route:** `/tra-cuu` (public, không cần đăng nhập)
- **Header:** Nút "Tra cứu đơn" ở thanh điều hướng

### 1.2 API Backend
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/api/v1/cargo-tracking/{ticketCode}` | Tra cứu đơn hàng theo mã vận đơn |

**Response:**
```json
{
  "ticketCode": "CG_CODE_0001",
  "status": "RECEIVED",
  "senderName": "Người Gửi Số 1",
  "senderPhone": "091200001",
  "receiverName": "Người Nhận Số 1",
  "receiverPhone": "097800001",
  "pickupStopName": "Bến xe Quảng Trị",
  "dropoffStopName": "Bến xe Mỹ Đình",
  "totalPrice": 80000.00,
  "feePayer": "SENDER",
  "codAmount": 0.00,
  "description": null,
  "tripRouteName": "Quảng Trị - Hà Nội",
  "tripDepartureTime": "2026-06-22T07:30:00",
  "items": [
    {
      "description": "Kiện bưu phẩm ký gửi mẫu số 1",
      "quantity": 2,
      "weightKg": 11.00,
      "dimensionVol": 0.40,
      "calculatedPrice": 80000.00,
      "unit": "kg"
    }
  ]
}
```

---

## 2. Backend — File thay đổi

### 2.1 File mới

| File | Vai trò |
|------|---------|
| `repository/CargoTicketRepository.java` | Repository: findByTicketCode |
| `repository/CargoTicketDetailRepository.java` | Repository: findByCargoTicketId |
| `dto/response/CargoTrackingResponse.java` | DTO response (thông tin đơn + danh sách items) |
| `service/CargoTrackingService.java` | Interface service |
| `service/impl/CargoTrackingServiceImpl.java` | Implementation: lookup ticket, details, stops, trip, route, pricing |
| `controller/CargoTrackingController.java` | REST endpoint `/api/v1/cargo-tracking/{ticketCode}` |

### 2.2 File sửa

| File | Thay đổi |
|------|----------|
| `config/SecurityConfig.java` | Thêm `.requestMatchers("/api/v1/cargo-tracking/**").permitAll()` — cho phép public |
| `.gitignore` | Thêm `**/firebase-service-account.json` — tránh commit secret |

---

## 3. Frontend (Customer) — File thay đổi

### 3.1 File mới

| File | Vai trò |
|------|---------|
| `features/cargo/api/cargoTrackingApi.js` | Gọi API backend |
| `pages/public/cargo-tracking/CargoTrackingPage.jsx` | Trang tra cứu (nhập mã → hiển thị kết quả) |
| `pages/public/cargo-tracking/CargoTrackingPage.css` | Style timeline, card, table |

### 3.2 File sửa

| File | Thay đổi |
|------|----------|
| `routes/AppRouter.jsx` | Thêm import + route `/tra-cuu` |
| `pages/auth/Login.jsx` | Uncomment `setError` để hiển thị lỗi đăng nhập (trước đây bị swallow) |

---

## 4. Giao diện tra cứu

- **Form:** Ô nhập mã vận đơn + nút "Tra cứu"
- **Timeline:** 4 bước trạng thái — Đã nhận → Đã lên xe → Đã đến nơi → Đã giao
- **Thông tin:** Người gửi, người nhận, điểm giao nhận, chuyến xe
- **Chi tiết đơn:** Mã vận đơn, trạng thái, người trả cước, COD, tổng cước
- **Bảng hàng hóa:** Mô tả, số lượng, kg, khối (m³), ĐVT, thành tiền
- **Responsive:** Grid 2 cột → 1 cột trên mobile

---

## 5. Cấu hình khác

| File | Thay đổi |
|------|----------|
| `backend/.env` | Firebase API key + config (đã thêm vào `.gitignore`) |
| `resources/firebase-service-account.json` | Firebase Admin SDK key (đã thêm vào `.gitignore`) |
