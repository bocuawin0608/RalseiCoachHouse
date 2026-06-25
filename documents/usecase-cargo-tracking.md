# Use Case: Tra cứu đơn hàng (Cargo Tracking)

---

## UC-C06: Tra cứu đơn hàng

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-C06 |
| **Name** | Tra cứu đơn hàng |
| **Primary Actors** | Khách hàng (Customer) |
| **Secondary Actor** | Hệ thống backend (CargoTrackingService, Database) |
| **Description** | Khách hàng nhập mã vận đơn để xem thông tin chi tiết về đơn hàng đã gửi, bao gồm trạng thái hiện tại, thông tin người gửi/nhận, điểm giao nhận, chuyến xe và danh sách hàng hóa kèm giá cước. |
| **Preconditions** | 1. Hệ thống backend đang hoạt động. 2. Dữ liệu đơn hàng đã tồn tại trong database (bảng `cargo_ticket`, `cargo_ticket_detail`). 3. API `/api/v1/cargo-tracking/{ticketCode}` cho phép truy cập public (không cần auth). |
| **Postconditions** | 1. Hiển thị đầy đủ thông tin đơn hàng nếu tìm thấy. 2. Hiển thị thông báo lỗi nếu không tìm thấy mã hoặc có lỗi hệ thống. |
| **Normal Flow** | 1. Khách hàng truy cập trang `/tra-cuu`. 2. Hệ thống hiển thị form nhập mã vận đơn. 3. Khách hàng nhập mã vận đơn (vd: `CG_CODE_0001`). 4. Khách hàng click nút "Tra cứu". 5. Hệ thống gọi `GET /api/v1/cargo-tracking/{ticketCode}`. 6. Backend tìm `CargoTicket` theo `ticketCode`. 7. Backend lấy thông tin `CargoTicketDetail` theo `cargoTicketId`. 8. Backend lấy tên điểm đón/trả từ `CoachStop`. 9. Backend lấy thông tin chuyến xe (`Trip` + `Route`). 10. Backend lấy đơn vị tính từ `CargoTypePrice`. 11. Backend trả về `CargoTrackingResponse`. 12. Frontend render timeline trạng thái + thẻ thông tin + bảng hàng hóa. |
| **Alternative Flow** | **A1: Đơn hàng chưa được gán chuyến xe** — Tại bước 9, nếu `tripId = 0` hoặc không tìm thấy Trip, backend trả về `tripRouteName = null`, `tripDepartureTime = null`. Frontend hiển thị "N/A". |
| **Alternative Flow** | **A2: Điểm đón/trả không tồn tại** — Tại bước 8, nếu `pickupStopId` hoặc `dropoffStopId` không tìm thấy trong `CoachStop`, backend trả về tên điểm = "N/A". |
| **Alternative Flow** | **A3: COD = 0** — Tại bước 12, nếu `codAmount = 0`, frontend ẩn dòng COD. |
| **Exception Flow** | **E1: Mã vận đơn không tồn tại** — Tại bước 6, `findByTicketCode` trả về `Optional.empty()`. Backend throw `ResourceNotFoundException`. Frontend nhận status 404, hiển thị "Không tìm thấy đơn hàng với mã này." |
| **Exception Flow** | **E2: Lỗi kết nối backend** — Tại bước 5, request thất bại do mạng hoặc server down. Frontend catch error, hiển thị "Có lỗi xảy ra, vui lòng thử lại sau." |
| **Exception Flow** | **E3: Lỗi hệ thống (500)** — Server gặp lỗi nội bộ. Frontend nhận status 500, hiển thị thông báo lỗi chung. |

---

## UC-C04: Đăng nhập bằng Google

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-C04 |
| **Name** | Đăng nhập bằng tài khoản Google |
| **Primary Actors** | Khách hàng (Customer) |
| **Secondary Actor** | Firebase Auth, Backend AuthService |
| **Description** | Khách hàng đăng nhập bằng tài khoản Google thông qua Firebase Authentication. Hệ thống tự động tạo tài khoản nếu lần đầu đăng nhập. |
| **Preconditions** | 1. Firebase project đã bật Google provider trong Authentication > Sign-in method. 2. API key không bị giới hạn HTTP referrer. 3. Backend có Firebase Admin SDK (`firebase-service-account.json`) hoạt động. |
| **Postconditions** | 1. JWT access token + refresh token được lưu trong localStorage. 2. User được chuyển hướng về trang chủ. 3. Account + Customer record được tạo trong DB nếu là lần đầu. |
| **Normal Flow** | 1. Khách hàng truy cập trang `/login`. 2. Hệ thống hiển thị form đăng nhập với các nút Google, Facebook. 3. Khách hàng click nút "Google". 4. Firebase mở popup đăng nhập Google. 5. Khách hàng chọn tài khoản Google và xác thực. 6. Firebase trả về `userCredential` với ID token. 7. Frontend gọi `POST /api/auth/customer/login` với `{ idToken, username: email }`. 8. Backend verify Firebase token bằng Firebase Admin SDK. 9. Backend tìm Account theo username (email). 10. Nếu chưa tồn tại → tạo mới Account + Customer + Role CUSTOMER. 11. Backend tạo JWT access token + refresh token. 12. Frontend lưu token vào localStorage. 13. Chuyển hướng về trang chủ. |
| **Alternative Flow** | **A1: Tài khoản đã tồn tại** — Tại bước 9, nếu Account đã tồn tại, bỏ qua bước 10, tiến hành cập nhật `firebaseUid` + `lastLogin`. |
| **Alternative Flow** | **A2: Popup bị chặn** — Tại bước 4, trình duyệt chặn popup. Người dùng cần cho phép popup hoặc click lại. |
| **Exception Flow** | **E1: Firebase chưa được cấu hình** — Tại bước 8, Firebase Admin SDK chưa được khởi tạo. Backend throw `BusinessRuleException("Firebase chưa được cấu hình!")`. Frontend hiển thị "Đăng nhập Google thất bại!" |
| **Exception Flow** | **E2: Google provider chưa bật** — Tại bước 4, Firebase throw `auth/operation-not-allowed`. Frontend hiển thị lỗi. |
| **Exception Flow** | **E3: API key bị chặn** — Firebase request bị từ chối do API key restriction (HTTP referrer không cho phép localhost). |
| **Exception Flow** | **E4: Người dùng đóng popup** — Tại bước 5, người dùng đóng popup trước khi xác thực. Firebase throw `auth/popup-closed-by-user`. Frontend bỏ qua (không hiển thị lỗi). |

---

## UC-C07: Xem chi tiết đơn hàng

| Field | Value |
|-------|-------|
| **Use Case ID** | UC-C07 |
| **Name** | Xem chi tiết đơn hàng sau tra cứu |
| **Primary Actors** | Khách hàng (Customer) |
| **Secondary Actor** | Hệ thống backend |
| **Description** | Sau khi tra cứu thành công, khách hàng xem chi tiết các thông tin của đơn hàng bao gồm timeline trạng thái, thông tin người gửi/nhận, điểm giao nhận, chuyến xe, và danh sách hàng hóa kèm giá. |
| **Preconditions** | 1. Tra cứu mã vận đơn thành công (UC-C06). 2. Dữ liệu đơn hàng tồn tại. |
| **Postconditions** | Khách hàng nắm được trạng thái hiện tại và thông tin chi tiết đơn hàng. |
| **Normal Flow** | 1. Hệ thống hiển thị timeline 4 bước trạng thái: Đã nhận → Đã lên xe → Đã đến nơi → Đã giao. 2. Hệ thống hiển thị 4 thẻ thông tin: người gửi, người nhận, điểm giao nhận, chuyến xe. 3. Hệ thống hiển thị thẻ tổng quan đơn hàng: mã vận đơn, trạng thái, mô tả, người trả cước, COD, tổng cước. 4. Hệ thống hiển thị bảng danh sách hàng hóa: mô tả, số lượng, kg, khối (m³), đơn vị tính, thành tiền. |
| **Exception Flow** | **E1: Không có hàng hóa** — Nếu `items` = [], ẩn bảng hàng hóa. |
| **Exception Flow** | **E2: Không có chuyến xe** — Nếu `tripRouteName = null`, hiển thị "N/A". |
