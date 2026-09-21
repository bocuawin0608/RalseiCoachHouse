import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './specs',
  fullyParallel: false,
  timeout: 30_000,
  expect: { timeout: 7_000 },
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: [
    ['list'],
    ['./reporters/status-reporter.js'],
    ['html', { outputFolder: 'reports/html', open: 'never' }],
  ],
  use: {
    baseURL: process.env.BASE_URL || 'https://127.0.0.1:3000',
    ignoreHTTPSErrors: true,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: process.env.BASE_URL
    ? undefined
    : {
        command: 'npm --prefix ../frontend-react run dev -- --host 127.0.0.1',
        url: 'https://127.0.0.1:3000',
        ignoreHTTPSErrors: true,
        reuseExistingServer: true,
        timeout: 120_000,
      },
  outputDir: 'reports/artifacts',
});
