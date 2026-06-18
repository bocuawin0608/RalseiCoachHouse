# Architecture & Design Pattern Document

> **Project:** CargoTrack (NhaXeTuanMV)
> **Tech Stack:** Spring Boot 3 + React 19 + MS SQL Server
> **Audience:** Developers, maintainers, contributors

---

## 1. Kiến Trúc Tổng Quan (High-Level Architecture)

```
┌─────────────────────────────────────────────────────────────────┐
│                    CLIENT LAYER                                  │
│                                                                   │
│  ┌─────────────────────────┐    ┌─────────────────────────────┐  │
│  │  frontend-react          │    │  frontend-react-staff       │  │
│  │  (Customer SPA)         │    │  (Staff SPA)                │  │
│  │  Port 3000 (HTTPS)      │    │  Port 2999 (HTTP)           │  │
│  │  React 19 + Vite 8      │    │  React 19 + Vite 8          │  │
│  └──────────┬──────────────┘    └─────────────┬───────────────┘  │
│             │                                  │                  │
└─────────────┼──────────────────────────────────┼──────────────────┘
              │           HTTP/JSON + JWT        │
              ▼                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                    API GATEWAY / BACKEND                         │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │  backend-springboot                                         │  │
│  │  Spring Boot 3.5.14 / Java 17                               │  │
│  │  Port 9090 (HTTPS)                                          │  │
│  │  WAR deployment (monolithic)                                │  │
│  │                                                             │  │
│  │  ┌──────────┐ ┌──────────────┐ ┌────────────────────┐       │  │
│  │  │ Controllers │ Services      │ │ Security (JWT)     │       │  │
│  │  │ (REST)    │ │ (Business)   │ │ + Firebase Admin   │       │  │
│  │  └─────┬─────┘ └──────┬───────┘ └────────────────────┘       │  │
│  │        │              │                                       │  │
│  │  ┌─────▼──────────────▼────────────────────────────────┐     │  │
│  │  │  Repositories (Spring Data JPA)                     │     │  │
│  │  └───────────────────────┬─────────────────────────────┘     │  │
│  │                          │                                   │  │
│  └──────────────────────────┼───────────────────────────────────┘  │
└─────────────────────────────┼─────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    DATA LAYER                                    │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │  Microsoft SQL Server                                       │  │
│  │  Database: VeXeDB                                           │  │
│  │  28 tables                                                  │  │
│  │  Stored Procedures (schedule generation)                    │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                                                                   │
│  ┌──────────────────────┐  ┌──────────────────────────────────┐  │
│  │  Firebase Auth        │  │  Goong.io Geocoding API         │  │
│  │  (Customer identity)  │  │  (Vietnamese maps)              │  │
│  └──────────────────────┘  └──────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Backend: Package Structure (Spring Boot Convention)

```
com.ralsei
├── Application.java              # @SpringBootApplication entry point
├── config/                       # Configuration classes
│   ├── SecurityConfig.java       # Spring Security filter chain
│   ├── FirebaseConfig.java       # Firebase Admin SDK init
│   ├── AppConfig.java            # Auth provider, password encoder
│   └── WebConfig.java            # CORS configuration
├── controller/                   # REST controllers (request handling)
├── service/                      # Business logic interfaces
│   └── impl/                     # Service implementations
├── repository/                   # Spring Data JPA interfaces
│   └── custom/                   # Custom repository queries
├── model/                        # JPA entities + enums
│   └── enums/                    # Enumerations
├── dto/
│   ├── request/                  # Inbound DTOs
│   ├── response/                 # Outbound DTOs
│   └── projection/               # Interface projections
├── security/                     # JWT filter, auth components
├── exception/                    # Global error handling
└── util/                         # Utilities, mappers, validators
    ├── validation/               # Custom validation annotations
    └── mapper/                   # Object mappers
```

**Nguyên tắc:** Mỗi tầng (layer) chỉ giao tiếp với tầng dưới liền kề. Controller → Service → Repository.

---

## 3. Design Patterns

### 3.1 MVC (Model-View-Controller)

Dù là REST API (không có View layer truyền thống), project áp dụng MVC pattern với:

| Layer | Trách nhiệm | Ví dụ |
|---|---|---|
| **Controller** | Nhận request, validate đầu vào, gọi service, trả response | `CoachController.java` |
| **Service** | Xử lý business logic, phối hợp nhiều repository | `CoachServiceImpl.java` |
| **Model/Entity** | Đại diện cho bảng trong DB, mapping JPA | `Coach.java` |
| **View** | React frontend (tách biệt hoàn toàn) | `SelectSeatPage.jsx` |

### 3.2 DTO Pattern (Data Transfer Object)

Tách biệt entity khỏi API contract. Request DTO (đầu vào) và Response DTO (đầu ra) riêng biệt.

```
Entity (JPA)  ←→  Repository  ←→  Service  ←→  Controller  ←→  Client
                      │                        │
                 Entity fields             DTO fields (khác nhau)
```

**Lợi ích:**
- Không expose entity trực tiếp ra API
- Có thể chọn field nào trả về (tránh circular reference)
- Request validation độc lập với entity constraint

### 3.3 Repository Pattern (DAO)

Spring Data JPA cung cấp CRUD mặc định qua interface. Các method đặc thù được định nghĩa bằng:
- **JPQL:** `@Query("SELECT ...")` với các named parameters
- **Native Query:** `@Query(value = "...", nativeQuery = true)` cho SQL phức tạp
- **Interface Projection:** `interface TripSummaryProjection { ... }` cho native queries

Ví dụ projection:
```java
public interface TripSummaryProjection {
    Integer getTripId();
    String getLicensePlate();
    String getCoachTypeName();
    BigDecimal getPrice();
    Integer getAvailableNormalSeats();
    Integer getTotalSeats();
}
```

### 3.4 Service Layer Pattern

Mỗi service gồm Interface + Implementation class riêng.

```
CoachService.java  (interface - contract)
       ↑
CoachServiceImpl.java  (implementation - business logic)
```

**Lý do:** Cho phép testing dễ dàng (mock interface), dễ thay đổi implementation.

### 3.5 Builder Pattern (Lombok)

Tất cả entity và DTO sử dụng Lombok `@Builder` cho object construction.

```java
@Builder
public class CoachResponse {
    private Integer coachId;
    private String licensePlate;
    // ...
}

// Usage:
CoachResponse response = CoachResponse.builder()
    .coachId(1)
    .licensePlate("29B-12345")
    .build();
```

### 3.6 Global Exception Handler Pattern

`@RestControllerAdvice` bắt toàn bộ exception, trả về JSON đồng nhất.

```
                    ┌─────────────────────┐
                    │  Controller Layer    │
                    └────────┬────────────┘
                             │ throws
                             ▼
               ┌─────────────────────────┐
               │  GlobalExceptionHandler  │
               │  (@RestControllerAdvice)  │
               └──────────┬──────────────┘
                          │ @ExceptionHandler
                          ▼
               ┌─────────────────────────┐
               │  ErrorResponse          │
               │  { status, message,     │
               │    timestamp, errors }   │
               └─────────────────────────┘
```

### 3.7 Filter Chain Pattern (Spring Security)

JWT authentication được implement như một `OncePerRequestFilter` trong filter chain.

```
Request → SecurityFilterChain
           ├── JwtAuthenticationFilter (extract & verify JWT)
           ├── UsernamePasswordAuthenticationToken (set authentication)
           ├── @PreAuthorize (role check)
           └── Controller (if authorized)
```

### 3.8 Frontend: Context Pattern (AuthContext)

Auth state được quản lý global qua React Context, tránh prop-drilling.

```
AuthProvider
  ├── state: { user, loading, isAuthenticated }
  ├── methods: { login, register, logout }
  └── children (entire app)
```

### 3.9 Frontend: Guard Pattern

Route protection dùng component guard pattern (tương tự middleware trong Angular).

```jsx
// RoleGuard checks role, then renders children or redirects
<Route element={<RoleGuard allowedRoles={['MANAGER', 'ADMIN']} />}>
  <Route path="/management/coaches" element={<CoachPage />} />
</Route>
```

### 3.10 Frontend: Feature-Based Module Structure

Mỗi tính năng là một module độc lập với đầy đủ routes, api, hooks, components.

```
features/
├── auth/
│   ├── index.js              # public exports
│   ├── context/AuthContext.jsx
│   ├── api/authApi.js
│   ├── config/firebase.js
│   └── util/authStorage.js
├── trips/
│   ├── index.js
│   ├── routes/PublicTripRoutes.jsx
│   ├── api/tripServices.js
│   └── components/SelectSeatPage.jsx
├── coaches/
│   ├── index.js
│   ├── routes/CoachRoutes.jsx
│   ├── hooks/useCoaches.js
│   ├── api/coachApi.js
│   └── components/...
└── ...
```

---

## 4. Kiến Trúc Database & Quan Hệ Entity

### 4.1 ER Diagram (Logical)

```
account ◄── account_role ──► role
   │
   ├── customer
   ├── staff ──► ticket_agency ──► coach_stop
   └── refresh_token

coach ──► coach_type ──► coach_type_price
  │            │
  ├── seat     └── (seat_layout JSON)
  │
  └── trip ──► route ──► route_stop ──► coach_stop
        │         │
        ├── trip_seat ◄── seat
        ├── passenger_ticket ◄── passenger_ticket_detail ◄── accompanied_child
        │         │
        │         └── payment ◄── refund
        ├── cargo_ticket ◄── cargo_ticket_detail ──► cargo_type_price ──► cargo_type
        │         │
        │         └── payment
        └── voucher
```

### 4.2 Entity Hierarchy

| Level | Loại entity | Ví dụ | Đặc điểm |
|---|---|---|---|
| **1** | Strong (độc lập) | account, role, voucher, coach_stop, route, coach_type, cargo_type | Không có FK bắt buộc |
| **2** | Associative | account_role, customer, staff, coach, route_stop, coach_type_price, cargo_type_price, seat | Có 1+ FK |
| **3** | Operational | trip, trip_seat | Kết quả của lịch chạy |
| **4** | Transactional | passenger_ticket, cargo_ticket | Giao dịch chính |
| **5** | Detail/Financial | passenger_ticket_detail, cargo_ticket_detail, payment | Chi tiết giao dịch |
| **6** | Sub-dependent | accompanied_child, refund, refresh_token | Phụ thuộc hoàn toàn |

### 4.3 BaseEntity Pattern

Mọi entity kế thừa `BaseEntity` với auditing fields:

```java
@MappedSuperclass
public abstract class BaseEntity {
    private LocalDateTime createdAt;    // tự động set khi tạo
    private String createdBy;           // người tạo
    private LocalDateTime updatedAt;    // tự động set khi update
    private String updatedBy;           // người sửa
}
```

### 4.4 Seat Layout JSON Schema

```json
{
  "totalFloors": 2,
  "rows": 5,
  "cols": 4,
  "floors": [
    [
      ["SEAT", "SEAT", "EMPTY", "SEAT"],
      ["SEAT", "SEAT", "SEAT", "SEAT"],
      ["EMPTY", "EMPTY", "EMPTY", "EMPTY"],
      ["SEAT", "EMPTY", "EMPTY", "SEAT"],
      ["SEAT", "DRIVER", "EMPTY", "SEAT"]
    ],
    [
      ["SEAT", "SEAT", "EMPTY", "SEAT"],
      ["SEAT", "SEAT", "SEAT", "SEAT"],
      ["EMPTY", "EMPTY", "EMPTY", "EMPTY"],
      ["SEAT", "EMPTY", "EMPTY", "SEAT"],
      ["SEAT", "EMPTY", "EMPTY", "SEAT"]
    ]
  ]
}
```

**Mã ghế:** Tầng 1: A01, A02... Tầng 2: B01, B02... (theo thứ tự row-col).

---

## 5. Luồng Dữ Liệu Chính

### 5.1 Customer Booking Flow

```
Browser                      Backend                        DB
  │                            │                            │
  │  GET /trips/home           │                            │
  │  ?date=&route=            │                            │
  │ ─────────────────────────►│  TripRepository             │
  │                           │  .findHomeTrips()           │
  │                           │ ──────────────────────────►│ native query
  │                           │ ◄──────────────────────────│ trip data
  │  Trip list (JSON)         │                            │
  │ ◄─────────────────────────│                            │
  │                            │                            │
  │  Select seats page        │                            │
  │ ─────────────────────────►│  (fetch seat layout)       │
  │                            │                            │
  │  Choose seat + info       │                            │
  │  + voucher                │                            │
  │ ─────────────────────────►│  Create passenger_ticket   │
  │                           │  + passenger_ticket_detail │
  │                           │  + update trip_seat status │
  │                           │ ──────────────────────────►│
  │  Ticket confirmed         │                            │
  │ ◄─────────────────────────│                            │
```

### 5.2 Authentication Flow (Staff)

```
Login Page          Backend                         DB
  │                   │                               │
  │ POST /auth/       │                               │
  │ staff/login       │                               │
  │ ─────────────────►│  AccountRepository            │
  │                   │  .findByUsername()            │
  │                   │ ────────────────────────────► │
  │                   │ ◄──────────────────────────── │ account + roles
  │                   │                               │
  │                   │  BCrypt.verify(password)      │
  │                   │  JwtService.generate(         │
  │                   │    account, roles)            │
  │                   │  RefreshTokenRepository.save()│
  │                   │ ────────────────────────────► │
  │  accessToken      │                               │
  │  + refreshToken   │                               │
  │ ◄─────────────────│                               │
```

### 5.3 Coach Creation with Seat Generation

```
Manager UI          Backend                         DB
  │                   │                               │
  │ POST /coaches     │                               │
  │ { licensePlate,   │                               │
  │   coachTypeId,    │                               │
  │   ... }           │                               │
  │ ─────────────────►│  CoachTypeRepository          │
  │                   │  .findById() ───────────────► │ get type + layout
  │                   │ ◄──────────────────────────── │
  │                   │                               │
  │                   │  Parse seatLayout JSON        │
  │                   │  For each "SEAT" cell:        │
  │                   │    create Seat(seatCode,      │
  │                   │      row, col, floor)         │
  │                   │                               │
  │                   │  Save coach                   │
  │                   │ ────────────────────────────► │
  │                   │  Save seats (batch)           │
  │                   │ ────────────────────────────► │
  │  Coach created    │                               │
  │ ◄─────────────────│                               │
```

---

## 6. Cơ Chế Xác Thực & Phân Quyền (Chi Tiết)

### 6.1 JWT Token Structure

```json
{
  "sub": "accountId",
  "username": "manager01",
  "roles": ["ROLE_MANAGER"],
  "scope": "ROLE_MANAGER",
  "iat": 1718000000,
  "exp": 1718086400
}
```

### 6.2 Refresh Token Cycle

```
Login ──► accessToken (24h) + refreshToken (7d)
              │
              ▼
     Axios interceptor:
     Response 401? ──► POST /auth/refresh-token
                            │
                            ▼
                    Verify refreshToken in DB
                    (not expired, not revoked)
                            │
                            ▼
                    Generate new accessToken
                    (rotate refreshToken)
                            │
                            ▼
                    Retry original request
```

### 6.3 Spring Security Filter Chain

```java
SecurityFilterChain (Spring Security 6)
├── .cors(Customizer.withDefaults())
├── .csrf(AbstractHttpConfigurer::disable)
├── .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
├── .authorizeHttpRequests(auth ->
│     requestMatchers(PUBLIC_ENDPOINTS).permitAll()
│     requestMatchers("/api/v1/coaches/**").hasAnyRole("ADMIN", "MANAGER")
│     .anyRequest().authenticated()
│   )
├── .authenticationProvider(daoAuthenticationProvider)
├── .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
└── .exceptionHandling(...)
```

### 6.4 So sánh cơ chế login: Staff vs Customer

| Tiêu chí | Staff | Customer |
|---|---|---|
| **Phương thức** | Username + Password | Firebase (Google, Facebook, SĐT) |
| **Hash** | BCrypt | Firebase handles |
| **Luồng** | Local DB verify | Firebase Admin SDK verify ID token |
| **Mục đích** | Nhân viên nội bộ | Khách hàng không cần tạo tài khoản phức tạp |

---

## 7. CORS & Security Config

### 7.1 CORS Policy

```java
@Bean
public CorsConfiguration corsConfiguration() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://localhost:3000", "http://localhost:2999"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowCredentials(true);
    config.setExposedHeaders(List.of("Authorization"));
    return config;
}
```

### 7.2 HTTPS

- Backend: port 9090 với PKCS12 keystore
- Customer frontend: port 3000 với `@vitejs/plugin-basic-ssl`
- Staff frontend: port 2999 (HTTP - dev)
- Thymeleaf (unused): vẫn khai báo
- Spring Session: lưu trên JDBC (DB)

---

## 8. Frontend Architecture Patterns

### 8.1 Component Tree

```
App
└── AppRouter (React Router v7)
    ├── PublicLayout
    │   ├── PublicHeader
    │   ├── HomePage
    │   │   └── (trip search form, results)
    │   ├── SelectSeatPage
    │   │   └── SeatIcon (reusable seat component)
    │   └── PublicFooter
    │
    ├── AuthLayout
    │   ├── Login
    │   └── Register
    │
    └── (Customer pages - protected)
```

### 8.2 Data Fetching Pattern

Sử dụng Axios instance duy nhất (`axiosClient.js`) với interceptors:

```javascript
// Interceptor: attach JWT token
axiosClient.interceptors.request.use(config => {
    config.headers.Authorization = `Bearer ${getAccessToken()}`;
    return config;
});

// Interceptor: auto refresh on 401
axiosClient.interceptors.response.use(
    response => response,
    async error => {
        if (error.response?.status === 401) {
            const newToken = await refreshAccessToken();
            error.config.headers.Authorization = `Bearer ${newToken}`;
            return axiosClient(error.config);
        }
        return Promise.reject(error);
    }
);
```

### 8.3 Feature Module Pattern (Staff App)

Mỗi feature là một module riêng trong `features/`:

```
features/coaches/
├── index.js               # Public exports
├── routes/CoachRoutes.jsx # Feature-specific routes
├── hooks/useCoaches.js    # Custom hooks for data fetching
├── api/coachApi.js        # API call functions
└── components/            # Feature-specific components
    ├── CoachForm.jsx
    ├── CoachTable.jsx
    └── SeatMapPreview.jsx
```

---

## 9. Key Design Decisions & Trade-offs

| Decision | Lý do | Trade-off |
|---|---|---|
| **Monolithic backend** | Đơn giản, phù hợp quy mô nhà xe | Khó scale riêng từng module |
| **2 separate React apps** | Tách biệt giao diện khách & nhân viên, bảo mật | Trùng code (axios, guards, formatters) |
| **JWT stateless** | Không cần server session, dễ scale | Token bị thu hồi khó (dùng refresh token table) |
| **SQL Server + Stored Proc** | Tận dụng scheduling phức tạp trong DB | Khó di chuyển DB, testing khó hơn |
| **Interface Projections** | Tối ưu query, chỉ lấy field cần | Phải maintain interface cho mỗi query |
| **SeatLayout JSON** | Linh hoạt, dễ thiết kế sơ đồ ghế đa dạng | Không thể query trực tiếp trong SQL |
| **Firebase Auth** | Giảm công sức xây dựng auth cho customer | Phụ thuộc bên thứ 3, tốn phí nếu vượt quota |
| **Soft delete pattern** | An toàn, dễ khôi phục | Phải luôn filter `WHERE isActive = 1` |

---

## 10. Exception Handling Standard

```
Client Error (4xx):
├── BusinessRuleException     (400) - Vi phạm logic kinh doanh
├── ResourceNotFoundException (404) - Không tìm thấy tài nguyên
├── MethodArgumentNotValid    (400) - Validation lỗi (jakarta.validation)
└── AuthenticationException   (401) - Sai credentials

Server Error (5xx):
└── Exception                 (500) - Lỗi không xác định

Response format:
{
    "status": 400,
    "message": "Tên loại xe đã tồn tại",
    "timestamp": "2026-06-18T10:30:00",
    "errors": ["coachTypeName: Tên loại xe không được để trống"]
}
```

---

## 11. Database Naming Conventions

| Quy tắc | Ví dụ |
|---|---|
| **Table name:** snake_case, số nhiều | `route_stops`, `coach_types` |
| **PK column:** table_name + Id | `route_stop_id` (Java: `routeStopId`) |
| **FK column:** reference_table + Id | `route_id`, `coach_id` |
| **Enum column:** is_ + tính từ | `is_active` |
| **Date column:** rõ ràng | `departure_time`, `start_effective_date` |
| **Unique constraint:** UQ_ + table + columns | `UQ_route_stop_route_id_stop_order` |
| **Soft delete:** `is_active` bit column | Không xóa vật lý (trừ bảng trung gian) |

---

## 12. Testing Strategy

| Tầng | Công cụ | Hiện trạng |
|---|---|---|
| **Unit Test (Backend)** | JUnit 5 | Chỉ có test mặc định `ApplicationTests.java` |
| **Integration Test** | Spring Boot Test | Chưa có |
| **Frontend Test** | Vitest? | Chưa có |
| **E2E Test** | Chưa xác định | Chưa có |

> **Ghi chú:** Hiện tại project chưa có test coverage. Cần bổ sung.
