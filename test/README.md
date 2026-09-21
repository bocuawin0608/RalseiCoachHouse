# Ralsei black-box tests

The root `.gitignore` excludes `/test/`, so this is a local test module and no
backend source or Maven configuration is changed.

## Install Playwright once

From the repository root:

```bash
npm install --prefix test
npx --prefix test playwright install chromium
```

## Run Scenario B

The Playwright suite starts the customer Vite application automatically and
mocks search responses so UI edge cases are deterministic.

```bash
# Headless (normal CI/local run)
npm run test:scenario-b --prefix test

# Watch the browser
npm run test:scenario-b:headed --prefix test

# Step through with Playwright Inspector
npm run test:scenario-b:debug --prefix test

# Pressure the UI race/boundary suite 100 times
npm run test:scenario-b --prefix test -- --repeat-each=100
```

Useful direct commands from inside `test/`:

```bash
npx playwright test specs/scenario-b-failed.spec.js -g "BK-TC-B26"
npx playwright test --ui
npx playwright show-report reports/html
```

Playwright locates controls by accessible label/role, performs real browser
actions, and asserts the resulting DOM, request order, or URL. On failure it
keeps a trace, screenshot, and video in `test/reports/artifacts/`. Open a trace
with `npx playwright show-trace <trace.zip>`.

These are regression specifications for defects currently marked Failed. A red
test means the production behavior still violates the expected result; do not
weaken the assertion merely to make the dashboard green.

For direct backend pressure tests, see [postman/README.md](postman/README.md).
The complete traceability matrix is in [TEST_PLAN.md](TEST_PLAN.md).
