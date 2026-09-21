# Cargo Booking Playwright suite

All Cargo Booking browser tests live under `test/specs/cargo-booking`.

From the repository root:

```bash
cd test
npm install
npx playwright install chromium
npm run test:cargo
```

The default suite starts the staff portal on port 2999 and mocks only its HTTP
API boundary. The live smoke test is read-only and runs only when explicitly
configured:

```bash
export E2E_LIVE=1
export STAFF_BASE_URL="https://staff-test.example.com"
export E2E_TICKET_STAFF_USERNAME="ticket.staff"
export E2E_TICKET_STAFF_PASSWORD="replace-me"
npm run test:cargo:live
```

Never commit live credentials.
