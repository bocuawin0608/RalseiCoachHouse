import { expect } from '@playwright/test';

export async function openCargoCreateForm(page) {
  await page.goto('/staff/cargo-tickets/create');
  await expect(page.getByRole('heading', { name: 'Thêm đơn gửi hàng' })).toBeVisible();
  await expect(page.getByText('Văn phòng Quận 1', { exact: true }).first()).toBeVisible();
}

export async function fillValidCargoForm(page, {
  length = '0.5',
  width = '0.4',
  height = '0.5',
  quantity = '1',
} = {}) {
  await page.locator('[name="senderPhone"]').fill('0901111111');
  await page.locator('[name="senderName"]').fill('Nguyễn Người Gửi');
  await page.locator('[name="receiverPhone"]').fill('0902222222');
  await page.locator('[name="receiverName"]').fill('Trần Người Nhận');
  await page.locator('[name="dropoffStopId"]').selectOption('201');

  await page.getByRole('button', { name: 'Đính kèm hàng hóa' }).click();
  const item = page
    .getByText('Hàng hóa #1', { exact: true })
    .locator('xpath=ancestor::div[contains(@class, "bg-light")]');

  await item.locator('select').selectOption('501');
  const numericInputs = item.locator('input[type="number"]');
  await numericInputs.nth(0).fill(quantity);
  await numericInputs.nth(1).fill('5');
  await numericInputs.nth(2).fill(length);
  await numericInputs.nth(3).fill(width);
  await numericInputs.nth(4).fill(height);
}
