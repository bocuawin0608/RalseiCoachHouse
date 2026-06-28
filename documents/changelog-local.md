# Changelog — Cargo Tracking & Related Changes

All changes on `feature/cargoTracking` branch.

---

## 1. Backend — Cargo Tracking

| File | Change |
|------|--------|
| `CargoTrackingController.java` | `GET /api/v1/cargo-tracking/{ticketCode}` with `@CrossOrigin` (HTTP + HTTPS) |
| `CargoTrackingService.java` | Interface |
| `CargoTrackingServiceImpl.java` | Lookup: ticket → details → stops → trip → route → pricing |
| `CargoTicketRepository.java` | `findByTicketCode(String)` |
| `CargoTicketDetailRepository.java` | `findByCargoTicketId(int)` |
| `CargoTrackingResponse.java` | DTO with nested `CargoDetailItem` |
| `SecurityConfig.java` | Public permit for `/api/v1/cargo-tracking/**` |
| `CargoTypePriceRepository.java` | Already existed, used for unit lookup |

## 2. Frontend — Cargo Tracking

| File | Change |
|------|--------|
| `CargoTrackingPage.jsx` + `.css` | Status timeline, info cards, items table, error/loading states, search form |
| `cargoTrackingApi.js` | `trackByCode(ticketCode)` → `GET /v1/cargo-tracking/{code}` |
| `AppRouter.jsx` | Route `/tra-cuu` added |

## 3. Infrastructure & Config

| File | Change |
|------|--------|
| `application-dev.properties` | Created (gitignored): DB, JWT, Firebase, SSL, Redis, SePay, Goong |
| `application-API.properties` | Created (gitignored): API-only config |
| `.gitignore` | Added secrets patterns: `application-dev.properties`, `application-API.properties`, `firebase-service-account.json` |
| `Login.jsx` | Uncommented `setError` — was silently swallowing social login errors |

## 4. SSL / CORS Fix

- **Problem**: Browser `ERR_CERT_AUTHORITY_INVALID` because backend keystore changed after merging `main`
- **Solution**: Vite proxy so browser calls same origin (`https://localhost:3000`) → Vite forwards to backend

| File | Change |
|------|--------|
| `vite.config.js` | Added proxy: `/api` → `https://localhost:9090` (secure: false) |
| `axiosClient.js` | `baseURL` changed from `https://localhost:9090/api` → `/api` |

- Controller `@CrossOrigin` updated to allow both `http://localhost:3000` and `https://localhost:3000`

## 5. Merges

- Merged `origin/main` into `feature/cargoTracking` (resolved conflicts in `.gitignore`, `SecurityConfig.java`, `AppRouter.jsx`)

## 6. Documents (gitignored)

| File | Content |
|------|---------|
| `documents/changelog-cargo-tracking.md` | Full cargo tracking changelog |
| `documents/test-cases-cargo-tracking.md` | 22 test cases (8 BE, 14 FE) |
| `documents/usecase-cargo-tracking.md` | 3 use cases |
| `documents/changelog-voucher-crud.md` | Voucher CRUD changelog |
| `documents/changelog-local.md` | This file — all local session changes |

## Quick Start

```bash
# Backend (terminal 1)
cd backend-springboot
mvn spring-boot:run

# Frontend (terminal 2)
cd frontend-react
npm run dev

# Test
https://localhost:3000/tra-cuu
```
