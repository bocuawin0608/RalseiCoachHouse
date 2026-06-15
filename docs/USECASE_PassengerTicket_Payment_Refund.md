# Use Case Document: Passenger Ticket, Payment, Refund

> **Project:** Nhà Xe Tuấn MV — Voucher & Ticketing System  
> **Domain:** Passenger Transport, Cargo Transport, Payment Processing  
> **Audience:** Developers, QA, Product Managers, Business Stakeholders  
> **Version:** 1.0  
> **Date:** 2026-06-15

---

## Table of Contents

1. [UC-PT-001: Create/Book Passenger Ticket](#uc-pt-001-createbook-passenger-ticket)
2. [UC-PT-002: Cancel/Change Passenger Ticket](#uc-pt-002-cancelchange-passenger-ticket)
3. [UC-PT-003: Check-In Passenger (Boarding)](#uc-pt-003-check-in-passenger-boarding)
4. [UC-PAY-001: Process Passenger Ticket Payment](#uc-pay-001-process-passenger-ticket-payment)
5. [UC-PAY-002: Process Cargo Ticket Payment](#uc-pay-002-process-cargo-ticket-payment)
6. [UC-RF-001: Request Refund](#uc-rf-001-request-refund)
7. [UC-RF-002: Process/Execute Refund](#uc-rf-002-processexecute-refund)

---

## UC-PT-001: Create/Book Passenger Ticket

**Use Case ID:** UC-PT-001  
**Name:** Create/Book Passenger Ticket

**Primary Actors:**  
- Ticket Staff (TICKET_STAFF) — operates the ticket selling interface  
- Customer (walk-in or registered) — purchases the ticket

**Secondary Actors:**  
- Voucher System — validates and applies discount vouchers  
- Trip/Seat System — manages trip schedules and seat availability (Trip, TripSeat, Route, CoachStop)  
- Payment Gateway (VNPAY/Bank) — processes the linked payment  
- Notification System — sends confirmation and QR code to customer

**Description:**  
Ticket Staff creates a new passenger ticket by selecting a scheduled trip, assigning available seats, capturing passenger details per seat, optionally applying a voucher, and initiating payment. The system enforces seat availability, route validity, voucher rules, and creates linked PassengerTicket + PassengerTicketDetail records with PENDING status. Seats are temporarily locked for 15 minutes to await payment completion.

**User Story:**  
As a Ticket Staff, I want to book a passenger ticket with seat selection and optional voucher so that the customer receives a confirmed reservation with QR code for boarding.

**Preconditions:**
1. Trip exists in the `trip` table with status = `'SCHEDULED'` and `departureTime` > current time + 30 minutes (minimum booking window).
2. At least one `TripSeat` exists for the trip with status = `'AVAILABLE'`.
3. Pickup and dropoff `CoachStop` records exist on the trip's Route; `pickupStopId` ≠ `dropoffStopId`.
4. If a voucher code is provided:
   - `Voucher` exists in the `voucher` table.
   - `Voucher.status` = `'ACTIVE'`.
   - `Voucher.startEffectiveDate` ≤ NOW() ≤ `Voucher.endEffectiveDate`.
   - `Voucher.usedCount` < `Voucher.usageLimit`.
   - `Voucher.minOrderValue` ≤ calculated total price before discount.
5. Staff account exists in the `staff` table with at least one role of `TICKET_STAFF`, `MANAGER`, or `ADMIN`.
6. Backend server is running on port 9090 and database connection is active.
7. If the customer is registered: a `Customer` record exists with the provided `customerId`.

**Postconditions:**

*On Success:*
1. A `PassengerTicket` record is created in the `passenger_ticket` table:
   - `status` = `'PENDING'`
   - `ticketCode` = `'TKT-YYYYMMDD-XXXX'` (YYYYMMDD = current date, XXXX = 4-digit sequence, guaranteed unique)
   - `totalPrice` = Σ detail prices − voucher discount (capped at `voucher.maxDiscountValue`)
   - `soldBy` = authenticated staff ID
   - `voucherId` = ID of applied voucher (NULL if none)
   - `customerId` = ID of registered customer (NULL if walk-in)
2. One `PassengerTicketDetail` record per selected seat:
   - `status` = `'PENDING'`
   - `tripSeatId` = selected seat ID
   - `price` = `TripSeat.price`
   - `qrcode` = NULL (generated after payment in UC-PAY-001)
   - `expiredAt` = NULL
3. Corresponding `TripSeat` records updated: `status` = `'LOCKED'` (seats held for 15 minutes).
4. If voucher was applied: voucher `usedCount` is NOT incremented yet (deferred until payment confirmation in UC-PAY-001).
5. Payment is initiated (redirect to UC-PAY-001).
6. API response: `{ passengerTicketId, ticketCode, totalPrice, paymentUrl (if VNPAY), expiresAt (lock timeout) }`.

*On Failure:*
- No records are created in any table.
- All selected seats remain in `'AVAILABLE'` status.
- An appropriate error message is returned.

**Normal Sequence/Flow:**

1. Staff navigates to the "Bán vé" page (`/staff/ticket/sell`) in the Internal Portal.
2. System displays the trip search interface with filters: route, date, departure time range.
3. Staff selects a trip and clicks "Chọn chuyến."
   - System validates the trip exists and `status` = `'SCHEDULED'`.
   - System validates `departureTime` > NOW() + 30 minutes.
4. System retrieves the coach seat map and displays it visually, color-coding each `TripSeat` by status:
   - Green = `'AVAILABLE'`
   - Yellow = `'LOCKED'`
   - Red = `'SOLD'`
   - Gray = aisle / non-seat area
5. Staff clicks on one or more available seats to select them.
   - System sends `UPDATE trip_seat SET status='LOCKED', updatedAt=NOW() WHERE tripSeatId IN (...) AND status='AVAILABLE'`.
   - If the optimistic lock fails (seat already taken), an error is returned (see E1).
6. For each selected seat, the system displays a passenger info form. Staff enters:
   - `fullName` (required, NVARCHAR up to 100 characters).
   - `phone` (required, Vietnamese mobile format: `[0-9]{10,11}`).
   - `dob` (required, must be ≥ 12 years old; children < 12 must use AccompaniedChild on a parent's detail).
   - `email` (optional, must be valid email format if provided).
7. Staff optionally enters a voucher code.
   - System retrieves the `Voucher` by `voucherCode`.
   - Validates: `status` = `'ACTIVE'`, dates within range, `usedCount` < `usageLimit`, `minOrderValue` ≤ `subtotal`.
   - Calculates discount:
     - If `discountType` = `'PERCENT'`: `discount = min(subtotal × discountValue / 100, maxDiscountValue)`.
     - If `discountType` = `'FIXED'`: `discount = min(discountValue, maxDiscountValue)` (maxDiscountValue acts as cap).
8. System calculates:
   - `subtotal` = SUM of all selected seat prices.
   - `discount` = voucher discount (0 if no voucher).
   - `totalPrice` = `subtotal` − `discount`.
9. Staff selects `pickupStop` and `dropoffStop` from a dropdown of stops on the trip's route.
   - System validates `pickupStopId` ≠ `dropoffStopId`.
   - System validates both stops exist in the trip's `route_stop` list and are in correct order (pickup before dropoff).
10. Staff optionally selects "Thêm trẻ em đi kèm" for an adult passenger detail.
    - Staff enters child's `fullname`, `dob`.
    - System validates child is < 12 years old.
11. Staff clicks "Xác nhận đặt vé."
12. System begins a database transaction:
    - Generates `ticketCode` = `'TKT-' + yyyyMMdd + '-' + LPAD(nextSequence, 4, '0')`.
    - `INSERT INTO passenger_ticket (...) VALUES (...)` — status = `'PENDING'`.
    - `INSERT INTO passenger_ticket_detail (...) VALUES (...)` — one per seat, status = `'PENDING'`.
    - If accompanied child: `INSERT INTO accompanied_child (...) VALUES (...)`.
    - `UPDATE trip_seat SET status='LOCKED', updatedAt=NOW() WHERE tripSeatId IN (...)`.
13. System starts a 15-minute seat lock timer (background scheduled job releases locks on timeout — see E6).
14. System commits the transaction and returns the ticket details to the frontend.
15. System redirects the Staff/Customer to the payment page (see UC-PAY-001), passing `passengerTicketId` and `totalPrice`.

**Alternative Sequence/Flow:**

*A1 — Walk-in Customer (No customerId)*  
- At step 6: The customer is not registered in the system.  
- Staff skips the customer lookup step.  
- `customerId` remains NULL in the `passenger_ticket` record.  
- Ticket is sold purely on the provided passenger details (phone number is the primary contact).  
- All other steps proceed identically.

*A2 — No Voucher Applied*  
- At step 7: Staff proceeds without entering a voucher code.  
- `discount` = 0, `totalPrice` = `subtotal`.  
- `voucherId` = NULL in the `passenger_ticket` record.  
- All other steps proceed identically.

*A3 — Multiple Passengers in One Booking*  
- At step 5: Staff selects 3 seats.  
- At step 6: System displays 3 passenger info forms. Staff fills in different passenger names.  
- A single `PassengerTicket` is created with 3 linked `PassengerTicketDetail` records.  
- `totalPrice` = sum of all 3 seat prices minus one voucher discount (voucher applies to entire booking).

*A4 — Accompanied Child Added*  
- At step 10: Staff checks "Thêm trẻ em đi kèm" for a specific adult passenger detail.  
- System creates an `AccompaniedChild` record linked to that `ticketDetailId`.  
- The `UNIQUE` constraint on `AccompaniedChild.ticketDetailId` ensures each adult seat can have at most one child.
- The child travels on the same seat as the adult (no additional `TripSeat` is consumed).
- Child does NOT have a separate `PassengerTicketDetail` or QR code.

*A5 — Voucher Covers Full Amount (totalPrice = 0)*  
- At step 8: `discount` ≥ `subtotal`, so `totalPrice` = 0.  
- System bypasses the payment gateway redirect.  
- Instead, system creates a `Payment` record with `amount` = 0, `status` = `'COMPLETED'`, `paymentMethod` = `'VOUCHER'`, `transactionId` = `'VOUCHER_FULL_' + ticketCode`.  
- System immediately transitions the ticket to `'CONFIRMED'`:
  - `UPDATE passenger_ticket SET status='CONFIRMED'`.
  - `UPDATE passenger_ticket_detail SET status='CONFIRMED', qrcode=generateQR(detailId), expiredAt=trip.departureTime + 2h`.
  - `UPDATE trip_seat SET status='SOLD' WHERE tripSeatId IN (...)`.
  - `UPDATE voucher SET usedCount = usedCount + 1`.
- QR codes are generated and returned directly without a payment step.

**Exceptional Sequence/Flow:**

*E1 — Seat Concurrency Conflict (Optimistic Lock Failure)*  
- At step 5: The `UPDATE trip_seat` query uses `WHERE status='AVAILABLE'`. It affects 0 rows because another staff member already locked or sold the seat between the seat map render and the booking click.
- The transaction is rolled back.
- System returns error:  
  `{ "errorCode": "ERR_SEAT_001", "message": "Ghế [A1] đã được chọn bởi nhân viên khác. Vui lòng chọn ghế khác.", "details": { "conflictingSeatIds": [456] } }`
- Frontend refreshes the seat map to show current statuses.
- Staff must re-select available seats.

*E2 — Voucher Validation Failed*  
- At step 7: One or more voucher checks fail:
  - `status` ≠ `'ACTIVE'`: `"ERR_VOUCHER_001: Voucher không tồn tại hoặc đã bị vô hiệu hóa"`
  - `endEffectiveDate` < NOW(): `"ERR_VOUCHER_002: Voucher đã hết hạn sử dụng"`
  - `usedCount` ≥ `usageLimit`: `"ERR_VOUCHER_003: Voucher đã hết lượt sử dụng"`
  - `minOrderValue` > `subtotal`: `"ERR_VOUCHER_004: Đơn hàng chưa đạt giá trị tối thiểu để áp dụng voucher (cần [X]đ)"`
- System displays the specific error on the voucher input field.
- Staff can retry with a different voucher code or proceed without voucher.

*E3 — Trip Not Bookable*  
- At step 3: The trip's `status` ≠ `'SCHEDULED'` (it may be `'CANCELLED'`, `'IN_PROGRESS'`, or `'COMPLETED'`).
- Or `departureTime` ≤ NOW() + 30 minutes.
- System returns error:  
  `{ "errorCode": "ERR_TRIP_001", "message": "Chuyến xe không khả dụng để bán vé. Vui lòng chọn chuyến khác." }`
- Staff is returned to the trip search screen.

*E4 — Route Validation Failed*  
- At step 9: `pickupStopId` = `dropoffStopId`, or one of the stops does not exist on the trip's route, or the pickup stop is after the dropoff stop in route order.
- System returns error:  
  `{ "errorCode": "ERR_TICKET_001", "message": "Điểm đón/trả không hợp lệ hoặc trùng nhau. Vui lòng chọn lại." }`

*E5 — Database Constraint Violation (ticketCode Duplicate)*  
- At step 12: The auto-generated `ticketCode` happens to collide with an existing row (extremely rare, but possible under high concurrency).
- The `INSERT` fails with a unique constraint violation.
- The system catches the exception and retries with a new `ticketCode` (re-generates the 4-digit sequence portion).
- If retry fails 3 consecutive times:  
  `{ "errorCode": "ERR_SYS_001", "message": "Không thể tạo mã vé do lỗi hệ thống. Vui lòng thử lại." }`
- Transaction is rolled back entirely.

*E6 — Seat Lock Timeout (Payment Not Completed)*  
- A background scheduled job runs every minute and queries:
  ```sql
  SELECT pt.passengerTicketId
  FROM passenger_ticket pt
  WHERE pt.status = 'PENDING'
    AND pt.createdAt < DATEADD(MINUTE, -15, GETDATE())
  ```
- For each expired lock:
  - `UPDATE passenger_ticket SET status='CANCELLED', updatedAt=NOW()`
  - `UPDATE passenger_ticket_detail SET status='CANCELLED' WHERE passengerTicketId=?`
  - `UPDATE trip_seat SET status='AVAILABLE' WHERE tripSeatId IN (SELECT tripSeatId FROM passenger_ticket_detail WHERE passengerTicketId=?)`
- If the customer eventually tries to pay (UC-PAY-001) after the lock expired, UC-PAY-001 will return error `"ERR_PAYMENT_003: Hết thời gian giữ ghế, vé đã bị hủy. Vui lòng đặt vé mới."`

---

## UC-PT-002: Cancel/Change Passenger Ticket

**Use Case ID:** UC-PT-002  
**Name:** Cancel/Change Passenger Ticket

**Primary Actors:**  
- Ticket Staff (TICKET_STAFF) — handles cancellation and change requests  
- Customer — initiates the request via staff

**Secondary Actors:**  
- Refund System — triggers refund workflow (UC-RF-001)  
- Trip/Seat System — releases and reassigns seats  
- Voucher System — restores voucher usage count  
- Notification System — informs customer of cancellation/change confirmation

**Description:**  
Staff cancels or modifies an existing passenger ticket. Cancellation releases all seats, triggers a refund if the ticket was paid, and restores the voucher usage count. Change reassigns seats and/or trip with a price difference settled via additional payment or refund. Both operations respect configurable cancellation policies (time windows, fare rules).

**User Story:**  
As a Ticket Staff, I want to cancel or change a passenger ticket so that the customer can receive proper refund/credit for cancellations or adjust their travel plan for changes.

**Preconditions:**

*For Cancellation:*
1. `PassengerTicket` exists with the given `ticketCode` or `passengerTicketId`.
2. `PassengerTicket.status` ∈ `{'PENDING', 'CONFIRMED', 'CHANGED'}`. (A ticket that is already `'CANCELLED'` or has `departureTime` ≤ NOW() − 2h cannot be cancelled.)
3. Current time < `departureTime` − `cancellationWindowHours` (default: 2 hours before departure).
4. Staff has role `TICKET_STAFF`, `MANAGER`, or `ADMIN`.

*For Change:*
- Preconditions 1-4 above (change is only allowed before departure and within window).
5. A new trip exists with status `'SCHEDULED'`, `departureTime` > NOW() + 30 minutes, and with available seats on the same route or a compatible route.
6. If the ticket had a voucher, the voucher must be re-validated for the new trip (if it applies).

**Postconditions:**

*On Success (Cancel):*
1. `PassengerTicket.status` = `'CANCELLED'`. All `PassengerTicketDetail.status` = `'CANCELLED'`.
2. All associated `TripSeat.records`: `status` = `'AVAILABLE'`.
3. If a `Payment` exists with `status` = `'COMPLETED'` and refundable amount > 0: a `Refund` record is created with `status` = `'PENDING'` (see UC-RF-001).
4. If a voucher was applied: `voucher.usedCount` = `usedCount` − 1.
5. Notification is sent to the customer (SMS to passenger phone, or email if available):
   - "Vé [ticketCode] chuyến [departureTime] [routeName] đã được hủy thành công."
   - If refund initiated: "Yêu cầu hoàn tiền [amount]đ đang được xử lý."
6. API response: `{ cancelled: true, refundAmount, refundId (if applicable) }`.

*On Success (Change):*
1. New `PassengerTicketDetail` records are created for the new seats; old records' `status` = `'CANCELLED'`.
2. Old `TripSeat.status` = `'AVAILABLE'`; new `TripSeat.status` = `'LOCKED'` → `'SOLD'` (after payment).
3. `PassengerTicket.tripId`, `pickupStopId`, `dropoffStopId` updated to new values; `status` = `'CHANGED'`.
4. If price increased: additional payment initiated via UC-PAY-001 for the difference.
5. If price decreased: refund initiated via UC-RF-001 for the difference.
6. Voucher re-validated and re-applied (usedCount may need adjustment if discount changes).

*On Failure:*
- No changes to any records. Original ticket remains as-is.
- Appropriate error returned.

**Normal Sequence/Flow (Cancel):**

1. Staff searches for the ticket by `ticketCode` or `passengerTicketId` from the ticket management screen.
2. System retrieves and displays: `PassengerTicket`, linked `PassengerTicketDetail`s, `Payment` (if exists), `Voucher` (if applied), and `Trip` info.
3. System validates:
   - `PassengerTicket.status` ∈ `{'PENDING', 'CONFIRMED', 'CHANGED'}`.
   - `Trip.departureTime` > NOW() + `cancellationWindowHours` (2 hours).
4. Staff clicks "Hủy vé."
5. System displays a confirmation dialog showing:
   - Ticket code, customer name, route, departure time.
   - Refundable amount (if paid): `payment.amount − payment.refundAmount`.
   - Confirmation message: "Bạn có chắc muốn hủy vé này?"
6. Staff confirms cancellation.
7. System begins a database transaction:
   - `UPDATE passenger_ticket SET status='CANCELLED', updatedAt=NOW(), updatedBy=staffId WHERE passengerTicketId=?`
   - `UPDATE passenger_ticket_detail SET status='CANCELLED' WHERE passengerTicketId=?`
   - `UPDATE trip_seat SET status='AVAILABLE' WHERE tripSeatId IN (SELECT tripSeatId FROM passenger_ticket_detail WHERE passengerTicketId=?)`
   - If voucher was applied (`voucherId IS NOT NULL`):
     - `UPDATE voucher SET usedCount = usedCount − 1 WHERE voucherId=? AND usedCount > 0`
   - If payment exists, `status='COMPLETED'`, and `payment.amount > payment.refundAmount`:
     - Create `Refund` record: `status='PENDING'`, `amount=payment.amount−payment.refundAmount`, `reason='CANCELLATION'`
     - The refund will be processed by Finance in UC-RF-002.
8. System commits the transaction.
9. Notification is sent to the customer.
10. Staff sees success screen with cancellation details.

**Normal Sequence/Flow (Change):**

1. Steps 1-3 as in Cancel (validate the ticket is changeable).
2. Staff clicks "Đổi vé."
3. System shows the same trip/seat selection UI as UC-PT-001 but pre-filled with current ticket data.
4. Staff selects a new trip (same route or compatible route) and new seats.
5. System validates new trip status and seat availability, locks new seats (`'LOCKED'` status).
6. System calculates:
   - `newSubtotal` = SUM of new seat prices.
   - `newDiscount` = re-validate voucher for new trip (voucher may not apply if new trip price is lower or if voucher expired).
   - `newTotalPrice` = `newSubtotal` − `newDiscount`.
   - `priceDiff` = `newTotalPrice` − `oldTicket.totalPrice`.
7. Staff confirms the change.
8. System begins a database transaction:
   - `UPDATE passenger_ticket SET tripId=?, pickupStopId=?, dropoffStopId=?, totalPrice=newTotalPrice, status='CHANGED', updatedAt=NOW(), updatedBy=staffId`
   - Cancel old details: `UPDATE passenger_ticket_detail SET status='CANCELLED' WHERE passengerTicketId=?`
   - Create new details: `INSERT INTO passenger_ticket_detail (...) VALUES (...)` for each new seat.
   - Release old seats: `UPDATE trip_seat SET status='AVAILABLE' WHERE tripSeatId IN (old IDs)`
   - Lock new seats: `UPDATE trip_seat SET status='LOCKED' WHERE tripSeatId IN (new IDs)`
9. If `priceDiff` > 0: Initiate UC-PAY-001 for amount = `priceDiff`.
10. If `priceDiff` < 0: Initiate UC-RF-001 for amount = `|priceDiff|`, reason = `'CHANGE_PRICE_DIFFERENCE'`.
11. If `priceDiff` = 0: No payment or refund needed; new seats are directly set to `'SOLD'`.
12. Commit transaction.
13. Notification sent to customer with new ticket details.
14. Staff sees change confirmation with any additional payment/refund amount.

**Alternative Sequence/Flow:**

*A1 — Partial Cancellation (Some Passengers Only)*  
- At step 4: Staff selects only a subset of `PassengerTicketDetail` records to cancel, leaving others active.
- Only the selected details are set to `'CANCELLED'`; their seats are released.
- The remaining details stay `'CONFIRMED'`.
- `totalPrice` is recalculated: `totalPrice` = `oldTotalPrice` − (sum of cancelled seat prices) (voucher discount is NOT recalculated — the remaining passengers bear the full discount).
- If the original voucher was applied to the entire booking and only some passengers cancel, the voucher's `usedCount` is NOT decremented (the booking still used the voucher).

*A2 — Change to Different Route (Compatible Route)*  
- At step 4: The new trip belongs to a different route that still covers the customer's pickup/dropoff stops.
- The voucher must be re-validated for the new route and new trip price.
- If the voucher's `minOrderValue` is no longer met, the voucher is removed and `totalPrice` recalculated without discount.

*A3 — Same Trip, Different Seats (Seat Swap)*  
- At step 4: `tripId` stays the same; only seat assignments change.
- Voucher is unchanged (same trip, same price).
- `priceDiff` depends only on new seat prices vs old seat prices.

**Exceptional Sequence/Flow:**

*E1 — Non-Refundable Fare or Past Cancellation Window*  
- At step 3: `Trip.departureTime` ≤ NOW() + `cancellationWindowHours`.
- Or the ticket's fare rules (if implemented in the future) mark it as non-refundable.
- System returns error:  
  `{ "errorCode": "ERR_TICKET_002", "message": "Vé không thể hủy/đổi do đã quá thời hạn hủy vé (cần hủy trước giờ khởi hành [N] tiếng) hoặc loại vé không hoàn tiền." }`
- Staff can inform the customer of the policy. If exceptional circumstances (e.g., trip cancellation by the company), MANAGER/ADMIN override is required.

*E2 — New Seats Unavailable During Change*  
- At step 5: The optimistic lock on new seats fails (seat was taken between display and confirmation).
- Transaction rolled back.
- Error: `"ERR_SEAT_002: Ghế mới không còn trống, vui lòng chọn ghế khác."`
- Old ticket and seats remain unchanged.
- Staff returns to seat selection.

*E3 — Voucher No Longer Valid for New Trip*  
- At step 6: Re-validation of voucher fails for the new trip (e.g., new trip price < `minOrderValue`, or voucher expired).
- System warns: `"ERR_VOUCHER_005: Voucher hiện tại không áp dụng cho chuyến xe mới. Vé sẽ được tính giá gốc."`
- Staff can proceed without the voucher or select a different trip.

*E4 — Payment/Refund Failure During Change*  
- At step 9 or 10: UC-PAY-001 fails (payment gateway down) or UC-RF-001 fails (refund amount invalid).
- The entire transaction is rolled back: new seats released, old seats restored, ticket unchanged.
- Error: `"ERR_PAYMENT_004: Xử lý thanh toán/hoàn tiền thất bại. Vé không thay đổi. Vui lòng thử lại sau."`

*E5 — Trip Already Departed*  
- At step 3: `Trip.departureTime` < NOW().
- System returns error:  
  `"ERR_TRIP_002: Chuyến xe đã khởi hành. Không thể hủy/đổi vé sau giờ khởi hành."`

---

## UC-PT-003: Check-In Passenger (Boarding)

**Use Case ID:** UC-PT-003  
**Name:** Check-In Passenger (Boarding)

**Primary Actors:**  
- Trip Attendant (TRIP_STAFF) — scans QR codes and validates boarding  
- Passenger — presents QR code for scanning

**Secondary Actors:**  
- Trip/Seat System — validates trip and seat association  
- Notification System — (optional) confirms check-in to customer

**Description:**  
The Trip Attendant scans a passenger's QR code at the boarding gate. The system validates that the passenger ticket detail is `'CONFIRMED'`, the QR code has not expired, the trip is in the boarding window, and the attendant is assigned to this trip. On success, the detail status is updated to `'CHECKED_IN'`, allowing the passenger to board.

**User Story:**  
As a Trip Attendant, I want to quickly scan QR codes to verify boarding eligibility so that only valid passengers board the correct trip and seat.

**Preconditions:**
1. `PassengerTicketDetail` exists with `qrcode` NOT NULL and `status` = `'CONFIRMED'`.
2. `PassengerTicketDetail.expiredAt` > NOW() (the QR code's grace period has not expired).
3. Parent `PassengerTicket.status` = `'CONFIRMED'`.
4. `Trip.status` = `'SCHEDULED'` or `'IN_PROGRESS'` (not `'COMPLETED'`, `'CANCELLED'`).
5. Current time is within the boarding window: `Trip.departureTime` − 1 hour ≤ NOW() ≤ `Trip.departureTime` + 2 hours (2-hour grace period after departure).
6. The Trip Attendant's `staffId` matches `Trip.attendantId` (or has `MANAGER`/`ADMIN` role for override).

**Postconditions:**

*On Success:*
1. `PassengerTicketDetail.status` = `'CHECKED_IN'`.
2. `updatedAt` and `updatedBy` are set to current time and attendant ID.
3. System displays the passenger's name, seat number, pickup/dropoff stops, and a "CHECKED IN" badge.
4. If all details for the `PassengerTicket` are now `'CHECKED_IN'`, the parent ticket's `status` remains `'CONFIRMED'` (no upgrade to a different status).
5. API response: `{ checkedIn: true, passengerName, seatNumber, pickupStop, dropoffStop, tripInfo }`.

*On Failure:*
- `PassengerTicketDetail.status` is unchanged.
- An appropriate error message is displayed to the attendant.

**Normal Sequence/Flow:**

1. Attendant opens the "Quét vé" page (`/staff/trip/scan`) on a mobile device.
2. System displays the list of trips assigned to this attendant for today (where `trip.attendantId` = staff's `staffId` and `trip.departureTime` is within the boarding window).
3. Attendant selects the active trip → the device camera activates for QR scanning.
4. Passenger presents their QR code (from their phone, email, or paper printout).
5. Attendant scans the QR code.
   - System decodes the QR content to extract `ticketDetailId` (the QR encodes `passengerTicketDetailId` + timestamp + HMAC signature for security).
   - System verifies the QR signature to prevent forgery.
6. System retrieves the `PassengerTicketDetail` by `ticketDetailId`, including its parent `PassengerTicket` and the linked `Trip`.
7. System performs validations:
   - `PassengerTicketDetail.status` = `'CONFIRMED'`.
   - `PassengerTicketDetail.expiredAt` > NOW().
   - `PassengerTicket.status` = `'CONFIRMED'`.
   - `Trip.status` ∈ `{'SCHEDULED', 'IN_PROGRESS'}`.
   - `Trip.departureTime` − 1h ≤ NOW() ≤ `Trip.departureTime` + 2h.
   - The selected trip in the app matches `Trip.tripId`.
   - The attendant's `staffId` = `Trip.attendantId` (or has override role).
8. On validation success:
   - `UPDATE passenger_ticket_detail SET status='CHECKED_IN', updatedAt=NOW(), updatedBy=attendantId WHERE ticketDetailId=? AND status='CONFIRMED'`.
9. System displays a success screen:
   - Passenger name, seat number (from `Seat.seatLabel`), pickup/dropoff stops.
   - A green checkmark and "Check-in thành công" message.
10. Attendant allows the passenger to board.
11. Attendant taps "Tiếp theo" → camera activates for the next passenger.

**Alternative Sequence/Flow:**

*A1 — Manual Check-In (QR Unreadable)*  
- At step 5: The QR code is damaged, smudged, or the camera cannot scan it.
- Attendant taps "Nhập thủ công."
- Attendant enters the passenger's `ticketCode` and phone number (or fullName).
- System searches for a `PassengerTicketDetail` matching the `ticketCode` (from parent `PassengerTicket`) and phone number.
- If found, proceeds to step 7.

*A2 — Re-Check-In (Already Checked In)*  
- At step 7: `PassengerTicketDetail.status` = `'CHECKED_IN'` (already scanned earlier).
- System does NOT return an error; instead, it shows a warning:
  - "Hành khách đã check-in lúc HH:mm. Cho phép lên xe."
- The attendant visually confirms the passenger and allows boarding.
- No status update is performed (same status).

*A3 — Bulk Check-In (Future Enhancement)*  
- Attendant uploads a CSV passenger list from the office before departure.
- System batch-updates all matching details to `'CHECKED_IN'` in a single transaction.
- Useful for pre-booked group tours.

**Exceptional Sequence/Flow:**

*E1 — Invalid QR Code (Decode Failure)*  
- At step 5: The scanned content does not match the expected QR format (missing fields, wrong length, HMAC signature mismatch).
- Error: `"ERR_CHECKIN_001: Mã QR không hợp lệ. Vui lòng thử quét lại hoặc nhập thủ công."`
- Attendant attempts manual check-in (A1).

*E2 — Detail Not Confirmed (Unpaid Ticket)*  
- At step 7: `PassengerTicketDetail.status` = `'PENDING'` (payment not completed).
- Error: `"ERR_CHECKIN_002: Vé chưa được thanh toán. Vui lòng hoàn tất thanh toán trước khi lên xe."`
- Attendant directs passenger to the ticket counter.

*E3 — QR Code Expired (Beyond Grace Period)*  
- At step 7: `PassengerTicketDetail.expiredAt` < NOW().
- Error: `"ERR_CHECKIN_003: Mã QR đã hết hạn (quá [N] tiếng sau giờ khởi hành). Vui lòng liên hệ quầy vé để được hỗ trợ."`
- Passenger must visit the ticket counter for a QR refresh or manual validation.

*E4 — Wrong Trip*  
- At step 7: The scanned detail's `Trip.tripId` does not match the trip the attendant selected in the app.
- Error: `"ERR_CHECKIN_004: Vé này thuộc chuyến xe khác (tuyến [routeName], khởi hành [departureTime])."` 
- Attendant may switch to the correct trip in the app, or direct the passenger to the correct boarding gate.

*E5 — Trip Not Boarding (Too Early / Too Late)*  
- At step 7:
  - If NOW() < `Trip.departureTime` − 1h: `"ERR_CHECKIN_005: Chuyến xe chưa đến giờ check-in. Vui lòng quay lại sau."`
  - If NOW() > `Trip.departureTime` + 2h: `"ERR_CHECKIN_006: Đã quá thời gian check-in cho phép. Vui lòng liên hệ quầy vé."`

*E6 — Unauthorized Attendant*  
- At step 7: The attendant's `staffId` ≠ `Trip.attendantId` and their role is not `MANAGER` or `ADMIN`.
- Error: `"ERR_AUTH_001: Bạn không được phân công cho chuyến xe này. Vui lòng liên hệ quản lý để được phân ca."`
- The attendant must contact the manager to be assigned to the trip or to use an override code.

---

## UC-PAY-001: Process Passenger Ticket Payment

**Use Case ID:** UC-PAY-001  
**Name:** Process Passenger Ticket Payment

**Primary Actors:**  
- Customer — the payer (initiates online payment)  
- Payment Gateway (VNPAY, Bank) — processes the actual financial transaction  
- Ticket Staff (TICKET_STAFF) — handles CASH payments at the counter

**Secondary Actors:**  
- Voucher System — increments `usedCount` after successful payment  
- Ticket System — confirms ticket and generates QR codes  
- Notification System — sends payment confirmation

**Description:**  
Processes payment for a `'PENDING'` passenger ticket. The system strictly enforces a **CHECK constraint** (`CK_Payment_Target`): exactly one of `passengerTicketId` or `cargoTicketId` must be provided, never both and never neither. Supports three payment methods: `VNPAY` (online gateway), `BANK_TRANSFER` (manual verification), and `CASH` (counter collection). On successful payment, the ticket is confirmed, QR codes are generated for each passenger detail, voucher usage is incremented, and seat status transitions to `'SOLD'`.

**User Story:**  
As a Customer, I want to pay for my ticket via VNPAY, bank transfer, or cash so that my seat reservation is confirmed and I receive a QR code for boarding.

**Preconditions:**
1. `PassengerTicket` exists with `passengerTicketId`, `status` = `'PENDING'`.
2. No `Payment` record exists for this `passengerTicketId` with `status` = `'COMPLETED'` (prevents double payment).
3. Exactly one of `passengerTicketId` or `cargoTicketId` is provided — enforced by DB constraint `CK_Payment_Target`.
4. `amount` = `ticket.totalPrice` − sum of prior `Payment.amount` (where `status='COMPLETED'`) > 0 (except for voucher-full-coverage case in UC-PT-001 A5).
5. `paymentMethod` ∈ `{'VNPAY', 'BANK_TRANSFER', 'CASH'}`.
6. For `VNPAY`: Customer has a VNPAY-enabled banking app or internet banking.
7. For `BANK_TRANSFER`: Staff can provide bank account details for the transfer.
8. For `CASH`: Staff is present at the counter with a cash register.
9. If a voucher was applied (`voucherId` NOT NULL): the voucher's `usedCount` < `usageLimit` is re-validated (race condition guard).

**Postconditions:**

*On Success (VNPAY / Bank Transfer / CASH):*
1. `Payment` record created/updated:
   - `status` = `'COMPLETED'`
   - `paymentTime` = NOW()
   - `refundAmount` = 0 (initialized, may increase later with refunds)
   - `transactionId` = unique per method: `'VNPAY_' + timestamp + '_' + random(6)` or `'BANK_' + timestamp` or `'CASH_' + timestamp`
   - `callbackData` = raw gateway response (for CASH, an empty JSON `{}`)
2. `PassengerTicket.status` = `'CONFIRMED'` — all `PassengerTicketDetail.status` = `'CONFIRMED'`.
3. QR codes generated for each detail:
   - `qrcode` = Base64-encoded JSON: `{ ticketDetailId, timestamp, hmac }`
   - The HMAC uses a server-side secret to prevent forgery.
4. `PassengerTicketDetail.expiredAt` = `Trip.departureTime` + 2 hours (allows boarding up to 2 hours after departure).
5. If a voucher was applied: `UPDATE voucher SET usedCount = usedCount + 1 WHERE voucherId=? AND usedCount < usageLimit`.
6. `TripSeat.status` = `'SOLD'` for all locked seats from UC-PT-001.
7. Notification sent to customer:
   - "Thanh toán thành công! Mã vé [ticketCode]. Vui lòng kiểm tra email/SMS để nhận mã QR lên xe."
8. API response: `{ paymentId, transactionId, status='COMPLETED', qrCodes[{ detailId, qrData }], ticketCode }`.

*On Failure:*
- `Payment.status` = `'FAILED'` (gateway error, timeout, signature invalid) or remains `'PENDING'` (awaiting async callback).
- `PassengerTicket.status` remains `'PENDING'`; seats remain `'LOCKED'` (subject to 15-min timeout, UC-PT-001 E6).
- Customer can retry payment.

**Normal Sequence/Flow (VNPAY — Online Gateway):**

1. Customer/Staff is on the payment page after UC-PT-001 (or from ticket history for a PENDING ticket).
2. System displays:
   - Ticket summary: `ticketCode`, departure date, route, total price.
   - Available payment methods: VNPAY, Bank Transfer, Cash.
   - Lock countdown timer showing remaining seat-hold time.
3. Customer selects "VNPAY" as the payment method.
4. System creates a `Payment` record in `'PENDING'` status with a unique `transactionId`:
   ```sql
   INSERT INTO payment (
     passengerTicketId, cargoTicketId, amount, paymentMethod,
     transactionId, status, refundAmount, createdBy
   ) VALUES (
     :passengerTicketId, NULL, :amount, 'VNPAY',
     'VNPAY_' + FORMAT(GETDATE(),'yyyyMMddHHmmss') + '_' + RIGHT(NEWID(),6),
     'PENDING', 0.00, :staffOrCustomerId
   )
   ```
   - Note: `cargoTicketId` = NULL, `passengerTicketId` = ticket ID (satisfies `CK_Payment_Target`).
5. System calls the VNPAY API:
   - Constructs a payment request with `vnp_TxnRef` = `transactionId`, `vnp_Amount` = `amount * 100` (VNPAY uses VND × 100), `vnp_ReturnUrl` = frontend success URL, `vnp_IpnUrl` = backend callback endpoint.
   - Signs the request with the VNPAY secret key (HMAC-SHA512).
   - VNPAY returns a `paymentUrl`.
6. System returns the `paymentUrl` to the frontend; the frontend redirects the customer to VNPAY's payment page.
7. Customer authenticates on VNPAY (enters card number, OTP, or uses banking app QR).
8. After the customer completes payment, VNPAY makes two callbacks:
   - **IPN (Instant Payment Notification):** Server-to-server POST to `POST /api/payment/vnpay/ipn` (this is the authoritative callback).
   - **Return URL:** Browser redirect to `vnp_ReturnUrl` (informational only — the system must trust the IPN, not the return URL, for status updates).
9. System processes the IPN callback:
   - Validates `vnp_SecureHash` = HMAC-SHA512 of all fields using the VNPAY secret key.
   - Validates `vnp_TxnRef` matches the `transactionId` in the `Payment` record.
   - Validates `vnp_Amount` = `payment.amount × 100`.
   - Validates `vnp_ResponseCode` = `'00'` (success).
   - If all checks pass:
     ```sql
     BEGIN TRANSACTION
       UPDATE payment SET status='COMPLETED', paymentTime=NOW(), callbackData=:rawJson WHERE transactionId=:vnp_TxnRef
       UPDATE passenger_ticket SET status='CONFIRMED', updatedAt=NOW() WHERE passengerTicketId=:id
       UPDATE passenger_ticket_detail SET status='CONFIRMED', qrcode=:qrData, expiredAt=:expiry WHERE passengerTicketId=:id
       UPDATE trip_seat SET status='SOLD' WHERE tripSeatId IN (:seatIds)
       IF :voucherId IS NOT NULL: UPDATE voucher SET usedCount = usedCount + 1 WHERE voucherId=:voucherId AND usedCount < usageLimit
     COMMIT
     ```
   - Returns HTTP 200 with `{ "RspCode": "00", "Message": "Confirm Success" }` to VNPAY.
10. VNPAY redirects the customer to the frontend `returnUrl` with result parameters. The frontend displays the success page (or error page) based on the parameter values.

**Normal Sequence/Flow (BANK_TRANSFER — Manual Verification):**

1-3. Same as VNPAY (steps 1-3), but customer selects "Chuyển khoản."
4. System creates a `Payment` record with `paymentMethod='BANK_TRANSFER'`, `status='PENDING'`.
5. System displays the company bank account details:
   - Bank name, account number, account holder name.
   - Transfer content/message: `'TT_' + transactionId` (include this in the transfer note).
6. Customer makes the transfer via their banking app.
7. Staff monitors the bank account (manually) for incoming transfers matching the expected content.
8. Staff finds a match and clicks "Xác nhận chuyển khoản" on the system.
9. System updates `Payment.status='COMPLETED'`, `paymentTime=NOW()`, and executes the same confirmation logic as step 9 of VNPAY above (update ticket, details, seats, voucher).
10. System prints a receipt (optional) and displays confirmation.

**Normal Sequence/Flow (CASH — Counter Collection):**

1-3. Same as VNPAY (steps 1-3), but staff selects "Tiền mặt."
4. Staff collects the exact cash amount from the customer.
5. System creates a `Payment` record with `paymentMethod='CASH'`, and directly sets `status='COMPLETED'`, `paymentTime=NOW()`.
   - No gateway callback needed — the transaction is instantly confirmed.
6. System executes the same confirmation logic: update ticket, details (generate QR codes), seats, voucher.
7. System prints a receipt for the customer:
   - Ticket code, seat numbers, departure time, route, amount paid, QR codes.
8. Staff hands the receipt to the customer.

**Alternative Sequence/Flow:**

*A1 — Partial Payment (Deposit)*
- At step 4: `amount` < `totalPrice` (e.g., 50% deposit).
- `Payment.status` = `'COMPLETED'` for the partial amount.
- `PassengerTicket.status` remains `'PENDING'` (not `'CONFIRMED'`).
- Seats remain `'LOCKED'` (with a longer timeout, e.g., 72 hours).
- Customer can come back to pay the remainder. Each partial payment creates a separate `Payment` record.
- Ticket is confirmed only when `SUM(payment.amount) >= totalPrice`.

*A2 — Multiple Payments for One Ticket*  
- After A1, the customer returns to pay the balance.
- A second `Payment` record is created for the remaining amount.
- When `SUM(payment.amount) >= totalPrice`, the ticket transitions to `'CONFIRMED'`.
- The system checks this condition after each payment completion.

*A3 — Voucher Full Coverage (totalPrice = 0)*  
- See UC-PT-001 A5.
- Payment is created with `amount=0`, `paymentMethod='VOUCHER'`, `status='COMPLETED'` immediately.
- No gateway call or cash collection needed.

**Exceptional Sequence/Flow:**

*E1 — VNPAY Gateway Timeout (No Callback in 15 Minutes)*  
- At step 8: VNPAY does not send an IPN callback within 15 minutes of creating the `paymentUrl`.
- A background job (`@Scheduled(fixedDelay=60000)`) detects:
  ```sql
  SELECT p.paymentId FROM payment p
  WHERE p.status = 'PENDING'
    AND p.paymentMethod = 'VNPAY'
    AND p.createdAt < DATEADD(MINUTE, -15, GETDATE())
  ```
- For each expired payment:
  - `UPDATE payment SET status='FAILED', callbackData='{"reason":"TIMEOUT"}' WHERE paymentId=?`
  - `UPDATE passenger_ticket SET status='CANCELLED' WHERE passengerTicketId=?`
  - `UPDATE passenger_ticket_detail SET status='CANCELLED' WHERE passengerTicketId=?`
  - `UPDATE trip_seat SET status='AVAILABLE' WHERE tripSeatId IN (SELECT tripSeatId FROM passenger_ticket_detail WHERE passengerTicketId=?)`
- Customer sees error: `"ERR_PAYMENT_001: Cổng thanh toán không phản hồi. Vé và ghế đã được giải phóng. Vui lòng đặt vé mới."`

*E2 — VNPAY Signature Invalid*  
- At step 9: `vnp_SecureHash` does not match the computed HMAC-SHA512.
- Possible causes: tampered callback, wrong secret key, mismatched parameter order.
- `UPDATE payment SET status='FAILED', callbackData=:rawJson WHERE transactionId=:vnp_TxnRef`
- Returns HTTP 200 with `{ "RspCode": "97", "Message": "Invalid Signature" }` to VNPAY.
- Customer sees error on return URL: `"ERR_PAYMENT_008: Giao dịch không hợp lệ (chữ ký không khớp). Vui lòng liên hệ hỗ trợ."`

*E3 — Amount Mismatch*  
- At step 9: `vnp_Amount` ≠ `payment.amount × 100`.
- Possible causes: race condition, price changed between ticket creation and payment.
- `UPDATE payment SET status='FAILED', callbackData=:rawJson`
- Returns HTTP 200 with `{ "RspCode": "04", "Message": "Amount Mismatch" }`.
- Customer sees: `"ERR_PAYMENT_002: Số tiền thanh toán không khớp. Vui lòng liên hệ quầy vé."`

*E4 — CHECK Constraint Violation (CK_Payment_Target)*  
- This error occurs at the database level during `INSERT INTO payment`.
- If both `passengerTicketId` and `cargoTicketId` are provided, or both are NULL:
  ```sql
  CONSTRAINT CK_Payment_Target CHECK (
    ([passengerTicketId] IS NOT NULL AND [cargoTicketId] IS NULL) OR 
    ([passengerTicketId] IS NULL AND [cargoTicketId] IS NOT NULL)
  )
  ```
- The database throws a check constraint violation (SQL Server error 547).
- System catches the `DataIntegrityViolationException` and returns:
  `"ERR_PAYMENT_005: Thanh toán phải liên kết với chính xác 1 vé (vé khách hoặc vé hàng). Không được để trống hoặc cung cấp cả hai."`
- This is a system-level error — it should never occur in normal operation if the API layer validates properly.

*E5 — Double Payment (Idempotency)*  
- Customer clicks "Pay" twice, or VNPAY sends duplicate IPN callbacks.
- Before step 9: System checks `SELECT status FROM payment WHERE transactionId=?`.
  - If `status` = `'COMPLETED'`: Return `{ "RspCode": "02", "Message": "Order already confirmed" }` (no update, idempotent).
  - If `status` = `'FAILED'`: Proceed with update (revive the payment).
- Customer sees no error — the second callback is silently ignored.

*E6 — Voucher Exhausted at Payment Time (Race Condition)*  
- At step 9: `UPDATE voucher SET usedCount = usedCount + 1 WHERE voucherId=? AND usedCount < usageLimit`.
- If `usedCount >= usageLimit` (another ticket was paid between UC-PT-001 and this payment):
  - The update affects 0 rows (does not error — the WHERE clause silently no-ops).
  - The ticket and payment are still confirmed (the voucher benefit was already included in `totalPrice`).
  - System logs a warning for manual reconciliation: `"Voucher [code] exceeded usage limit; ticket [ticketCode] confirmed with voucher discount applied."`
  - Admin should review and adjust `usedCount` if needed.

*E7 — Database Failure During Confirmation*  
- At step 9: Any SQL error (connection loss, deadlock, disk full) during the confirmation transaction.
- The entire transaction is rolled back.
- `Payment.status` remains `'PENDING'`.
- The ticket remains `'PENDING'`, seats remain `'LOCKED'`.
- System returns HTTP 500 to VNPAY: `{ "RspCode": "99", "Message": "Unknown Error" }`.
- VNPAY will retry the IPN callback (up to 5 times with exponential backoff).
- Background job monitors stuck `'PENDING'` payments and alerts engineering.

---

## UC-PAY-002: Process Cargo Ticket Payment

**Use Case ID:** UC-PAY-002  
**Name:** Process Cargo Ticket Payment

**Primary Actors:**  
- Cargo Staff (TICKET_STAFF or CARGO_STAFF) — manages payment for shipments  
- Sender / Receiver — the party responsible for paying the shipping fee  
- Payment Gateway (VNPAY, Bank)

**Secondary Actors:**  
- Cargo System — updates cargo ticket status after payment  
- Notification System — notifies sender/receiver of payment confirmation

**Description:**  
Processes payment for a cargo ticket (`CargoTicket`). This use case mirrors UC-PAY-001 but operates on `cargoTicketId` instead of `passengerTicketId`, satisfying the `CK_Payment_Target` constraint (exactly one of the two must be set, and in this case `passengerTicketId` = NULL). The payment amount is the cargo ticket's `totalPrice` (excluding `codAmount`, which is collected separately at delivery). The payer is determined by `CargoTicket.feePayer` (`'SENDER'` or `'RECEIVER'`).

**User Story:**  
As a Cargo Staff, I want to collect payment for a cargo shipment so that the cargo is accepted for transport and loaded onto the assigned trip.

**Preconditions:**
1. `CargoTicket` exists with `cargoTicketId`, `status` ∈ `{'RECEIVED'}` (not yet loaded).
2. `CargoTicket.totalPrice` > 0 (cargo fee).
3. Exactly one of `cargoTicketId` provided, `passengerTicketId` = NULL (satisfies `CK_Payment_Target`).
4. `paymentMethod` ∈ `{'VNPAY', 'BANK_TRANSFER', 'CASH'}`.
5. `feePayer` ∈ `{'SENDER', 'RECEIVER'}` determines who pays.
6. `codAmount` (Cash on Delivery, if > 0) is tracked separately and collected at delivery (not part of this payment).
7. Amount to collect = `totalPrice` − `codAmount` (the COD is collected by the driver at dropoff).

**Postconditions:**

*On Success:*
1. `Payment` record created with `paymentMethod`, `status='COMPLETED'`, `cargoTicketId` set, `passengerTicketId` = NULL.
2. `CargoTicket.status` = `'PAID'` (the cargo can now proceed to loading).
3. `CargoTicket.feePayer` recorded so the system knows who settled the bill.
4. Notification sent to sender/receiver (depending on `feePayer`).
5. If `codAmount` > 0: A separate collection at delivery is still pending.

*On Failure:* Payment fails; cargo ticket remains `'RECEIVED'`; cargo may not be loaded.

**Normal Sequence/Flow (VNPAY / Bank Transfer / CASH):**
The flow is identical to UC-PAY-001 (steps 1-10) with the following substitutions:
- `passengerTicketId` = NULL, `cargoTicketId` = ticket ID.
- Amount = `totalPrice` − `codAmount` (CODs are collected at delivery).
- On confirmation: `UPDATE cargo_ticket SET status='PAID'` instead of confirming a passenger ticket.
- No QR codes are generated (cargo uses a printed waybill, not digital QR for boarding).

**Alternative Sequence/Flow:**

*A1 — COD Payment at Delivery*  
- At dropoff: The driver collects `codAmount` from the receiver.
- A separate `Payment` record is created with `paymentMethod='CASH'`, `amount=codAmount`, `cargoTicketId` set.
- This payment happens later (in a separate UC not detailed here).
- The `codAmount` is later disbursed to the seller/sender.

*A2 — Fee Payer = Receiver (Payment After Delivery)*  
- The cargo is loaded and delivered before payment is collected.
- `CargoTicket.feePayer` = `'RECEIVER'`.
- Payment is collected at delivery (similar to COD).
- The cargo ticket status may advance to `'DELIVERED'` even before payment is received, but `'PAID'` is set after payment.

**Exceptional Sequence/Flow:**

*E1 — CK_Payment_Target Violation*  
- Same as UC-PAY-001 E4 but for cargo:
  - `passengerTicketId` should be NULL, `cargoTicketId` should be set.
  - Error: `"ERR_PAYMENT_005: Thanh toán hàng hóa phải liên kết với vé hàng. Không được liên kết với vé khách."`

*E2 — Weight/Volume Mismatch*  
- At payment time: The actual cargo weight/volume (checked during loading) differs significantly from the `CargoTicketDetail` at booking.
- The price may need to be recalculated before payment.
- Staff must update the `CargoTicketDetail.calculatedPrice` first, then proceed with payment.

*E3 — Fee Payer Dispute*  
- Sender and receiver disagree on who should pay the shipping fee.
- Cargo is held at the station until the dispute is resolved.
- Manager override can force the payment.

---

## UC-RF-001: Request Refund

**Use Case ID:** UC-RF-001  
**Name:** Request Refund

**Primary Actors:**  
- Customer — initiates the refund request  
- Ticket Staff (TICKET_STAFF) — submits the request on behalf of the customer

**Secondary Actors:**  
- Payment System — validates refund eligibility  
- Voucher System — (if refund restores a voucher, handled in UC-PT-002)  
- Finance Approval Workflow — routes the request for processing (UC-RF-002)

**Description:**  
Initiates a refund request for a previously completed payment. The system validates eligibility based on the refund policy (time window, fare type, refundable amount), creates a `Refund` record with `status='PENDING'`, and queues it for Finance approval and execution in UC-RF-002. The refund may be full (cancellation), partial (price difference from change), or for other reasons (overcharge, service failure).

**User Story:**  
As a Customer, I want to request a refund for my cancelled or changed ticket so that I can get my money back according to the refund policy.

**Preconditions:**
1. `Payment` exists with `paymentId`, `status` = `'COMPLETED'`.
2. `refundableAmount` = `payment.amount` − `payment.refundAmount` > 0 (there is still money to refund).
3. Refund reason is one of:
   - `'CANCELLATION'` — full ticket cancellation (triggered by UC-PT-002).
   - `'CHANGE_PRICE_DIFFERENCE'` — partial refund from a change (triggered by UC-PT-002).
   - `'OVERCHARGE'` — customer was charged more than the correct price.
   - `'SERVICE_FAILURE'` — trip delayed/cancelled by the company.
   - `'OTHER'` — other reasons requiring manager approval.
4. Refund window check (configurable):
   - For `CANCELLATION`: Must have been requested before `departureTime` − `cancellationWindowHours`.
   - For `SERVICE_FAILURE`: Up to 30 days after the trip date.
   - For `OTHER`: Requires manager approval.
5. Associated `PassengerTicket.status` = `'CANCELLED'` or `'CHANGED'` (the trigger event must have already occurred).
6. Staff has `TICKET_STAFF`, `MANAGER`, or `ADMIN` role (if staff-initiated).

**Postconditions:**

*On Success:*
1. `Refund` record created in `refund` table:
   - `status` = `'PENDING'`
   - `amount` = the refundable amount (full or partial)
   - `refundMethod` = determined from the original `Payment.paymentMethod`:
     - VNPAY → VNPAY
     - BANK_TRANSFER → BANK_TRANSFER (same account)
     - CASH → CASH (at counter) or BANK_TRANSFER (customer preference)
   - `reason` = provided reason code
   - `paymentId` = original payment ID
   - `transactionId` = NULL (assigned in UC-RF-002)
2. Refund is queued for Finance review (visible in the Finance dashboard under "Pending Refunds").
3. Notification to customer: "Yêu cầu hoàn tiền [amount]đ cho vé [ticketCode] đã được ghi nhận. Thời gian xử lý dự kiến: 1-3 ngày làm việc."
4. API response: `{ refundId, amount, status='PENDING', estimatedProcessingTime: '1-3 business days' }`.

*On Failure:*
- No `Refund` record is created.
- The ticket remains cancelled/changed but without a refund in progress.
- An appropriate error message is returned.

**Normal Sequence/Flow:**

1. Staff (or customer via self-service portal) navigates to the ticket detail page of a cancelled (`'CANCELLED'`) or changed (`'CHANGED'`) passenger ticket.
2. System retrieves:
   - `PassengerTicket` (current status, `totalPrice`, `ticketCode`).
   - All `Payment` records for this ticket (array, ordered by `paymentTime`).
   - `Payment.refundAmount` (cumulative refunds already processed).
3. System calculates:
   - `refundableAmount = payment.amount − payment.refundAmount`.
4. System displays:
   - Ticket code, cancellation/change reason, original amount, already refunded amount, eligible refund amount.
   - Suggested refund method (from original payment method).
   - A dropdown for refund reason (pre-populated if triggered from UC-PT-002).
5. Staff/Customer selects the refund method and confirms the reason.
6. Staff clicks "Gửi yêu cầu hoàn tiền."
7. System validates the refund against business rules (preconditions 2-4).
8. System creates the `Refund` record:
   ```sql
   INSERT INTO refund (
     paymentId, amount, reason, refundMethod, transactionId,
     status, refundTime, createdBy
   ) VALUES (
     :paymentId, :refundableAmount, :reason, :refundMethod,
     NULL, 'PENDING', NULL, :staffOrCustomerId
   )
   ```
9. System sends a notification to the Finance team (internal alert on the dashboard and/or email):
   - "Yêu cầu hoàn tiền #[refundId] — [amount]đ — Lý do: [reason] — [ticketCode]."
10. System displays a confirmation screen with the `refundId` and estimated processing time.

**Alternative Sequence/Flow:**

*A1 — Refund as Voucher Credit*  
- At step 5: Customer selects "Hoàn tiền dưới dạng voucher" instead of cash/bank.
- The system does NOT create a `Refund` record.
- Instead, the system creates a new `Voucher`:
  - `voucherCode` = `'RF-' + ticketCode` (e.g., `'RF-TKT-20250115-0042'`)
  - `discountValue` = refund amount
  - `discountType` = `'FIXED'`
  - `minOrderValue` = 0
  - `maxDiscountValue` = refund amount
  - `usageLimit` = 1
  - `startEffectiveDate` = NOW()
  - `endEffectiveDate` = NOW() + 180 days (6 months expiry)
- Customer receives a credit voucher to use on future bookings.

*A2 — Partial Refund with Multiple Refund Records*  
- A ticket may have multiple refunds over its lifecycle (e.g., partial cancellation + later change).
- Each refund creates a separate `Refund` record.
- The sum of all `Refund.amount` (with status = `'COMPLETED'`) = `Payment.refundAmount`.
- The system enforces that `Payment.refundAmount` never exceeds `Payment.amount` (DB constraint `CK_Payment_Amount`).

*A3 — Auto-Refund on Cancellation (No Manual Request)*  
- When UC-PT-002 (Cancel) detects that `Payment.status` = `'COMPLETED'` and refundable amount > 0, it may be configured to auto-create the `Refund` record (with `reason='CANCELLATION'`) rather than requiring a manual request.
- This is controlled by a configuration flag: `refund.autoCreateOnCancel` = true/false.
- If true, the Refund is created automatically during the cancellation transaction, and the customer is notified directly.

**Exceptional Sequence/Flow:**

*E1 — No Refundable Amount*  
- At step 3: `refundableAmount` ≤ 0 (the payment has already been fully refunded).
- Error: `"ERR_REFUND_001: Vé đã được hoàn tiền đầy đủ. Không thể yêu cầu hoàn thêm."`

*E2 — Refund Window Expired*  
- At step 4: The request is outside the allowed time window.
  - Cancellation window: `departureTime` < NOW() − `cancellationWindowHours`.
  - Service failure window: `departureTime` < NOW() − 30 days.
- Error: `"ERR_REFUND_002: Đã quá thời hạn hoàn tiền theo quy định ([N] ngày). Vui lòng liên hệ quản lý để được xét duyệt ngoại lệ."`

*E3 — Non-Refundable Fare*  
- At step 4: The ticket's fare rules (if implemented) mark it as non-refundable.
- Error: `"ERR_REFUND_003: Loại vé này không hỗ trợ hoàn tiền theo điều khoản đã đồng ý khi đặt vé."`

*E4 — Payment Not Completed*  
- At step 2: `Payment.status` ≠ `'COMPLETED'`.
- Error: `"ERR_REFUND_004: Chỉ có thể yêu cầu hoàn tiền cho giao dịch đã thanh toán thành công."`
- PENDING payments should be cancelled (not refunded). FAILED payments require no action.

*E5 — Missing Refund Reason*  
- At step 5: Reason is required but not selected/provided.
- For partial refunds or `'CHANGE_PRICE_DIFFERENCE'`: A reason is mandatory.
- Error: `"ERR_REFUND_005: Vui lòng nhập lý do hoàn tiền."`

*E6 — Duplicate Request*  
- At step 8: A `Refund` record with the same `paymentId`, `amount`, and `reason` already exists with `status='PENDING'`.
- Error: `"ERR_REFUND_007: Yêu cầu hoàn tiền trùng lặp. Yêu cầu #[existingRefundId] đang chờ xử lý."`

---

## UC-RF-002: Process/Execute Refund

**Use Case ID:** UC-RF-002  
**Name:** Process/Execute Refund

**Primary Actors:**  
- Finance Staff (MANAGER/ADMIN) — reviews and approves/rejects refund requests  
- Payment Gateway (VNPAY, Bank) — executes the actual financial refund

**Secondary Actors:**  
- Customer — receives the refunded money  
- Payment System — updates `Payment.refundAmount` after successful refund

**Description:**  
Finance Staff reviews pending refund requests (created by UC-RF-001) and executes them. For VNPAY refunds, the system calls the VNPAY refund API. For BANK_TRANSFER refunds, the staff initiates a manual transfer via the banking portal and records it. For CASH refunds, the staff pays the customer at the counter. On successful execution, the `Refund` and `Payment` records are updated, and the customer is notified.

**User Story:**  
As a Finance Staff, I want to review and execute refund requests so that customers receive their money back accurately and promptly through the correct payment channel.

**Preconditions:**
1. `Refund` exists with `refundId`, `status` = `'PENDING'`.
2. `Refund.amount` ≤ `payment.amount` − `payment.refundAmount` (validated at creation, re-validated at execution).
3. Finance Staff has `MANAGER` or `ADMIN` role.
4. For VNPAY: Merchant account has sufficient balance to process the refund. The original transaction is ≤ 365 days old (VNPAY policy).
5. For BANK_TRANSFER: Customer bank account details are available (from the original `Payment.callbackData` or from the refund request form).
6. For CASH: Customer can come to the counter with ID.

**Postconditions:**

*On Success:*
1. `Refund.status` = `'COMPLETED'`, `refundTime` = NOW(), `transactionId` = gateway refund reference (e.g., VNPAY refund transaction ID, or `'BANK_' + timestamp`, or `'CASH_' + timestamp').
2. `Payment.refundAmount` += `Refund.amount`.
3. If `Payment.refundAmount` = `Payment.amount`: Payment is fully refunded (status remains `'COMPLETED'` — the DB schema does not have a separate `'REFUNDED'` status; full refund is tracked via `refundAmount` equaling `amount`).
4. Customer notified:
   - "Hoàn tiền [amount]đ cho vé [ticketCode] thành công. Số tiền sẽ về tài khoản trong 1-3 ngày làm việc."

*On Failure:*
- `Refund.status` = `'FAILED'`, error details stored in `callbackData`.
- `Payment.refundAmount` is NOT updated.
- Finance staff is alerted for manual intervention.

**Normal Sequence/Flow (VNPAY Refund):**

1. Finance Staff opens the "Quản lý hoàn tiền" screen (e.g., `/management/refunds`).
2. Staff sees a list of `Refund` records with `status='PENDING'`, sorted by `createdAt` ascending.
3. Staff clicks on a specific refund request to view details:
   - Refund ID, amount, reason, requested date.
   - Original payment: method, transaction ID, amount, date.
   - Customer name, phone, ticket code, trip info.
   - Current `Payment.refundAmount` (to verify available balance).
4. Staff reviews and clicks "Xử lý hoàn tiền."
5. System displays a confirmation dialog: "Xác nhận hoàn [amount]đ qua VNPAY cho khách hàng [name]?"
6. Staff confirms.
7. System calls the VNPAY Refund API:
   - `vnp_RequestId` = refund `refundId` (idempotency key).
   - `vnp_TxnRef` = original `Payment.transactionId`.
   - `vnp_TransactionType` = `'03'` (full refund) or `'02'` (partial refund).
   - `vnp_Amount` = `refund.amount × 100`.
   - Signs the request with HMAC-SHA512.
8. VNPAY processes the refund (varies by bank — usually instant, but may take up to 24 hours).
9. VNPAY calls the refund callback IPN endpoint (similar structure to UC-PAY-001's IPN).
10. System validates the callback signature, transaction reference, and amount.
11. On success:
    ```sql
    BEGIN TRANSACTION
      UPDATE refund SET status='COMPLETED', refundTime=NOW(), transactionId=:vnp_TransactionNo, callbackData=:rawJson WHERE refundId=:id
      UPDATE payment SET refundAmount = refundAmount + :amount WHERE paymentId=:paymentId AND (refundAmount + :amount) <= amount
      -- The WHERE clause on payment prevents over-refund (CK_Payment_Amount constraint guard)
    COMMIT
    ```
12. Customer is notified via SMS/email.
13. The refund request moves to the "Completed" tab in the Finance dashboard.

**Normal Sequence/Flow (BANK_TRANSFER Refund):**

1-4. Same as VNPAY (steps 1-4).
5. Staff initiates a bank transfer from the company's bank account to the customer's bank account via the banking portal.
   - Beneficiary name, account number, bank name (retrieved from original payment data or provided by customer in the refund request).
   - Amount = `Refund.amount`.
   - Transfer content = `"HT-" + refundId` (for reconciliation).
6. Staff returns to the system and clicks "Đã chuyển khoản."
7. System prompts: "Nhập mã giao dịch ngân hàng:" — Staff enters the banking reference number.
8. Staff confirms.
9. System executes:
   ```sql
   UPDATE refund SET status='COMPLETED', refundTime=NOW(), transactionId=:bankRefNo, callbackData='{"executedBy": "staffId", "note": "Bank transfer completed manually"}' WHERE refundId=:id
   UPDATE payment SET refundAmount = refundAmount + :amount WHERE paymentId=:paymentId
   ```
10. Customer is notified.

**Normal Sequence/Flow (CASH Refund):**

1-4. Same as VNPAY (steps 1-4).
5. Customer arrives at the counter with their ID card (CMND/CCCD).
6. Staff verifies the customer's identity: name matches ticket, or phone number OTP.
7. Staff pays the cash amount from the register.
8. Staff clicks "Đã hoàn tiền mặt" on the system.
9. System prompts customer to sign on a digital signature pad (optional) or prints a receipt for the customer to sign.
10. Staff confirms.
11. System executes the SQL update (same as BANK_TRANSFER step 9).
12. Customer receives printed receipt: "Đã hoàn tiền [amount]đ — Mã hoàn: [refundId]."

**Alternative Sequence/Flow:**

*A1 — Refund to Different Method Than Original*  
- Original payment was CASH, but customer requests BANK_TRANSFER (because they cannot come to the counter).
- Finance Staff updates the `refundMethod` to `'BANK_TRANSFER'` before processing.
- Staff requests customer's bank account details via phone/email.
- Proceeds with BANK_TRANSFER flow.

*A2 — Split Refund Processing*  
- A partial refund: half goes to VNPAY, half to CASH (customer request).
- Staff creates two separate `Refund` records (or processes the same refund in two stages).
- Each half is processed independently.

*A3 — Rejection of Refund Request*  
- At step 4: Staff determines the refund is invalid (e.g., duplicate request, customer not entitled).
- Staff clicks "Từ chối" instead of "Xử lý."
- System prompts for rejection reason.
- `UPDATE refund SET status='FAILED', callbackData='{"rejectedBy": staffId, "reason": "..."}' WHERE refundId=?`
- Customer is notified: "Yêu cầu hoàn tiền [refundId] đã bị từ chối. Lý do: [reason]. Vui lòng liên hệ hotline để biết thêm chi tiết."

**Exceptional Sequence/Flow:**

*E1 — VNPAY Refund Rejected by Gateway*  
- At step 7: VNPAY API returns an error response:
  - `"02"`: Merchant account insufficient balance.
  - `"03"`: Original transaction > 1 year (refund not allowed by VNPAY policy).
  - `"04"`: Duplicate refund request (idempotency — refund already processed).
  - `"91"`: Unknown error.
- System catches the error:
  - `UPDATE refund SET status='FAILED', callbackData=errorResponse WHERE refundId=?`
  - Alert Finance: "Hoàn tiền VNPAY thất bại. Lý do: [message]. Cần xử lý thủ công qua chuyển khoản."
- Finance Staff should switch to BANK_TRANSFER method (A1).

*E2 — Over-Refund Protection (CK_Payment_Amount Guard)*  
- At step 11: Race condition — another refund was processed simultaneously, updating `Payment.refundAmount` between validation and execution.
- The SQL update:
  ```sql
  UPDATE payment SET refundAmount = refundAmount + :amount
  WHERE paymentId = :paymentId AND (refundAmount + :amount) <= amount
  ```
  If 0 rows affected: the WHERE clause prevented the over-refund.
- The `Refund` update is rolled back (transaction).
- System throws error: `"ERR_REFUND_006: Số tiền hoàn vượt quá số tiền có thể hoàn do một hoàn tiền khác đã được xử lý trước đó."`
- Finance Staff must review and adjust.

*E3 — Bank Transfer Failure*  
- At step 7: Customer's bank account details are wrong (wrong account number, closed account).
- The transfer is rejected by the bank (usually 1-2 business days later).
- Staff discovers the rejection and contacts the customer for correct details.
- A new refund request is created (the old one is marked `'FAILED'`).

*E4 — Customer Unreachable for Cash Refund*  
- At step 5: Customer does not show up at the counter within 7 days of the refund request.
- The `Refund` remains in `'PENDING'` state with a note.
- Escalation: Manager is notified to contact the customer via alternative channels (phone, email).
- If unreachable after 30 days: The refund may be canceled (`'FAILED'`) and the amount returned to company revenue.

---

## Appendix A: State Transition Tables

### PassengerTicket Status Lifecycle

```
                                 ┌────────────────────────────────────┐
                                 │              PENDING              │
                                 │        (Created, unpaid)          │
                                 └────┬──────────┬──────────┬────────┘
                                      │          │          │
                         Payment done │          │          │ Payment failed /
                                      │          │          │ lock timeout
                                      ▼          │          ▼
                              ┌──────────────┐   │   ┌──────────────┐
                              │  CONFIRMED   │   │   │  CANCELLED   │
                              │  (Paid)      │   │   │  (Aborted)   │
                              └──────┬───────┘   │   └──────────────┘
                                     │           │
                          Change     │           │ Cancel (paid)
                          seat/trip  ▼           ▼
                              ┌──────────────┐   ┌──────────────┐
                              │   CHANGED    │   │  CANCELLED   │
                              │  (Modified)  │   │  (Refund)    │
                              └──────┬───────┘   └──────────────┘
                                     │
                          Can change │ again
                                     ▼
                              ┌──────────────┐
                              │   CHANGED    │
                              │  (Re-change) │
                              └──────────────┘

   (No COMPLETED/EXPIRED status at the PassengerTicket level —
   these are tracked at the detail level.)
```

### PassengerTicketDetail Status Lifecycle

```
      PENDING ──────► CONFIRMED ──────► CHECKED_IN (Boarded)
         │                │
         │                ├──────────────► CANCELLED (Ticket cancelled)
         │                │
         ▼                ▼
     CANCELLED         EXPIRED (Trip departed + 2h without check-in)
  (Unpaid/Timeout)
```

### Payment Status Lifecycle

```
     PENDING ──────► COMPLETED ──────► (refundAmount tracked)
        │
        ▼
     FAILED
```

Note: The Payment entity does not have a `'REFUNDED'` status in the DB schema. Full or partial refunds are tracked via the `refundAmount` column. A payment is considered "fully refunded" when `refundAmount = amount`.

### Refund Status Lifecycle

```
     PENDING ──────► COMPLETED
        │
        ▼
     FAILED
```

No retry from FAILED. A failed refund requires a new request (new Refund record).

---

## Appendix B: Error Code Reference

| Code | HTTP | Domain | Vietnamese Message | Resolution |
|------|------|--------|-------------------|------------|
| ERR_SEAT_001 | 409 | Seat | Ghế đã được chọn bởi nhân viên khác. Vui lòng chọn ghế khác. | Refresh seat map, re-select available seats |
| ERR_SEAT_002 | 409 | Seat | Ghế mới không còn trống. Vui lòng chọn ghế khác. | Refresh seat map, re-select |
| ERR_TRIP_001 | 400 | Trip | Chuyến xe không khả dụng để bán vé. Vui lòng chọn chuyến khác. | Search for a different trip |
| ERR_TRIP_002 | 400 | Trip | Chuyến xe đã khởi hành. Không thể hủy/đổi vé sau giờ khởi hành. | N/A — policy limitation |
| ERR_TICKET_001 | 400 | Ticket | Điểm đón/trả không hợp lệ hoặc trùng nhau. Vui lòng chọn lại. | Re-select pickup/dropoff stops |
| ERR_TICKET_002 | 400 | Ticket | Vé không thể hủy/đổi do quá hạn hủy vé hoặc loại vé không hoàn tiền. | Contact manager for exception |
| ERR_VOUCHER_001 | 400 | Voucher | Voucher không tồn tại hoặc đã bị vô hiệu hóa. | Check voucher code |
| ERR_VOUCHER_002 | 400 | Voucher | Voucher đã hết hạn sử dụng. | Use a different voucher |
| ERR_VOUCHER_003 | 400 | Voucher | Voucher đã hết lượt sử dụng. | Use a different voucher |
| ERR_VOUCHER_004 | 400 | Voucher | Đơn hàng chưa đạt giá trị tối thiểu để áp dụng voucher (cần {minOrderValue}₫). | Add more items or remove voucher |
| ERR_VOUCHER_005 | 400 | Voucher | Voucher không áp dụng cho chuyến xe mới. Vé sẽ được tính giá gốc. | Proceed without voucher or select different trip |
| ERR_PAYMENT_001 | 502 | Payment | Cổng thanh toán không phản hồi, vé và ghế đã được giải phóng. Vui lòng đặt vé mới. | Re-book the ticket |
| ERR_PAYMENT_002 | 400 | Payment | Số tiền thanh toán không khớp. Vui lòng liên hệ quầy vé. | Contact support |
| ERR_PAYMENT_003 | 400 | Payment | Hết thời gian giữ ghế, vé đã bị hủy. Vui lòng đặt vé mới. | Re-book the ticket |
| ERR_PAYMENT_004 | 500 | Payment | Xử lý thanh toán/hoàn tiền thất bại. Vé không thay đổi. Vui lòng thử lại. | Retry or contact support |
| ERR_PAYMENT_005 | 400 | Payment | Thanh toán phải liên kết với chính xác 1 vé (vé khách hoặc vé hàng). Không được để trống hoặc cung cấp cả hai. | Fix API request payload |
| ERR_PAYMENT_006 | 409 | Payment | Vé đã được thanh toán. Không thể thanh toán trùng. | Customer already paid |
| ERR_PAYMENT_007 | 400 | Payment | Số tiền hoàn vượt quá số tiền thanh toán. Kiểm tra logic hoàn tiền. | Debug refund logic |
| ERR_PAYMENT_008 | 400 | Payment | Chữ ký giao dịch không hợp lệ. | Investigate callback security |
| ERR_CHECKIN_001 | 400 | Check-in | Mã QR không hợp lệ. Vui lòng thử quét lại hoặc nhập thủ công. | Retry scan or use manual input |
| ERR_CHECKIN_002 | 400 | Check-in | Vé chưa được thanh toán. Vui lòng hoàn tất thanh toán trước khi lên xe. | Direct passenger to ticket counter |
| ERR_CHECKIN_003 | 400 | Check-in | Mã QR đã hết hạn. Vui lòng liên hệ quầy vé để được hỗ trợ. | Counter can refresh QR |
| ERR_CHECKIN_004 | 400 | Check-in | Vé này thuộc chuyến xe khác ({routeName}, {departureTime}). | Switch trip or direct passenger |
| ERR_CHECKIN_005 | 400 | Check-in | Chuyến xe chưa đến giờ check-in. Vui lòng quay lại sau. | Wait for boarding window |
| ERR_CHECKIN_006 | 400 | Check-in | Đã quá thời gian check-in cho phép. Vui lòng liên hệ quầy vé. | Counter can assist |
| ERR_AUTH_001 | 403 | Auth | Bạn không được phân công cho chuyến xe này. Vui lòng liên hệ quản lý để được phân ca. | Contact manager for schedule change |
| ERR_REFUND_001 | 400 | Refund | Vé đã được hoàn tiền đầy đủ. Không thể yêu cầu hoàn thêm. | N/A |
| ERR_REFUND_002 | 400 | Refund | Đã quá thời hạn hoàn tiền theo quy định ({days} ngày). Liên hệ quản lý để xét duyệt ngoại lệ. | Manager override required |
| ERR_REFUND_003 | 400 | Refund | Loại vé này không hỗ trợ hoàn tiền theo điều khoản. | Review fare rules |
| ERR_REFUND_004 | 400 | Refund | Chỉ có thể yêu cầu hoàn tiền cho giao dịch đã thanh toán thành công. | Wait for payment or cancel PENDING ticket |
| ERR_REFUND_005 | 400 | Refund | Vui lòng nhập lý do hoàn tiền. | Provide reason |
| ERR_REFUND_006 | 400 | Refund | Số tiền hoàn vượt quá số tiền có thể hoàn do một hoàn tiền khác đã được xử lý. | Review refund history |
| ERR_REFUND_007 | 409 | Refund | Yêu cầu hoàn tiền trùng lặp. Yêu cầu #{existingRefundId} đang chờ xử lý. | Check existing refund |
| ERR_SYS_001 | 500 | System | Không thể tạo mã vé do lỗi hệ thống. Vui lòng thử lại. | Retry |
| ERR_SYS_002 | 500 | System | Lỗi tạo giao dịch, vui lòng thử lại. | Retry |

---

## Appendix C: Configuration Parameters

| Parameter | Key | Default | Description |
|-----------|-----|---------|-------------|
| Minimum booking window | `ticket.minBookingWindowMinutes` | 30 | Minutes before departure to allow new bookings |
| Seat lock timeout | `ticket.seatLockMinutes` | 15 | Minutes to hold seats in LOCKED state awaiting payment |
| Cancellation window | `ticket.cancellationWindowHours` | 2 | Hours before departure by which cancellation must be requested |
| Refund policy (service failure) | `ticket.refundWindowDays` | 30 | Days after trip to request refund for service failures |
| Check-in window | `ticket.checkinWindowHours` | 1 | Hours before departure when check-in becomes available |
| QR code grace period | `ticket.checkinGraceHours` | 2 | Hours after departure for which QR codes remain valid |
| VNPAY payment timeout | `payment.vnpay.timeoutMinutes` | 15 | Minutes to wait for VNPAY IPN callback before cancelling |
| VNPAY max transaction age for refund | `refund.vnpay.maxAgeDays` | 365 | Max age of original VNPAY transaction eligible for refund |
| Auto-create refund on cancel | `refund.autoCreateOnCancel` | true | Automatically create Refund record when cancelling a paid ticket |
| Voucher credit expiry | `refund.voucherCreditExpiryDays` | 180 | Days until a voucher credit (from refund) expires |

---

## Appendix D: DTO Definitions

### UC-PT-001: Create Ticket Request
```json
POST /api/v1/tickets
{
  "tripId": 123,
  "seatIds": [456, 457],
  "passengers": [
    {
      "fullName": "Nguyễn Văn A",
      "phone": "0901234567",
      "dob": "1990-01-15",
      "email": "a@example.com"
    },
    {
      "fullName": "Trần Thị B",
      "phone": "0907654321",
      "dob": "1995-05-20",
      "email": null
    }
  ],
  "voucherCode": "SUMMER20",
  "pickupStopId": 10,
  "dropoffStopId": 25,
  "customerId": 999,
  "paymentMethod": "VNPAY",
  "children": [
    { "ticketDetailIndex": 0, "fullname": "Nguyễn Văn C", "dob": "2020-03-10" }
  ]
}
```

### UC-PT-001: Create Ticket Response
```json
{
  "passengerTicketId": 1001,
  "ticketCode": "TKT-20250615-0042",
  "totalPrice": 450000.00,
  "discount": 50000.00,
  "voucherApplied": true,
  "voucherCode": "SUMMER20",
  "status": "PENDING",
  "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_TxnRef=...",
  "expiresAt": "2025-06-15T10:30:00",
  "details": [
    { "ticketDetailId": 2001, "seatLabel": "A1", "price": 250000.00 },
    { "ticketDetailId": 2002, "seatLabel": "A2", "price": 250000.00 }
  ]
}
```

### UC-PT-002: Cancel Ticket Request
```json
POST /api/v1/tickets/{ticketCode}/cancel
{
  "reason": "CUSTOMER_REQUEST",
  "partialDetailIds": null
}
```

### UC-PT-002: Cancel Ticket Response
```json
{
  "cancelled": true,
  "ticketCode": "TKT-20250615-0042",
  "refundAmount": 450000.00,
  "refundId": 3001,
  "message": "Vé đã được hủy thành công. Yêu cầu hoàn tiền đang được xử lý."
}
```

### UC-PT-003: Check-In Request (QR Scan)
```json
POST /api/v1/checkin/scan
{
  "tripSeatId": 456,
  "qrcode": "eyJ0aWNrZXREZXRhaWxJZCI6MjAwMSwidGltZXN0YW1wIjoiMjAyNS0wNi0xNVQwODowMDowMCIsImhtYWMiOiJhYmMxMjMifQ=="
}
```

### UC-PT-003: Check-In Response
```json
{
  "checkedIn": true,
  "passengerName": "Nguyễn Văn A",
  "seatNumber": "A1",
  "pickupStop": "Bến xe Miền Đông",
  "dropoffStop": "Bến xe Vũng Tàu",
  "tripInfo": {
    "tripId": 123,
    "routeName": "Sài Gòn - Vũng Tàu",
    "departureTime": "2025-06-15T08:00:00"
  }
}
```

### UC-PAY-001: VNPAY IPN Callback
```json
POST /api/v1/payment/vnpay/ipn
{
  "vnp_TxnRef": "VNPAY_20250615080000_A1B2C3",
  "vnp_Amount": "45000000",
  "vnp_ResponseCode": "00",
  "vnp_TransactionNo": "1234567890",
  "vnp_BankCode": "NCB",
  "vnp_PayDate": "20250615080405",
  "vnp_OrderInfo": "Thanh toán vé TKT-20250615-0042",
  "vnp_SecureHash": "abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890"
}
```

### UC-PAY-001: Payment Confirmation Response
```json
{
  "paymentId": 5001,
  "transactionId": "VNPAY_20250615080000_A1B2C3",
  "status": "COMPLETED",
  "amount": 450000.00,
  "paymentMethod": "VNPAY",
  "paymentTime": "2025-06-15T08:04:05",
  "ticketStatus": "CONFIRMED",
  "qrCodes": [
    {
      "detailId": 2001,
      "seatLabel": "A1",
      "qrcode": "data:image/png;base64,iVBORw0KG...",
      "expiredAt": "2025-06-15T10:00:00"
    },
    {
      "detailId": 2002,
      "seatLabel": "A2",
      "qrcode": "data:image/png;base64,iVBORw0KG...",
      "expiredAt": "2025-06-15T10:00:00"
    }
  ],
  "voucherUpdated": true
}
```

### UC-RF-001: Request Refund
```json
POST /api/v1/refunds
{
  "paymentId": 5001,
  "amount": 450000.00,
  "reason": "CANCELLATION",
  "refundMethod": "VNPAY",
  "notes": "Khách hàng yêu cầu hủy vé do thay đổi lịch trình"
}
```

### UC-RF-001: Request Refund Response
```json
{
  "refundId": 3001,
  "amount": 450000.00,
  "status": "PENDING",
  "refundMethod": "VNPAY",
  "estimatedProcessingTime": "1-3 ngày làm việc",
  "message": "Yêu cầu hoàn tiền đã được ghi nhận. Chúng tôi sẽ xử lý trong thời gian sớm nhất."
}
```

### UC-RF-002: Process Refund Request (Finance)
```json
POST /api/v1/refunds/{refundId}/process
{
  "action": "APPROVE",
  "transactionId": null,
  "notes": "Approved — process via VNPAY refund API"
}
```

### UC-RF-002: Process Refund Response
```json
{
  "refundId": 3001,
  "status": "COMPLETED",
  "transactionId": "VNPAY_REFUND_9876543210",
  "refundTime": "2025-06-16T09:15:00",
  "amount": 450000.00,
  "paymentRefundAmount": 450000.00,
  "paymentFullyRefunded": true
}
```

---

## Appendix E: Database Schema Reference (Key Constraints)

### CK_Payment_Target (Mutual Exclusivity)
```sql
CONSTRAINT CK_Payment_Target CHECK (
    ([passengerTicketId] IS NOT NULL AND [cargoTicketId] IS NULL) OR 
    ([passengerTicketId] IS NULL AND [cargoTicketId] IS NOT NULL)
)
```
**Business Rule:** A payment record must reference exactly one ticket type — either a passenger ticket or a cargo ticket. Never both, never neither.

### CK_Payment_Amount
```sql
CONSTRAINT CK_Payment_Amount CHECK ([amount] > 0 AND [refundAmount] >= 0)
```

### CK_PassengerTicket_Status
```sql
CONSTRAINT CK_PassengerTicket_Status CHECK ([status] IN ('PENDING', 'CONFIRMED', 'CHANGED', 'CANCELLED'))
```

### CK_PassengerDetail_Status
```sql
CONSTRAINT CK_PassengerDetail_Status CHECK ([status] IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CANCELLED', 'EXPIRED'))
```

### CK_PassengerTicket_Route
```sql
CONSTRAINT CK_PassengerTicket_Route CHECK ([pickupStopId] <> [dropoffStopId])
```

### CK_Refund_Amount
```sql
CONSTRAINT CK_Refund_Amount CHECK ([amount] > 0)
```

---

*End of Document*
