# SDLC Report — Quản lý Nhân Viên (Manage Staff)

| SDLC Phase | Task / Activity | AI Output | Student's Validation / Modification | Evidence / Link | Quantitative Measure | Value Added (1-5) | Risks / Limitations Observed |
|---|---|---|---|---|---|---|---|
| **Planning / Requirements** | Write use case document (UC-STF01) | Drafted full use case covering entity definition, endpoint list, DTO specs, business rules, frontend components | Kept as-is; no modification needed | `documents/usecase-manage-staff.md` | 1 doc, ~90 lines, 5 sections (info, entity, endpoints, rules, frontend) | 5 | None — template matched existing `usecase-manage-ticket-agencies.md` exactly |
| **Design — Backend** | Create projections, request/response DTOs, repository native query, service interface | Generated 8 files: StaffListProjection, StaffFilterRequest, UpdateStaffRequest, StaffListResponse, StaffDetailResponse, StaffService (interface), + modified StaffRepository with `filterStaff()` native query | Added `@JsonProperty("active")` on `isActive` fields; validated SQL column aliases match projection getters; added `LOWER`/`ISNULL` for null-safe search | `dto/projection/staff/StaffListProjection.java`, `dto/request/staff/*.java` (2), `dto/response/staff/*.java` (2), `service/StaffService.java`, `repository/StaffRepository.java` | 7 new files + 1 modified = 8 files, ~100 LOC total, 3 endpoints designed, 4 filter params in native query | 4 | Risk of Jackson serialization mismatch if `@JsonProperty` omitted (caught proactively); SQL Server `ISNULL` not portable; column alias mismatch → silent runtime failure |
| **Implementation — Backend** | Create service implementation + REST controller | Generated `StaffServiceImpl` with filter/getDetail/update/toggleActive + `StaffController` with GET `/`, GET `/{id}`, PUT `/{id}`, PATCH `/toggle-active` | Removed unused `CoachStopDropdownDTO` from controller; simplified to only needed endpoints (no create/delete); verified `AccountRepository` injection | `service/impl/StaffServiceImpl.java`, `controller/StaffController.java` | 2 new files, ~160 LOC total, 4 REST endpoints, 4 service methods | 4 | Minor: `CoachStopRepository` injected but unused in StaffServiceImpl (copy-paste residue; no runtime impact) |
| **Implementation — Frontend** | Create API layer, hook, 4 components, page, routes, barrel export + update sidebar & app router | Generated 9 new files: staffApi.js, useStaff.js, StaffFilter.jsx, StaffTable.jsx, StaffDetailModal.jsx, StaffUpdateModal.jsx, StaffRoutes.jsx, index.js, StaffListPage.jsx + modified 2 files: SideBar.jsx, AppRouter.jsx | Added ticket agency dropdown to filter + update modal; added CCCD column to table; wired cross-module import `ticketAgencyApi` for dropdown data; placed route inside ADMIN guard | `features/manage-staff/` (9 files), `pages/admin/StaffListPage.jsx`, `components/layout/DesktopStaffLayout/SideBar.jsx`, `routes/AppRouter.jsx` | 9 new + 2 modified = 11 files, ~380 LOC total, 2 reusable hooks, 4 React components, 1 page, 1 route, 1 menu item | 5 | Hardcoded POSITIONS array (DRIVER, TICKET_STAFF, etc.) — must update manually if DB gains new positions; cross-module import to `manage-ticket-agencies` creates coupling (better: dedicated staff dropdown endpoint) |
| **Testing — Backend** | Compile Spring Boot project | — (no errors to fix) | `mvn compile -q` passed with zero errors | Terminal output | 0 errors, 0 warnings, 8 new + 1 modified Java files compiled | 5 | None |
| **Testing — Frontend** | Build React + Vite project | — (no errors to fix) | `npm run build` passed with zero errors | Terminal output | 0 errors, 557 modules transformed, 9 new + 2 modified frontend files bundled | 5 | Pre-existing warnings only (dynamic import chunking, bundle size) — unrelated to this module |

## Totals

| Metric | Count |
|--------|-------|
| New BE files | 8 |
| Modified BE files | 1 |
| New FE files | 9 |
| Modified FE files | 2 |
| Total files touched | 20 |
| Backend LOC (new) | ~260 |
| Frontend LOC (new) | ~380 |
| REST endpoints | 4 |
| React components | 4 + 1 page |
| Build errors | 0 |
