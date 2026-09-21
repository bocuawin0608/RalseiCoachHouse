import { defineConfig, devices } from '@playwright/test';

const externalStaffUrl = process.env.STAFF_BASE_URL;

export default defineConfig({
  testDir: './specs/cargo-booking',
  fullyParallel: true,
  timeout: 30_000,
  expect: { timeout: 7_000 },
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['list'],
    ['./reporters/status-reporter.js'],
    ['html', { outputFolder: 'reports/cargo-html', open: 'never' }],
  ],
  use: {
    baseURL: externalStaffUrl || 'http://127.0.0.1:2999',
    ignoreHTTPSErrors: true,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'cargo-chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: externalStaffUrl
    ? undefined
    : {
        command: 'npm --prefix ../frontend-react-staff run dev -- --host 127.0.0.1',
        url: 'http://127.0.0.1:2999/staff/login',
        reuseExistingServer: !process.env.CI,
        timeout: 120_000,
      },
  outputDir: 'reports/cargo-artifacts',
});
