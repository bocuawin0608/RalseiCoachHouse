# Ralsei coach house

![Ralsei coach house](frontend-react/public/images/GITHUB_README.png)

Coach ticketing and transport management system for routes between Quang Tri and Ha Noi. The repository contains a Spring Boot backend API, a customer-facing React application, a staff/management React application, and SQL Server database scripts.

---

## Overview

Ralsei coach house supports passenger trip search, seat booking, payment tracking, customer ticket history, cargo lookup, and operational management for routes, stops, coaches, coach types, trips, vouchers, cargo pricing, staff profiles, and staff-side passenger ticket operations.

The project is organized as a multi-application repository:

| Application | Path | Purpose |
| --- | --- | --- |
| Backend API | `backend-springboot` | Spring Boot REST API, persistence, security, payment, email, Firebase integration, and business logic |
| Customer frontend | `frontend-react` | Public website for customers: trip search, booking, authentication, profile, ticket history, and cargo lookup |
| Staff frontend | `frontend-react-staff` | Staff and manager UI for transport operations and passenger ticket handling |
| Database scripts | `backend-springboot/db` | SQL Server DDL, seed data, stored procedures, and schema changes |

---

## Features

| Area | Implemented features |
| --- | --- |
| Authentication | Customer Firebase login/register, staff local login, token refresh, logout, staff forgot-password flow |
| Customer booking | Trip search, seat map retrieval, seat locking/release, phone verification checks, price calculation, booking confirmation, payment status lookup, payment SSE stream, payment expiry/cancel endpoints |
| Customer account | Customer profile retrieval/update, account deletion |
| Customer history | Passenger ticket history, ticket detail lookup, QR image retrieval, ticket cancellation |
| Cargo lookup | Customer cargo order history lookup |
| Payments | Checkout endpoint, SePay IPN webhook, transaction status lookup |
| Route management | Route CRUD-style operations, route dropdowns, customer locations, soft delete and restore |
| Stop management | Coach stop and route stop creation/update/list/detail, route stop ordering, deletion or soft-delete/restore depending on resource |
| Coach management | Coach type management, coach price timeline, seat layout updates, coach CRUD-style operations, seat updates, maintenance/reactivation/retirement, status logs |
| Trip management | Public trip listing, trip stops, manager trip create/update/delete, trip summaries, available coaches/drivers/attendants, staff trip info |
| Voucher management | Voucher create/list/detail/update/delete and metrics |
| Cargo management | Cargo type and cargo type price management |
| Staff account | Staff profile retrieval/update and password change |
| Staff ticket operations | Passenger ticket search/detail, QR retrieval, passenger info change, full cancellation, seat lock/release, seat change, transfer candidates, itinerary preview, itinerary change |
| Notifications | HTML email templates for passenger ticket confirmation and cancellation |

---

## Technology Stack

| Category | Technology |
| --- | --- |
| Backend language | Java 17 |
| Backend framework | Spring Boot 3.5.14 |
| Backend modules | Spring Web, Spring Security, Spring Data JPA, Spring Validation, Spring Mail, Spring Thymeleaf, Spring Actuator, Spring Session JDBC, Spring Data Redis |
| Database | Microsoft SQL Server |
| Persistence | Hibernate/JPA, Spring Data repositories |
| Authentication | JWT, Spring Security, BCrypt, Firebase Admin SDK |
| Payment integration | SePay webhook and transaction configuration |
| QR generation | ZXing |
| Backend build tool | Maven Wrapper |
| Backend packaging | WAR |
| Frontend language | JavaScript with JSX |
| Frontend framework | React 19, Vite 8 |
| Frontend libraries | Axios, React Router, React Hook Form, React Bootstrap, Bootstrap, React Icons, DnD Kit |
| Customer state management | Redux Toolkit, React Redux |
| Customer realtime/SSE support | Server-sent events usage in booking payment flow; STOMP/SockJS dependencies are present |
| Deployment/config tools | Firebase emulator config for Auth; Vite dev proxy for customer app |

---

## Architecture

The backend follows a layered Spring Boot architecture under `backend-springboot/src/main/java/com/ralsei`.

| Layer | Package | Responsibility |
| --- | --- | --- |
| Application bootstrap | `application` | Spring Boot entry point and servlet initializer |
| Controller | `controller` | REST endpoints for auth, booking, payment, trips, routes, coaches, vouchers, cargo, customer account/history, staff account, and staff passenger tickets |
| Service | `service`, `service.impl` | Business logic, transaction boundaries, integrations, and orchestration |
| Specialized services | `service.passengerbooking`, `service.passengerticket`, `service.notification`, `service.ticketgenerator` | Booking workflow, staff ticket workflow, ticket email assembly/delivery, and ticket code generation |
| Repository | `repository` | Spring Data JPA repositories and projection-backed queries |
| Entity | `model` | JPA entities mapped to SQL Server tables |
| DTO/projection | `dto` | Request DTOs, response DTOs, notification DTOs, and query projections |
| Security | `security`, `config` | JWT request filter, Spring Security chain, authentication provider, password encoder, Firebase configuration |
| Exceptions | `exception` | Business/resource exceptions and global exception handling |
| Utilities | `util` | Formatting, date/time, phone, QR, PII masking, email, validation, and mapping helpers |

The frontends are feature-oriented React applications. Each feature generally contains its own API client, components, hooks, routes, styles, or utilities.

---

## Project Structure

```text
.
├── backend-springboot/
│   ├── db/
│   │   ├── ddl.sql
│   │   ├── fakedata.sql
│   │   ├── Procedure.sql
│   │   ├── alter_add_major_change_quota.sql
│   │   ├── MyQuerry(Duc).sql
│   │   └── seed_staff_ticket_test_0357756884.sql
│   ├── src/main/java/com/ralsei/
│   │   ├── application/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── exception/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── security/
│   │   ├── service/
│   │   └── util/
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── application-dev.properties
│   │   ├── firebase-service-account.json
│   │   ├── keystore.p12
│   │   └── templates/email/
│   ├── src/test/
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
├── frontend-react/
│   ├── public/
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   ├── features/
│   │   ├── hooks/
│   │   ├── pages/
│   │   ├── routes/
│   │   ├── stores/
│   │   └── utils/
│   ├── .env
│   ├── package.json
│   └── vite.config.js
├── frontend-react-staff/
│   ├── public/
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   ├── features/
│   │   ├── hooks/
│   │   ├── pages/
│   │   ├── routes/
│   │   ├── stores/
│   │   └── utils/
│   ├── package.json
│   └── vite.config.js
├── documents/
├── firebase.json
├── LICENSE
└── README.md
```

---

## Installation

### Requirements

| Requirement | Version or default | Used by |
| --- | --- | --- |
| Java JDK | 17 | Spring Boot backend |
| Node.js and npm | Node.js 20+ recommended for the React 19/Vite 8 toolchain | Customer and staff frontends |
| Microsoft SQL Server | Local instance reachable on port `1433` by default | Application database |
| Redis | Local instance reachable on port `6379` by default | Seat holds, concurrency-sensitive flows, and ticket code generation |
| Firebase project | Firebase Authentication enabled | Customer login/register and backend Firebase token verification |
| Firebase CLI | Optional | Local Firebase Auth emulator |
| Git | Any current version | Repository checkout |

Default local ports used by the repository:

| Service | Port |
| --- | --- |
| Backend API | `9090` |
| SQL Server | `1433` |
| Redis | `6379` |
| Customer frontend | `3000` |
| Staff frontend | `2999` |
| Firebase Auth emulator | `9099` |
| Firebase emulator UI | `4000` |

Clone the repository:

```bash
git clone <repository-url>
cd <repository-directory>
```

Install customer frontend dependencies:

```bash
cd frontend-react
npm install
```

Install staff frontend dependencies:

```bash
cd frontend-react-staff
npm install
```

The backend uses Maven Wrapper, so a local Maven installation is not required:

```bash
cd backend-springboot
./mvnw clean install
```

---

## Configuration

### Local Environment Setup

The project expects these local services before the full application can run:

1. SQL Server running with a database named `VeXeDB`.
2. Redis running on `localhost:6379`.
3. Backend HTTPS configured with a PKCS12 keystore.
4. Firebase Authentication configured for the customer frontend and Firebase Admin SDK configured for the backend.
5. Optional SMTP credentials for staff forgot-password and ticket email delivery.

The repository currently stores some environment-specific values in committed configuration files. For production or shared deployments, move secrets into environment variables or an external profile-specific configuration file.

### Backend

Common backend configuration is in `backend-springboot/src/main/resources/application.properties`.

| Property | Purpose |
| --- | --- |
| `spring.application.name` | Spring application name |
| `spring.profiles.active` | Active profile, defaults to `dev` through `SPRING_PROFILES_ACTIVE` |
| `spring.jpa.properties.hibernate.dialect` | SQL Server Hibernate dialect |
| `spring.jpa.hibernate.naming.physical-strategy` | Physical naming strategy |
| `spring.session.jdbc.initialize-schema` | JDBC session schema initialization |
| `spring.datasource.hikari.*` | HikariCP connection pool settings |
| `jwt.expiration` | Access token lifetime in milliseconds |
| `jwt.refresh.expiration` | Refresh token lifetime in milliseconds |

The `dev` profile is configured in `backend-springboot/src/main/resources/application-dev.properties`. The current local profile is configured for:

| Setting | Default local value in the project |
| --- | --- |
| Backend port | `9090` |
| Database name | `VeXeDB` |
| Database host | `localhost:1433` |
| Redis host | `localhost:6379` |
| Firebase Admin service account | `classpath:firebase-service-account.json` |
| Backend TLS keystore | `classpath:keystore.p12` |

Required backend configuration keys include:

| Key | Purpose |
| --- | --- |
| `server.port` | Backend HTTPS port |
| `spring.datasource.url` | SQL Server JDBC URL |
| `spring.datasource.username` | SQL Server username |
| `spring.datasource.password` | SQL Server password |
| `spring.datasource.driver-class-name` | SQL Server JDBC driver |
| `spring.jpa.hibernate.ddl-auto` | Hibernate schema mode |
| `firebase.service-account-key` | Firebase Admin service account file path |
| `spring.data.redis.host` | Redis host |
| `spring.data.redis.port` | Redis port |
| `server.ssl.key-store` | TLS keystore path |
| `server.ssl.key-store-password` | TLS keystore password |
| `server.ssl.key-store-type` | TLS keystore type |
| `server.ssl.key-alias` | TLS key alias |
| `jwt.secret` | JWT signing secret |
| `sepay.api.token` | SePay API token |
| `sepay.bank.account` | SePay bank account |
| `sepay.bank.name` | SePay bank name |
| `goong.api.key` | Goong API key |
| `goong.api.url` | Goong API base URL |
| `spring.mail.*` | SMTP configuration |
| `app.mail.from` | Sender email address |
| `app.mail.sender-name` | Sender display name |

The mail configuration reads these environment variables:

| Environment variable | Purpose |
| --- | --- |
| `MAIL_HOST` | SMTP host; defaults to `smtp.gmail.com` |
| `MAIL_PORT` | SMTP port; defaults to `587` |
| `MAIL_USERNAME` | SMTP username |
| `MAIL_PASSWORD` | SMTP password or app password |
| `MAIL_FROM` | Sender email address; defaults to `MAIL_USERNAME` |
| `MAIL_SENDER_NAME` | Sender display name; defaults to `Nhà xe Ralsei` |

Example local mail setup:

```bash
export MAIL_USERNAME="your-email@example.com"
export MAIL_PASSWORD="your-mail-app-password"
export MAIL_FROM="your-email@example.com"
export MAIL_SENDER_NAME="Ralsei coach house"
```

To run the backend against the Firebase Auth emulator, set:

```bash
export FIREBASE_AUTH_EMULATOR_HOST="localhost:9099"
```

The backend profile is selected through `SPRING_PROFILES_ACTIVE` and defaults to `dev`:

```bash
export SPRING_PROFILES_ACTIVE=dev
```

### Backend Local Config Template

Use this as a safe reference when creating a local profile. Do not commit real credentials.

```properties
server.port=9090

spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=VeXeDB;encrypt=true;trustServerCertificate=true;
spring.datasource.username=sa
spring.datasource.password=<your-sql-server-password>
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

firebase.service-account-key=firebase-service-account.json

spring.data.redis.host=localhost
spring.data.redis.port=6379

server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=<your-keystore-password>
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=springboot

jwt.secret=<at-least-32-byte-signing-secret>

sepay.api.token=<your-sepay-token>
sepay.bank.account=<your-sepay-bank-account>
sepay.bank.name=<your-sepay-bank-name>

goong.api.key=<your-goong-api-key>
goong.api.url=https://rsapi.goong.io/v2/geocode

spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=10000
spring.mail.properties.mail.smtp.writetimeout=10000
app.mail.from=${MAIL_FROM:${MAIL_USERNAME:}}
app.mail.sender-name=${MAIL_SENDER_NAME:Ralsei coach house}
```

### Customer Frontend

The customer frontend reads Firebase configuration from `frontend-react/.env`:

| Variable | Purpose |
| --- | --- |
| `VITE_FIREBASE_API_KEY` | Firebase web API key |
| `VITE_FIREBASE_AUTH_DOMAIN` | Firebase auth domain |
| `VITE_FIREBASE_PROJECT_ID` | Firebase project ID |
| `VITE_FIREBASE_STORAGE_BUCKET` | Firebase storage bucket |
| `VITE_FIREBASE_MESSAGING_SENDER_ID` | Firebase messaging sender ID |
| `VITE_FIREBASE_APP_ID` | Firebase app ID |
| `VITE_FIREBASE_MEASUREMENT_ID` | Firebase measurement ID |
| `VITE_USE_FIREBASE_EMULATOR` | Enables Firebase Auth emulator when set to `true` |
| `VITE_API_PROXY_TARGET` | Optional Vite proxy target; defaults to `https://localhost:9090` |

The customer Vite config forces browser API calls to `/api` and proxies `/api` to the Spring Boot backend during development.

Safe local `.env` template:

```env
VITE_FIREBASE_API_KEY=<your-firebase-web-api-key>
VITE_FIREBASE_AUTH_DOMAIN=<your-project>.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=<your-project-id>
VITE_FIREBASE_STORAGE_BUCKET=<your-project>.firebasestorage.app
VITE_FIREBASE_MESSAGING_SENDER_ID=<your-sender-id>
VITE_FIREBASE_APP_ID=<your-app-id>
VITE_FIREBASE_MEASUREMENT_ID=<your-measurement-id>

VITE_USE_FIREBASE_EMULATOR=false
VITE_API_PROXY_TARGET=https://localhost:9090
```

When using the Firebase Auth emulator:

```env
VITE_USE_FIREBASE_EMULATOR=true
```

### Staff Frontend

The staff frontend API client is configured directly with:

```text
https://localhost:9090/api
```

There is no `.env`-driven API base URL in the staff frontend at the moment.

---

## Running the Project

### First-Time Local Setup

1. Create the SQL Server database:

```sql
CREATE DATABASE VeXeDB;
```

2. Apply the database schema and seed scripts from `backend-springboot/db`.

Recommended order for a local development database:

```text
ddl.sql
fakedata.sql
Procedure.sql
alter_add_major_change_quota.sql
```

Use `seed_staff_ticket_test_0357756884.sql` only when staff passenger-ticket test data is needed.

3. Start Redis:

```bash
docker run -d --name ralsei-coach-house-redis -p 6379:6379 redis
```

If Redis is already installed locally, any equivalent Redis server on `localhost:6379` works.

4. Configure backend secrets and third-party credentials in `application-dev.properties` or in an external Spring profile.

5. Configure `frontend-react/.env` with Firebase web app values.

6. If using Firebase Auth emulator, start it from the repository root:

```bash
firebase emulators:start
```

### Backend

Start SQL Server and Redis, configure `application-dev.properties`, then run:

```bash
cd backend-springboot
./mvnw spring-boot:run
```

The backend is configured to use HTTPS when the SSL properties and keystore are available.

### Customer Frontend

Start the customer frontend after the backend is running:

```bash
cd frontend-react
npm run dev
```

Default development URL:

```text
https://localhost:3000
```

### Staff Frontend

Start the staff frontend after the backend is running:

```bash
cd frontend-react-staff
npm run dev
```

Default development URL:

```text
http://localhost:2999
```

### Database

Apply SQL Server scripts from `backend-springboot/db` as needed. The project does not contain an automated migration runner.

```text
ddl.sql
fakedata.sql
Procedure.sql
alter_add_major_change_quota.sql
seed_staff_ticket_test_0357756884.sql
```

### Startup Order

Use this order for local development:

```text
1. SQL Server
2. Redis
3. Firebase emulator, if enabled
4. Spring Boot backend
5. Customer frontend
6. Staff frontend
```

The repository does not include Docker configuration.

---

## API Documentation

Swagger/OpenAPI configuration is not present in the repository.

REST APIs are organized by controller:

| Controller | Base path | Responsibility |
| --- | --- | --- |
| `AuthController` | `/api/auth` | Customer/staff login, customer registration, staff forgot password, refresh token, logout |
| `PassengerBookingController` | `/api/v1/bookings` | Seat map, seat locks, phone checks, booking initialization, price calculation, booking confirmation, payment status, SSE payment stream, payment expiry/cancel |
| `PaymentController` | `/api/payment` | Checkout, SePay IPN webhook, transaction lookup |
| `TripController` | `/api/v1` | Public trips, trip stops, manager trip operations, staff trip info, available trip resources |
| `RouteController` | `/api/v1/routes` | Route creation/update/list/detail, dropdowns, customer locations, soft delete, restore |
| `RouteStopController` | `/api/v1/route-stops` | Route stop creation/update/detail/list, bulk order update, delete |
| `CoachStopController` | `/api/v1/coach-stops` | Coach stop creation/update/detail/list, soft delete, restore |
| `CoachTypeController` | `/api/v1/coach-types` | Coach type list/create/detail/update, dropdown, price management, seat layout, deactivation checks |
| `CoachController` | `/api/v1/coaches` | Coach list/create/detail/update, seat updates, status changes, status logs |
| `VoucherController` | `/api/v1/vouchers` | Voucher create/list/detail/update/delete and metrics |
| `CargoTypeController` | `/api/v1/manager/cargo-types` | Cargo type list/detail/create/update/soft-delete/restore |
| `CargoTypePriceController` | `/api/v1/manager/cargo-type-prices` | Cargo type price list/detail/create/update/delete |
| `CustomerAccountController` | `/api/v1/customer/me` | Customer profile and account operations |
| `CustomerTicketHistoryController` | `/api/v1/customer/history` | Customer passenger ticket history, detail, QR, cancellation |
| `CargoOrderLookupController` | `/api/v1/customer/cargo-history` | Customer cargo order lookup |
| `StaffAccountController` | `/api/v1/staff/me` | Staff profile and password operations |
| `StaffPassengerTicketController` | `/api/v1/staff/passenger-tickets` | Staff passenger ticket search/detail, QR, passenger changes, cancellation, seat changes, itinerary changes |
| `GoongController` | `/api/v2/goong` | Goong place autocomplete proxy |

---

## Database

The database engine is Microsoft SQL Server.

Main tables defined by `backend-springboot/db/ddl.sql` and mapped by JPA entities:

| Table | Purpose |
| --- | --- |
| `account`, `role`, `account_role`, `refresh_token` | Authentication, authorization, and refresh tokens |
| `customer`, `staff`, `ticket_agency` | Customer, employee, and agency profiles |
| `coach_stop`, `route`, `route_stop` | Stops, routes, and ordered stop relationships |
| `coach_type`, `coach_type_price`, `coach`, `seat`, `coach_status_log` | Coach categories, pricing, vehicles, seats, and status history |
| `trip`, `trip_seat` | Scheduled trips and trip-specific seat inventory |
| `passenger_ticket`, `passenger_ticket_detail`, `accompanied_child` | Passenger ticket orders, seat-level ticket details, and accompanied children |
| `cargo_type`, `cargo_type_price`, `cargo_ticket`, `cargo_ticket_detail` | Cargo categories, cargo pricing, cargo tickets, and cargo line items |
| `voucher` | Voucher definitions and usage limits |
| `payment`, `refund` | Payment and refund records |

Database scripts include schema creation, fake data, stored procedures, a passenger ticket schema alteration, and staff ticket test data. No Java migration tool such as Flyway or Liquibase is configured.

---

## Security

Implemented security mechanisms:

| Mechanism | Implementation |
| --- | --- |
| Spring Security | `SecurityConfig` configures CORS, disables CSRF for API usage, uses stateless sessions, and installs a JWT filter |
| JWT authentication | `JwtAuthenticationFilter` reads `Authorization: Bearer <token>` and populates Spring Security authorities from token roles |
| JWT generation | `JwtServiceImpl` signs access and refresh tokens with HS256 |
| Password hashing | Staff/local account passwords use BCrypt |
| Firebase authentication | Customer authentication verifies Firebase ID tokens through Firebase Admin SDK |
| Role-based authorization | Backend uses `@PreAuthorize`; frontends use route guards for `CUSTOMER`, `ADMIN`, `MANAGER`, `TICKET_STAFF`, and `TRIP_STAFF` |
| Refresh token persistence | Refresh tokens are stored through `RefreshTokenRepository` |
| Validation | Spring Validation is included and request DTOs use validation annotations where defined |

Public backend paths include authentication endpoints, payment endpoints, booking endpoints, public trip endpoints, and public route dropdown/customer-location endpoints.

---

## Development

### Backend Commands

```bash
cd backend-springboot
./mvnw clean install
./mvnw test
./mvnw spring-boot:run
```

### Customer Frontend Commands

```bash
cd frontend-react
npm run dev
npm run build
npm run lint
npm run preview
```

### Staff Frontend Commands

```bash
cd frontend-react-staff
npm run dev
npm run build
npm run lint
npm run preview
```

### Firebase Emulator

`firebase.json` configures the Firebase Auth emulator on port `9099` and emulator UI on port `4000`.

---

## Future Improvements

| Improvement | Reason |
| --- | --- |
| Add OpenAPI/Swagger documentation | The backend exposes many REST endpoints but has no generated API documentation |
| Move committed secrets out of the repository | Firebase service account, keystore, and environment-specific secrets should be provided through secure environment configuration |
| Add Docker Compose for local development | SQL Server, Redis, backend, and frontends currently require manual startup |
| Add CI workflows | No GitHub Actions workflow is present for backend tests, frontend linting, or builds |
| Introduce Flyway or Liquibase | SQL scripts are present, but schema versioning is not automated |
| Externalize the staff frontend API base URL | The staff frontend currently hardcodes `https://localhost:9090/api` |
| Expand automated tests | The backend has a basic Spring Boot test; broader service/controller coverage would reduce regression risk |

---

## Contributors

Contributors from Git history:

| Name | Email |
| --- | --- |
| bocuawin0608 | `doanngocduc01102006@gmail.com` |
| Gin | `131593532+RootGin@users.noreply.github.com` |
| Gin | `rootgin@proton.me` |
| loliconhihi | `doanngocduc01102006@gmail.com` |
| loliconhihi | `doanngocduc2006@gmail.com` |
| RootGin | `trungthephantom@gmail.com` |
| Thanh | `imthegoatonyt@gmail.com` |
| Yoshi | `hameo.kapy@gmail.com` |

---

## License

This project is licensed under the MIT License. See `LICENSE` for details.
