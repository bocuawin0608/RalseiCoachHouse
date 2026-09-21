import { expect, test } from '@playwright/test';

const username = process.env.E2E_TICKET_STAFF_USERNAME;
const password = process.env.E2E_TICKET_STAFF_PASSWORD;
const liveEnabled = Boolean(process.env.E2E_LIVE && username && password);

test.describe('Cargo Booking live environment', () => {
  test.skip(
    !liveEnabled,
    'Set E2E_LIVE=1, STAFF_BASE_URL, E2E_TICKET_STAFF_USERNAME, and E2E_TICKET_STAFF_PASSWORD.',
  );

  test('CG-TC-A01 @live ticket staff opens cargo operations', async ({ page }) => {
    await page.goto('/staff/login');
    await page.locator('[name="username"]').fill(username);
    await page.locator('[name="password"]').fill(password);
    await page.getByRole('button', { name: 'Đăng nhập vào hệ thống' }).click();

    await expect(page).toHaveURL(/\/staff\//);
    await page.goto('/staff/cargo-tickets');
    await expect(page.getByRole('heading', { name: 'Quản lý đơn hàng' })).toBeVisible();
    await expect(page.getByRole('button', { name: /Gửi hàng/ })).toBeVisible();
    await expect(page.getByRole('button', { name: /Kiểm tra hàng/ })).toBeVisible();
  });
});
