# Use Case: Hệ Thống Quản Lý Nhà Xe & Vận Chuyển Hàng Hóa

> **Project:** CargoTrack (NhaXeTuanMV)
> **Domain:** Quản lý tuyến xe khách Quảng Trị - Hà Nội & vận chuyển hàng hóa
> **Audience:** Chủ nhà xe, quản lý, nhân viên, khách hàng

---

## 1. Actors

| Actor | Mô tả | Mục tiêu chính |
|---|---|---|
| **Khách hàng** (Customer) | Người đi xe hoặc gửi hàng | Đặt vé, gửi hàng, tra cứu thông tin |
| **Quản lý** (Manager) | Điều hành hoạt động nhà xe | Quản lý giá, tài nguyên, báo cáo |
| **NV Bán vé** (Ticket Staff) | Nhân viên bán vé tại bến | Bán vé trực tiếp, xác nhận đặt chỗ |
| **NV Điều xe** (Trip Staff) | Nhân viên giám sát chuyến | Soát vé, cập nhật trạng thái chuyến |
| **Admin** | Quản trị hệ thống | Quản lý tài khoản, cấu hình hệ thống |
| **Firebase Auth** | Hệ thống xác thực bên thứ ba | Xác thực khách hàng qua Google/Facebook/SĐT |

---

## 2. Use Case Diagram (Tổng Quan)

```
                    ┌─────────────────────────────────────┐
                    │         HỆ THỐNG QUẢN LÝ NHÀ XE      │
                    └─────────────────────────────────────┘
                                    │
        ┌────────────┬──────────────┬──────────────┬────────────┐
        │            │              │              │            │
   ┌────▼────┐ ┌────▼────┐  ┌──────▼──────┐  ┌────▼────┐  ┌───▼────┐
   │КНÁCH HÀNG│ │QUẢN LÝ  │  │NV BÁN VÉ   │  │NV ĐIỀU  │  │ ADMIN  │
   │          │ │(MANAGER)│  │(TICKET STAFF)│  │(TRIP    │  │        │
   └──────────┘ └─────────┘  └─────────────┘  │ STAFF)  │  └────────┘
                                                └─────────┘
```

---

## 3. Use Cases - Khách Hàng (Customer)

### UC-C01: Tra cứu chuyến xe
| Mục | Mô tả |
|---|---|
| **Mô tả** | Khách hàng tìm kiếm chuyến xe theo ngày, tuyến, khung giờ |
| **Trigger** | Khách truy cập trang chủ, nhập điểm đi/đến + ngày |
| **Luồng chính** | 1. Nhập tuyến (tỉnh đi → tỉnh đến) 2. Chọn ngày 3. Hệ thống hiển thị danh sách chuyến (giờ, loại xe, giá, ghế trống) 4. Lọc nâng cao theo khung giờ sáng/trưa/chiều/tối, loại xe, khoảng giá |
| **Kết quả** | Danh sách chuyến phù hợp với thông tin chi tiết về giờ, giá, ghế |
| **API** | `GET /api/v1/trips/home` (basic & advanced) |

### UC-C02: Đặt vé xe khách
| Mục | Mô tả |
|---|---|
| **Mô tả** | Khách hàng chọn ghế và đặt vé cho chuyến xe |
| **Pre-condition** | Khách đã tra cứu chuyến, click vào chuyến muốn đặt |
| **Luồng chính** | 1. Xem sơ đồ ghế (seat layout) của chuyến 2. Chọn ghế trống 3. Nhập thông tin hành khách (tên, SĐT, email, điểm đón/trả) 4. Nhập mã giảm giá (nếu có) 5. Xác nhận đặt vé 6. Thanh toán |
| **Kết quả** | Vé được tạo, QR code gửi cho khách, ghế bị khóa |
| **Lưu ý** | Hỗ trợ đặt nhiều ghế cùng lúc, trẻ em đi kèm (accompanied_child) |

### UC-C03: Gửi hàng hóa
| Mục | Mô tả |
|---|---|
| **Mô tả** | Khách hàng gửi hàng theo chuyến xe |
| **Luồng chính** | 1. Chọn chuyến xe 2. Nhập thông tin người gửi (tên, SĐT, email) 3. Nhập thông tin người nhận (tên, SĐT, email) 4. Chọn loại hàng, nhập khối lượng/kích thước 5. Chọn điểm gửi/điểm nhận 6. Chọn bên trả phí (người gửi/người nhận) 7. Nhập COD (nếu có) 8. Xác nhận và thanh toán |
| **Trạng thái hàng** | `RECEIVED` → `LOADED` → `ARRIVED` → `DELIVERED` |
| **Kết quả** | Cargo ticket được tạo, mã vận đơn gửi cho khách |

### UC-C04: Đăng nhập/Đăng ký
| Mục | Mô tả |
|---|---|
| **Mô tả** | Đăng nhập bằng Firebase (Google, Facebook, SĐT) |
| **API** | `POST /api/auth/customer/login`, `POST /api/auth/customer/register` |
| **Luồng** | Firebase SDK xác thực → gửi token lên server → server verify → trả JWT |

### UC-C05: Xem lịch sử đặt vé
| Mục | Mô tả |
|---|---|
| **Mô tả** | Khách hàng xem lại các vé đã đặt, trạng thái chuyến |
| **Ghi chú** | Chưa implement đầy đủ trên frontend customer |

---

## 4. Use Cases - Quản Lý (Manager)

### UC-M01: Quản lý tuyến xe (Route CRUD)
| Mục | Mô tả |
|---|---|
| **Mô tả** | Quản lý danh sách tuyến, thêm/sửa/xóa tuyến |
| **Tính năng** | Tạo tuyến (tên, tổng km, tổng thời gian), soft-delete, khôi phục |
| **API** | `GET/POST/PUT /api/v1/routes`, `PATCH .../soft-delete`, `PATCH .../restore` |

### UC-M02: Quản lý điểm dừng trên tuyến (RouteStops)
| Mục | Mô tả |
|---|---|
| **Mô tả** | Quản lý thứ tự các điểm dừng trên tuyến |
| **Tính năng** | Thêm điểm dừng (khoảng cách từ đầu, thời gian từ đầu), kéo-thả sắp xếp thứ tự |
| **API** | `PUT /api/v1/route-stops/bulk-update-orders` (reorder), `POST/PUT/DELETE` từng điểm |

### UC-M03: Quản lý xe (Coach CRUD)
| Mục | Mô tả |
|---|---|
| **Mô tả** | Quản lý danh sách xe, thêm xe mới |
| **Tính năng** | Thêm xe (biển số, hãng, năm, loại xe, tuyến), tự động sinh sơ đồ ghế từ loại xe, lọc theo trạng thái (ACTIVE/MAINTENANCE/RETIRED), soft-delete |
| **API** | `GET/POST /api/v1/coaches` |

### UC-M04: Quản lý loại xe (CoachType CRUD)
| Mục | Mô tả |
|---|---|
| **Mô tả** | Quản lý loại xe (giường nằm, ghế ngồi, limousine...) |
| **Tính năng** | Tạo loại xe với sơ đồ ghế (seat layout JSON), cập nhật giá, lịch sử giá, cập nhật sơ đồ ghế |
| **Ràng buộc** | Không thể hủy kích hoạt loại xe nếu còn xe đang hoạt động |
| **API** | Full CRUD `/api/v1/coach-types` |

### UC-M05: Quản lý bến xe (CoachStop CRUD)
| Mục | Mô tả |
|---|---|
| **Mô tả** | Quản lý danh sách bến xe / điểm đón trả |
| **Ràng buộc** | UNIQUE(address, city) |
| **API** | Full CRUD `/api/v1/coach-stops` |

### UC-M06: Quản lý voucher/khuyến mãi
| Mục | Mô tả |
|---|---|
| **Mô tả** | Tạo và quản lý mã giảm giá |
| **Loại** | Giảm theo % (capped) hoặc số tiền cố định |
| **Ràng buộc** | Có ngày hiệu lực, số lần dùng tối đa, giá trị đơn hàng tối thiểu |
| **Thống kê** | Xem metrics: số lượng voucher đã dùng, còn hiệu lực, sắp hết hạn |
| **API** | Full CRUD + metrics `/api/v1/vouchers` |

### UC-M07: Quản lý loại hàng hóa (CargoType)
| Mục | Mô tả |
|---|---|
| **Mô tả** | Định nghĩa các loại hàng hóa được vận chuyển |
| **API** | Full CRUD `/api/v1/manager/cargo-types` |

### UC-M08: Định giá hàng hóa (CargoTypePrice)
| Mục | Mô tả |
|---|---|
| **Mô tả** | Thiết lập giá cước cho từng loại hàng theo thời gian |
| **Tính năng** | Giá có hiệu lực theo ngày (start/end effective date), lịch sử giá |
| **API** | Full CRUD `/api/v1/manager/cargo-type-prices` |

### UC-M09: Tự động sinh lịch chạy xe hàng tuần
| Mục | Mô tả |
|---|---|
| **Mô tả** | Sinh lịch trình cho 7 ngày với 24 chuyến/ngày/hướng |
| **Kết quả** | Tạo trip + trip_seat với giá hiện tại, phân công tài xế/phụ xe, tránh xung đột lịch |
| **Công nghệ** | Stored Procedure `sp_AutoGenerateWeeklySchedule_Final` trong SQL Server |
| **Ghi chú** | Chưa có API endpoint gọi thủ tục này từ backend |

---

## 5. Use Cases - Nhân Viên Bán Vé (Ticket Staff)

### UC-TS01: Bán vé trực tiếp
| Mục | Mô tả |
|---|---|
| **Mô tả** | Nhân viên bán vé cho khách đến bến mua trực tiếp |
| **Ghi chú** | Chưa implement đầy đủ, chỉ có trang placeholder `/staff/ticket/sell` |

### UC-TS02: Tra cứu lịch sử bán vé
| Mục | Mô tả |
|---|---|
| **Ghi chú** | Chưa implement |

---

## 6. Use Cases - Nhân Viên Điều Xe (Trip Staff)

### UC-TR01: Soát vé lên xe
| Mục | Mô tả |
|---|---|
| **Mô tả** | Quét QR code vé, xác nhận hành khách đã lên xe |
| **Trạng thái** | `CHECKED_IN` |
| **Ghi chú** | Chưa implement |

### UC-TR02: Cập nhật trạng thái chuyến
| Mục | Mô tả |
|---|---|
| **Mô tả** | Cập nhật chuyến: IN_PROGRESS, COMPLETED, CANCELLED |
| **Ghi chú** | Chưa implement |

---

## 7. Use Cases - Admin

### UC-A01: Quản lý tài khoản
| Mục | Mô tả |
|---|---|
| **Mô tả** | Tạo/sửa/xóa tài khoản nhân viên, phân quyền |
| **Ghi chú** | Chưa implement |

### UC-A02: Cấu hình hệ thống
| Mục | Mô tả |
|---|---|
| **Mô tả** | Cấu hình chung: tham số, API key, v.v. |
| **Ghi chú** | Chưa implement |

---

## 8. Use Cases - Hệ Thống (System)

### UC-S01: Xác thực & phân quyền
| Mục | Mô tả |
|---|---|
| **Mô tả** | Hệ thống xác thực người dùng (JWT) và phân quyền dựa trên role |
| **Cơ chế** | JWT access token (24h) + refresh token (7d) → Spring Security Filter Chain → Role-based authorization với `@PreAuthorize` |
| **Role** | `CUSTOMER`, `MANAGER`, `TICKET_STAFF`, `TRIP_STAFF`, `ADMIN` |

### UC-S02: Tự động tạo ghế từ sơ đồ
| Mục | Mô tả |
|---|---|
| **Mô tả** | Khi tạo xe mới, hệ thống tự động sinh các bản ghi ghế từ seat layout JSON |
| **Cơ chế** | CoachServiceImpl.createCoach() parse seatLayout → tạo N bản ghi Seat |

### UC-S03: Refresh token tự động
| Mục | Mô tả |
|---|---|
| **Mô tả** | Axios interceptor tự động refresh JWT khi token hết hạn (401) |
| **Cơ chế** | Axios response interceptor → gọi `/api/auth/refresh-token` → lưu token mới → retry request |

---

## 9. Ma Trận Use Case - Actor

| Use Case | Khách hàng | Manager | Ticket Staff | Trip Staff | Admin | System |
|---|---|---|---|---|---|---|
| UC-C01: Tra cứu chuyến | ✓ | | | | | ✓ |
| UC-C02: Đặt vé khách | ✓ | | | | | ✓ |
| UC-C03: Gửi hàng | ✓ | | | | | ✓ |
| UC-C04: Đăng nhập/Đăng ký | ✓ | | | | | ✓ |
| UC-C05: Lịch sử vé | ✓ | | | | | |
| UC-M01: Quản lý tuyến | | ✓ | | | | |
| UC-M02: Quản lý điểm dừng | | ✓ | | | | |
| UC-M03: Quản lý xe | | ✓ | | | | ✓ |
| UC-M04: Quản lý loại xe | | ✓ | | | | ✓ |
| UC-M05: Quản lý bến xe | | ✓ | | | | |
| UC-M06: Quản lý voucher | | ✓ | | | | |
| UC-M07: Quản lý loại hàng | | ✓ | | | | |
| UC-M08: Định giá hàng | | ✓ | | | | |
| UC-M09: Sinh lịch tuần | | ✓ | | | | ✓ |
| UC-TS01: Bán vé trực tiếp | | | ✓ | | | |
| UC-TS02: Lịch sử bán vé | | | ✓ | | | |
| UC-TR01: Soát vé | | | | ✓ | | |
| UC-TR02: Cập nhật chuyến | | | | ✓ | | |
| UC-A01: Quản lý tài khoản | | | | | ✓ | |
| UC-A02: Cấu hình hệ thống | | | | | ✓ | |
| UC-S01: Xác thực & phân quyền | | | | | | ✓ |
| UC-S02: Tạo ghế tự động | | | | | | ✓ |
| UC-S03: Refresh token | | | | | | ✓ |

---

## 10. Trạng Thái (Status Flow)

### Trạng thái vé xe khách (PassengerTicketDetail)
```
PENDING ──► CONFIRMED ──► CHECKED_IN ──► EXPIRED (sau giờ khởi hành)
                  │
                  └──► CANCELLED
```

### Trạng thái vé hàng hóa (CargoTicket)
```
RECEIVED ──► LOADED ──► ARRIVED ──► DELIVERED
   │                      │              │
   ├──► CANCELLED         │              │
   ├──► REJECTED          │              │
   └──► ABANDONED         │              │
                          └──► ABANDONED  │
                                         └──► ABANDONED
```

### Trạng thái chuyến (Trip)
```
SCHEDULED ──► IN_PROGRESS ──► COMPLETED
       │                         │
       └──► CANCELLED            └──► ...
```

### Trạng thái voucher
```
(Chưa đến hạn) UPCOMING
(Đang hiệu lực) ACTIVE
(Hết hạn) EXPIRED
(Hết lượt dùng) EXHAUSTED
```

---

## 11. Ràng Buộc Kinh Doanh (Business Rules)

1. **Đặt ghế:** Một ghế trong một chuyến chỉ được đặt bởi một người (UNIQUE tripId + seatId trong trip_seat)
2. **Voucher:** Giảm % không vượt quá 100%; không vượt quá maxDiscountValue; chỉ dùng được trong hiệu lực; kiểm tra minOrderValue
3. **Không thể hủy loại xe đang có xe hoạt động**
4. **Lịch tài xế/phụ xe:** Khoảng cách tối thiểu 720 phút (12 tiếng) giữa 2 chuyến của cùng một người
5. **Loại xe:** Tổng số ghế 1-44, tối đa 2 tầng, 11 hàng, 5 cột
6. **Thanh toán:** Một payment chỉ thuộc về passenger_ticket hoặc cargo_ticket (exclusive), không thể cả hai
