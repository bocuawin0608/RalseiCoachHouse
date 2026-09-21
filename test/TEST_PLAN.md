# Booking Black-box Test Plan

## Scenario B: View Trip List, Filter, Select Trip (failed rows only)

| ID | Layer | Regression contract |
|---|---|---|
| BK-TC-B02 | Playwright + Postman | A sold-out trip displays zero availability and cannot be selected |
| BK-TC-B03 | Playwright + Postman | Null `availableSeats` falls back to a valid `totalSeats` value |
| BK-TC-B06 | Playwright + Postman | Malformed timestamps never leak into a card |
| BK-TC-B07 | Playwright + Postman | Null, negative, and textual prices are rejected |
| BK-TC-B13 | Postman | A fifth time range is explicitly rejected, never silently dropped |
| BK-TC-B14 | Postman | `25:00-26:00` returns HTTP 400 |
| BK-TC-B18 | Postman | A fourth layout is explicitly rejected, never silently dropped |
| BK-TC-B19 | Playwright + Postman | “Under 300,000” excludes exactly 300,000 |
| BK-TC-B21 | Playwright + Postman | “Over 500,000” excludes 500,000 and has no hidden upper cap |
| BK-TC-B22 | Postman | Reversed price bounds return HTTP 400 |
| BK-TC-B26 | Playwright + Postman | A late stale response cannot replace the newest filter result |
| BK-TC-B34 | Playwright + Postman | A missing trip ID blocks selection |
| BK-TC-B37 | Playwright + Postman | A trip with no active available seat cannot be selected |
| BK-TC-B40 | Playwright + Postman | Overlapping price records cannot duplicate a trip card |
| BK-TC-B41 | Playwright + Postman | Route names containing extra hyphens remain complete |

Playwright owns browser behavior and deliberately controlled response fixtures.
Postman owns direct API validation against seeded integration data. B13, B14,
B18, and B22 have no meaningful browser control, so inventing a UI test for
them would test a mock rather than the product.

### Backend trace

`GET /api/v1/trips/home?advanced=true` binds `TripFilterRequest`, then
`TripServiceImpl#getFilteredTripDetails` normalizes filters and calls the native
`TripRepository#filterTrips` query. The React path is
`HomePage#executeSearch` -> `tripService.searchTrips` -> that endpoint.

Implementation safeguards captured by the tests:

- oversized time/layout lists and malformed time ranges return HTTP 400;
- price bounds are finite, non-negative, and ordered before SQL executes;
- UI labels with strict boundaries serialize decimal-exclusive API bounds;
- customer price selection chooses one deterministic effective record per trip;
- the UI validates timestamp, price, ID, and availability response fields;
- a monotonically increasing request ID prevents stale filter responses from winning;
- route rendering preserves all destination segments after the first hyphen.

## Scope

Customer booking from seat selection through creation, QR payment display, and cancellation. Tests run against the real React UI with controlled HTTP responses. They do not contact Firebase, SePay, email, Redis, or the production database.

## Preconditions

- Node.js 20+ and the customer frontend dependencies are installed.
- Install this suite once with `npm install --prefix test`.
- Run with `npm test --prefix test`.
- To target an already running deployment: `BASE_URL=https://host.example npm test --prefix test`.

## Scenarios

| ID | Source | Scenario | Expected result |
|---|---|---|---|
| TC-01 | UC01 normal + A2.2/A3.1 | Guest selects a seat, supplies a known/verified phone and valid details, then confirms | Confirm payload is correct and pending SePay QR page opens |
| TC-02 | Code-derived | No seat or sold seat selected | Continue remains disabled |
| TC-03 | Code-derived | Attempt to select 11 seats | Selection is capped at 10 |
| TC-04 | Code-derived/concurrency | Seat becomes unavailable while being locked | Stale selection is cleared and progression is blocked |
| TC-05 | Code-derived | Required passenger/route data and accompanied-child data are invalid | Inline validation prevents confirmation |
| TC-06 | UC01 A2.2/A3 | Unknown guest phone | OTP verification dialog is required |
| TC-07 | UC01 A4.2 + code-derived | Customer cancels pending QR payment | Transaction becomes failed and QR is disabled |
| TC-08 | Code-derived | Hold expires or backend rejects confirmation | Error remains on passenger step; payment is not opened |

## Use-case gaps and assumptions

- The document says OTP is only for a customer who is not logged in. The code actually verifies each unrecognized passenger phone; a known phone can bypass OTP.
- “Failed QR payment sends the customer home” differs from the code: cancellation stays on the payment page and shows a failed state.
- The lock-conflict message is currently cleared immediately when the seat map refresh starts. TC-04 therefore verifies the safety behavior (selection reset), while the missing feedback is a known UI defect.
- Passenger `Form.Label` elements are not associated with their controls (`htmlFor`/`id`), which weakens accessibility and prevents label-based automation. Tests use stable names/placeholders until the markup is corrected.
- Email delivery, final database seat state, owner revenue, and real payment completion are integration/system checks. Browser mocks cannot prove them. They require a SePay sandbox/webhook fixture plus email and database observability.
- Logged-in profile prefill and voucher application require a stable authentication fixture and are intentionally outside this guest suite.

## Status and evidence

Every test prints `[BOOKING TEST] <ID>: PASSED|FAILED|SKIPPED` immediately after completion. The complete latest run is written to:

- `test/reports/latest-status.json`
- `test/reports/html/index.html`
- failure traces/screenshots/videos under `test/reports/artifacts/`

No test should be reported as passed until the suite has actually run in the target environment.
